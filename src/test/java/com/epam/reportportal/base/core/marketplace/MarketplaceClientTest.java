/*
 * Copyright 2025 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.base.core.marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.epam.reportportal.base.core.configs.MarketplaceConfig;
import com.epam.reportportal.base.core.marketplace.exception.LicenceFailure;
import com.epam.reportportal.base.core.marketplace.exception.LicenceRejectedException;
import com.epam.reportportal.base.core.marketplace.exception.PluginRemovedException;
import com.epam.reportportal.base.core.marketplace.exception.RegistryResponseException;
import com.epam.reportportal.base.core.marketplace.exception.RegistryUnreachableException;
import com.epam.reportportal.base.core.marketplace.exception.VersionBlockedException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

/**
 * Wire-level tests of {@link MarketplaceClient} against a mocked registry.
 */
class MarketplaceClientTest {

  private static final String BASE_URL = "http://registry.internal";

  private RestTemplate restTemplate;
  private MockRestServiceServer server;
  private MarketplaceClient client;

  @BeforeEach
  void setUp() {
    // Real bean wiring: the tolerant ObjectMapper and converters are under test too.
    restTemplate = new MarketplaceConfig(BASE_URL, Duration.ofSeconds(3), Duration.ofSeconds(15))
        .marketplaceRestTemplate();
    server = MockRestServiceServer.bindTo(restTemplate).build();
    client = new MarketplaceClient(restTemplate, BASE_URL);
  }

  @Test
  void catalogueSendsFiltersAsQueryParams() {
    server.expect(requestTo(BASE_URL + "/api/v1/plugins?category=bug-tracking&q=jira"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("""
            {"plugins":[
              {"id":"jira","name":"Jira","latestVersion":"1.4.2","description":"BTS",
               "category":"bug-tracking","access":"public","tier":"official","pf4jId":"jira"}
            ]}""", MediaType.APPLICATION_JSON));

    var plugins = client.getCatalogue("bug-tracking", "jira");

    server.verify();
    assertEquals(1, plugins.size());
    var plugin = plugins.get(0);
    assertEquals("jira", plugin.id());
    assertEquals("Jira", plugin.name());
    assertEquals("1.4.2", plugin.latestVersion());
    assertEquals("BTS", plugin.description());
    assertEquals("bug-tracking", plugin.category());
    assertEquals("public", plugin.access());
    assertEquals("official", plugin.tier());
    assertEquals("jira", plugin.pf4jId());
  }

  @Test
  void catalogueOmitsBlankFilters() {
    server.expect(requestTo(BASE_URL + "/api/v1/plugins"))
        .andRespond(withSuccess("{\"plugins\":[]}", MediaType.APPLICATION_JSON));

    assertTrue(client.getCatalogue(null, "   ").isEmpty());
    server.verify();
  }

  @Test
  void catalogueToleratesMissingPf4jIdAndUnknownFields() {
    server.expect(requestTo(BASE_URL + "/api/v1/plugins"))
        .andRespond(withSuccess("""
            {"plugins":[{"id":"slack","name":"Slack","latestVersion":"2.0.0",
             "category":"notifications","access":"public","tier":"official",
             "iconUrl":"https://cdn/slack.png"}],"totalCount":1}""",
            MediaType.APPLICATION_JSON));

    var plugins = client.getCatalogue(null, null);

    assertEquals(1, plugins.size());
    assertNull(plugins.get(0).pf4jId());
  }

  @Test
  void pluginDetailIsParsed() {
    server.expect(requestTo(BASE_URL + "/api/v1/plugins/jira"))
        .andRespond(withSuccess("""
            {"id":"jira","name":"Jira","version":"1.4.2","description":"BTS",
             "author":{"name":"EPAM","email":"support@epam.com","url":"https://epam.com"},
             "license":"Apache-2.0","category":"bug-tracking",
             "compatibility":{"reportportal":">=25.1, <26.0"},
             "homepage":"https://rp.io/jira","access":"premium",
             "contactUrl":"https://rp.io/contact","tier":"official","latestVersion":"1.4.2"}""",
            MediaType.APPLICATION_JSON));

    var detail = client.getPlugin("jira");

    assertEquals("jira", detail.id());
    assertEquals("1.4.2", detail.latestVersion());
    assertEquals("EPAM", detail.author().name());
    assertEquals(">=25.1, <26.0", detail.compatibility().reportportal());
    assertEquals("premium", detail.access());
    assertEquals("https://rp.io/contact", detail.contactUrl());
    assertEquals("official", detail.tier());
  }

  @Test
  void versionListIsParsed() {
    server.expect(requestTo(BASE_URL + "/api/v1/plugins/jira/versions"))
        .andRespond(withSuccess("""
            {"pluginId":"jira","versions":[
              {"version":"1.4.2","publishedAt":"2026-08-01T10:00:00Z","blocked":false},
              {"version":"1.4.1","publishedAt":"2026-07-01T10:00:00Z","blocked":true,
               "blockedAt":"2026-07-20T09:30:00Z","blockReason":"CVE-2026-1"}]}""",
            MediaType.APPLICATION_JSON));

    var versions = client.listVersions("jira");

    assertEquals(2, versions.size());
    assertEquals(Instant.parse("2026-08-01T10:00:00Z"), versions.get(0).publishedAt());
    assertTrue(versions.get(1).blocked());
    assertEquals(Instant.parse("2026-07-20T09:30:00Z"), versions.get(1).blockedAt());
    assertEquals("CVE-2026-1", versions.get(1).blockReason());
  }

  @Test
  void versionDetailIsParsed() {
    server.expect(requestTo(BASE_URL + "/api/v1/plugins/jira/versions/1.4.1"))
        .andRespond(withSuccess("""
            {"id":"jira","name":"Jira","version":"1.4.1","description":"BTS",
             "author":{"name":"EPAM"},"license":"Apache-2.0","category":"bug-tracking",
             "compatibility":{"reportportal":">=25.1, <26.0"},"access":"public",
             "tier":"official","blocked":true,"blockedAt":"2026-07-20T09:30:00Z",
             "blockReason":"CVE-2026-1",
             "advisory":{"severity":"high","text":"Update now",
                         "attachedAt":"2026-07-19T08:00:00Z"},
             "sha256":"abc123","changelogUrl":"https://cdn/changelog.md",
             "screenshotUrls":["https://cdn/1.png","https://cdn/2.png"]}""",
            MediaType.APPLICATION_JSON));

    var detail = client.getVersion("jira", "1.4.1");

    assertEquals("abc123", detail.sha256());
    assertTrue(detail.blocked());
    assertEquals("CVE-2026-1", detail.blockReason());
    assertEquals(Instant.parse("2026-07-20T09:30:00Z"), detail.blockedAt());
    assertEquals("high", detail.advisory().severity());
    assertEquals(Instant.parse("2026-07-19T08:00:00Z"), detail.advisory().attachedAt());
    assertEquals("https://cdn/changelog.md", detail.changelogUrl());
    assertEquals(2, detail.screenshotUrls().size());
  }

  @Test
  void publicArtifactRedirectYieldsCdnUrlWithoutLicenceHeader() {
    server.expect(requestTo(BASE_URL + "/api/v1/plugins/jira/versions/1.4.2/artifact"))
        .andExpect(headerDoesNotExist(HttpHeaders.AUTHORIZATION))
        // Go's http.Redirect writes an HTML body alongside the 302.
        .andRespond(withStatus(HttpStatus.FOUND)
            .header(HttpHeaders.LOCATION, "https://cdn.rp.io/jira/1.4.2.jar")
            .contentType(MediaType.TEXT_HTML)
            .body("<a href=\"https://cdn.rp.io/jira/1.4.2.jar\">Found</a>."));

    var artifact = client.resolveArtifact("jira", "1.4.2", null);

    server.verify();
    assertEquals("https://cdn.rp.io/jira/1.4.2.jar", artifact.downloadUrl());
    assertNull(artifact.expiresAt());
  }

  @Test
  void premiumArtifactSendsLicenceJwtAndReturnsSignedUrl() {
    server.expect(requestTo(BASE_URL + "/api/v1/plugins/premium/versions/2.0.0/artifact"))
        .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer licence-jwt"))
        .andRespond(withSuccess("""
            {"downloadUrl":"https://cdn.rp.io/signed?exp=1","expiresAt":"2026-08-01T10:01:00Z"}""",
            MediaType.APPLICATION_JSON));

    var artifact = client.resolveArtifact("premium", "2.0.0", "licence-jwt");

    server.verify();
    assertEquals("https://cdn.rp.io/signed?exp=1", artifact.downloadUrl());
    assertEquals(Instant.parse("2026-08-01T10:01:00Z"), artifact.expiresAt());
  }

  @Test
  void blockedArtifactIsDistinguishedFromLicenceFailureByBody() {
    server.expect(requestTo(BASE_URL + "/api/v1/plugins/jira/versions/1.4.1/artifact"))
        .andRespond(withStatus(HttpStatus.FORBIDDEN)
            .contentType(MediaType.APPLICATION_JSON)
            .body("""
                {"blocked":true,"blockedAt":"2026-07-20T09:30:00Z","reason":"CVE-2026-1"}"""));

    var ex = assertThrows(VersionBlockedException.class,
        () -> client.resolveArtifact("jira", "1.4.1", "licence-jwt"));

    assertEquals("jira", ex.getPluginId());
    assertEquals("1.4.1", ex.getVersion());
    assertEquals("CVE-2026-1", ex.getReason());
    assertEquals(Instant.parse("2026-07-20T09:30:00Z"), ex.getBlockedAt());
  }

  @Test
  void genericForbiddenBodyIsLicenceFailureWithoutInventedDetail() {
    server.expect(requestTo(BASE_URL + "/api/v1/plugins/premium/versions/2.0.0/artifact"))
        .andRespond(withStatus(HttpStatus.FORBIDDEN)
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"code\":\"FORBIDDEN\",\"message\":\"Invalid license\"}"));

    var ex = assertThrows(LicenceRejectedException.class,
        () -> client.resolveArtifact("premium", "2.0.0", "licence-jwt"));

    assertEquals(LicenceFailure.UNSPECIFIED, ex.getFailure());
    assertEquals("FORBIDDEN", ex.getRegistryCode());
    assertEquals("Invalid license", ex.getRegistryMessage());
  }

  @Test
  void specificLicenceCodeIsPickedUpWhenRegistryStartsSendingIt() {
    server.expect(requestTo(BASE_URL + "/api/v1/plugins/premium/versions/2.0.0/artifact"))
        .andRespond(withStatus(HttpStatus.FORBIDDEN)
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"code\":\"LICENSE_EXPIRED\",\"message\":\"Entitlement expired\"}"));

    var ex = assertThrows(LicenceRejectedException.class,
        () -> client.resolveArtifact("premium", "2.0.0", "licence-jwt"));

    assertEquals(LicenceFailure.EXPIRED, ex.getFailure());
    assertEquals("LICENSE_EXPIRED", ex.getRegistryCode());
  }

  @Test
  void missingLicenceJwtIsUnauthorized() {
    server.expect(requestTo(BASE_URL + "/api/v1/plugins/premium/versions/2.0.0/artifact"))
        .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"code\":\"UNAUTHORIZED\",\"message\":\"License JWT required\"}"));

    var ex = assertThrows(LicenceRejectedException.class,
        () -> client.resolveArtifact("premium", "2.0.0", null));

    assertEquals(LicenceFailure.MISSING, ex.getFailure());
    assertEquals("License JWT required", ex.getRegistryMessage());
  }

  @Test
  void removedPluginTombstoneCarriesRemovalReason() {
    server.expect(requestTo(BASE_URL + "/api/v1/plugins/gone/versions/1.0.0/artifact"))
        .andRespond(withStatus(HttpStatus.GONE)
            .contentType(MediaType.APPLICATION_JSON)
            .body("""
                {"removed":"2026-06-01T12:00:00Z","removalReason":"licence violation",
                 "removedBy":"operator@rp.io"}"""));

    var ex = assertThrows(PluginRemovedException.class,
        () -> client.resolveArtifact("gone", "1.0.0", null));

    assertEquals("gone", ex.getPluginId());
    assertEquals("licence violation", ex.getRemovalReason());
    assertEquals(Instant.parse("2026-06-01T12:00:00Z"), ex.getRemovedAt());
  }

  @Test
  void removedPluginOnDetailRouteIsTyped() {
    server.expect(requestTo(BASE_URL + "/api/v1/plugins/gone"))
        .andRespond(withStatus(HttpStatus.GONE)
            .contentType(MediaType.APPLICATION_JSON)
            .body("""
                {"removed":"2026-06-01T12:00:00Z","removalReason":"licence violation",
                 "removedBy":"operator@rp.io"}"""));

    var ex = assertThrows(PluginRemovedException.class, () -> client.getPlugin("gone"));

    assertEquals("licence violation", ex.getRemovalReason());
  }

  @Test
  void readTimeoutBecomesUnreachableNamingTheHost() {
    server.expect(requestTo(BASE_URL + "/api/v1/plugins"))
        .andRespond(request -> {
          throw new SocketTimeoutException("Read timed out");
        });

    var ex = assertThrows(RegistryUnreachableException.class,
        () -> client.getCatalogue(null, null));

    assertEquals("registry.internal", ex.getHost());
    // startsWith, not contains: the wrapped cause quotes the URL and would mask a lost host.
    assertTrue(ex.getMessage().startsWith("Marketplace registry at 'registry.internal'"),
        ex.getMessage());
  }

  @Test
  void unexpectedStatusesArePassedThroughWithStatusAndCode() {
    server.expect(requestTo(BASE_URL + "/api/v1/plugins/absent"))
        .andRespond(withStatus(HttpStatus.NOT_FOUND)
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"code\":\"NOT_FOUND\",\"message\":\"Plugin not found\"}"));

    var ex = assertThrows(RegistryResponseException.class, () -> client.getPlugin("absent"));

    assertEquals(RegistryResponseException.class, ex.getClass());
    assertEquals(404, ex.getStatus());
    assertEquals("NOT_FOUND", ex.getRegistryCode());
    assertEquals("Plugin not found", ex.getRegistryMessage());
  }

  @Test
  void serverErrorIsNotMistakenForLicenceOrBlockedFailure() {
    server.expect(requestTo(BASE_URL + "/api/v1/plugins"))
        .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"code\":\"INTERNAL_ERROR\",\"message\":\"boom\"}"));

    var ex = assertThrows(RegistryResponseException.class,
        () -> client.getCatalogue(null, null));

    // Not a subclass: 5xx is neither a blocked version nor a licence problem.
    assertEquals(RegistryResponseException.class, ex.getClass());
    assertEquals(500, ex.getStatus());
  }
}
