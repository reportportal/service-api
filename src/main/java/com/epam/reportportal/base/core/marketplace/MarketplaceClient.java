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

import com.epam.reportportal.base.core.marketplace.exception.LicenceFailure;
import com.epam.reportportal.base.core.marketplace.exception.LicenceRejectedException;
import com.epam.reportportal.base.core.marketplace.exception.MarketplaceException;
import com.epam.reportportal.base.core.marketplace.exception.PluginRemovedException;
import com.epam.reportportal.base.core.marketplace.exception.RegistryProtocolException;
import com.epam.reportportal.base.core.marketplace.exception.RegistryResponseException;
import com.epam.reportportal.base.core.marketplace.exception.RegistryUnreachableException;
import com.epam.reportportal.base.core.marketplace.exception.VersionBlockedException;
import com.epam.reportportal.base.model.marketplace.BlockedArtifactBody;
import com.epam.reportportal.base.model.marketplace.MarketplaceArtifact;
import com.epam.reportportal.base.model.marketplace.MarketplacePlugin;
import com.epam.reportportal.base.model.marketplace.MarketplacePluginDetail;
import com.epam.reportportal.base.model.marketplace.MarketplacePluginList;
import com.epam.reportportal.base.model.marketplace.MarketplaceVersionDetail;
import com.epam.reportportal.base.model.marketplace.MarketplaceVersionList;
import com.epam.reportportal.base.model.marketplace.MarketplaceVersionSummary;
import com.epam.reportportal.base.model.marketplace.PluginTombstoneBody;
import com.epam.reportportal.base.model.marketplace.RegistryErrorBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Read-only client of the Go marketplace registry (/api/v1). Every failure leaves here as a typed
 * {@link MarketplaceException} carrying what the registry actually said.
 */
public class MarketplaceClient {

  private static final String PLUGINS_PATH = "/api/v1/plugins";

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper = MarketplaceJson.mapper();
  private final String registryUrl;
  private final String registryHost;

  /**
   * Creates a client.
   *
   * @param restTemplate the marketplace-only template, with its own timeouts
   * @param registryUrl  registry base URL, e.g. {@code https://marketplace.reportportal.io}
   */
  public MarketplaceClient(RestTemplate restTemplate, String registryUrl) {
    this.restTemplate = restTemplate;
    this.registryUrl = registryUrl;
    this.registryHost = Optional.ofNullable(URI.create(registryUrl).getHost()).orElse(registryUrl);
  }

  /**
   * GET /api/v1/plugins — the catalogue. Blank filters are not sent.
   *
   * @param category registry category, may be null or blank
   * @param q        free-text query, may be null or blank
   * @return catalogue entries, never null
   */
  public List<MarketplacePlugin> getCatalogue(String category, String q) {
    var uri = plugins()
        .queryParamIfPresent("category", Optional.ofNullable(StringUtils.trimToNull(category)))
        .queryParamIfPresent("q", Optional.ofNullable(StringUtils.trimToNull(q)))
        .build().encode().toUri();
    var response = get(uri, MarketplacePluginList.class, null, null);
    return response == null || response.plugins() == null ? List.of() : response.plugins();
  }

  /**
   * GET /api/v1/plugins/{pluginId}.
   */
  public MarketplacePluginDetail getPlugin(String pluginId) {
    var uri = plugins().pathSegment(pluginId).build().encode().toUri();
    return get(uri, MarketplacePluginDetail.class, pluginId, null);
  }

  /**
   * GET /api/v1/plugins/{pluginId}/versions.
   *
   * @return version summaries, never null
   */
  public List<MarketplaceVersionSummary> listVersions(String pluginId) {
    var uri = plugins().pathSegment(pluginId, "versions").build().encode().toUri();
    var response = get(uri, MarketplaceVersionList.class, pluginId, null);
    return response == null || response.versions() == null ? List.of() : response.versions();
  }

  /**
   * GET /api/v1/plugins/{pluginId}/versions/{version}.
   */
  public MarketplaceVersionDetail getVersion(String pluginId, String version) {
    var uri = plugins().pathSegment(pluginId, "versions", version).build().encode().toUri();
    return get(uri, MarketplaceVersionDetail.class, pluginId, version);
  }

  /**
   * GET /api/v1/plugins/{pluginId}/versions/{version}/artifact.
   *
   * <p>Public plugins answer 302 and resolve to the CDN URL of the Location header — the redirect
   * is deliberately not followed, service-api downloads the jar itself. Premium plugins need a
   * signed licence JWT and answer with a URL valid for about 60 seconds.
   *
   * @param licenceJwt Ed25519-signed licence JWT, null or blank for public plugins
   */
  public MarketplaceArtifact resolveArtifact(String pluginId, String version, String licenceJwt) {
    var uri = plugins().pathSegment(pluginId, "versions", version, "artifact")
        .build().encode().toUri();
    var headers = new HttpHeaders();
    if (StringUtils.isNotBlank(licenceJwt)) {
      headers.setBearerAuth(licenceJwt);
    }
    ResponseEntity<String> response;
    try {
      // String, not the DTO: the 302 carries an HTML body no JSON converter can read.
      response = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers),
          String.class);
    } catch (RestClientResponseException e) {
      throw mapError(e, pluginId, version);
    } catch (ResourceAccessException e) {
      throw new RegistryUnreachableException(registryHost, e);
    }
    if (response.getStatusCode().is3xxRedirection()) {
      var location = response.getHeaders().getLocation();
      if (location == null) {
        throw new RegistryProtocolException("Marketplace registry redirected the artifact of '"
            + pluginId + ":" + version + "' without a Location header");
      }
      return new MarketplaceArtifact(location.toString(), null);
    }
    MarketplaceArtifact artifact;
    try {
      artifact = objectMapper.readValue(response.getBody(), MarketplaceArtifact.class);
    } catch (Exception e) {
      throw new RegistryProtocolException("Unreadable artifact response for '" + pluginId + ":"
          + version + "'", e);
    }
    if (artifact.downloadUrl() == null) {
      throw new RegistryProtocolException("Marketplace registry returned no download URL for '"
          + pluginId + ":" + version + "'");
    }
    return artifact;
  }

  private UriComponentsBuilder plugins() {
    return UriComponentsBuilder.fromUriString(registryUrl).path(PLUGINS_PATH);
  }

  private <T> T get(URI uri, Class<T> type, String pluginId, String version) {
    try {
      return restTemplate.getForObject(uri, type);
    } catch (RestClientResponseException e) {
      throw mapError(e, pluginId, version);
    } catch (ResourceAccessException e) {
      throw new RegistryUnreachableException(registryHost, e);
    } catch (RestClientException e) {
      throw new RegistryProtocolException(
          "Unreadable response from marketplace registry at '" + registryHost + "'", e);
    }
  }

  /**
   * Maps a non-2xx response onto a typed exception. A 403 is ambiguous — only the body tells a
   * blocked version from a licence rejection.
   */
  private MarketplaceException mapError(RestClientResponseException e, String pluginId,
      String version) {
    var status = e.getStatusCode().value();
    var body = e.getResponseBodyAsString();
    if (status == HttpStatus.GONE.value()) {
      var tombstone = parse(body, PluginTombstoneBody.class);
      return tombstone == null
          ? new PluginRemovedException(pluginId, null, null, null)
          : new PluginRemovedException(pluginId, tombstone.removalReason(), tombstone.removed(),
              tombstone.removedBy());
    }
    if (status == HttpStatus.FORBIDDEN.value()) {
      var blocked = parse(body, BlockedArtifactBody.class);
      if (blocked != null && Boolean.TRUE.equals(blocked.blocked())) {
        return new VersionBlockedException(pluginId, version, blocked.reason(),
            blocked.blockedAt());
      }
    }
    var error = parse(body, RegistryErrorBody.class);
    var code = error == null ? null : error.code();
    var message = error == null ? null : error.message();
    if (status == HttpStatus.FORBIDDEN.value() || status == HttpStatus.UNAUTHORIZED.value()) {
      return new LicenceRejectedException(pluginId, version, status,
          LicenceFailure.from(code, status), code, message);
    }
    return new RegistryResponseException(status, code, message);
  }

  /**
   * Best-effort read of an error body: an unparseable one means no detail, never a lost failure.
   */
  private <T> T parse(String body, Class<T> type) {
    if (StringUtils.isBlank(body)) {
      return null;
    }
    try {
      return objectMapper.readValue(body, type);
    } catch (Exception e) {
      return null;
    }
  }
}
