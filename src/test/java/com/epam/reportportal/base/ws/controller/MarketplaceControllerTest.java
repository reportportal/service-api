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

package com.epam.reportportal.base.ws.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.base.core.marketplace.exception.RegistryUnreachableException;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.marketplace.MarketplaceInstallResource;
import com.epam.reportportal.base.model.marketplace.MarketplaceCompatibility;
import com.epam.reportportal.base.model.marketplace.MarketplacePlugin;
import com.epam.reportportal.base.model.marketplace.MarketplacePluginDetail;
import com.epam.reportportal.base.model.marketplace.MarketplaceVersionSummary;
import com.epam.reportportal.base.ws.BaseMvcTest;
import com.epam.reportportal.base.core.marketplace.exception.PluginRemovedException;
import java.net.SocketTimeoutException;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

/**
 * Routing, guard and offline shape of {@code /v1/plugins}.
 */
class MarketplaceControllerTest extends BaseMvcTest {

  // marketplaceClient and installMarketplacePluginHandler are mocked on BaseMvcTest so this class
  // shares its context; both are reset between tests, so each test speaks to a registry of its own.

  @Test
  void catalogueIsReadableByAnyAuthenticatedUser() throws Exception {
    when(marketplaceClient.registryHost()).thenReturn("marketplace.reportportal.io");
    when(marketplaceClient.getCatalogue(any(), any())).thenReturn(List.of(
        new MarketplacePlugin("slack", "Slack", "2.0.0", "Notifier", "notifications", "public",
            "official", null, "slack")));

    mockMvc.perform(get("/v1/plugins").with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.registry.status").value("ONLINE"))
        .andExpect(jsonPath("$.registry.host").value("marketplace.reportportal.io"))
        .andExpect(jsonPath("$.available[0].id").value("slack"));
  }

  @Test
  void catalogueIsNotReadableWithoutAuthentication() throws Exception {
    mockMvc.perform(get("/v1/plugins")).andExpect(status().isUnauthorized());
  }

  @Test
  void installIsAdminOnly() throws Exception {
    mockMvc.perform(post("/v1/plugins/slack/install")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"version\":\"2.0.0\"}")
            .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isForbidden());
    verifyNoInteractions(installMarketplacePluginHandler);
  }

  @Test
  void installIsNotReachableWithoutAuthentication() throws Exception {
    mockMvc.perform(post("/v1/plugins/slack/install")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"version\":\"2.0.0\"}"))
        .andExpect(status().isUnauthorized());
    verifyNoInteractions(installMarketplacePluginHandler);
  }

  @Test
  void installReachesTheHandlerForAnAdmin() throws Exception {
    when(installMarketplacePluginHandler.install(eq("slack"), any(), any()))
        .thenReturn(new MarketplaceInstallResource(9L, "slack", "slack", "2.0.0"));

    mockMvc.perform(post("/v1/plugins/slack/install")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"version\":\"2.0.0\"}")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.integrationTypeId").value(9))
        .andExpect(jsonPath("$.version").value("2.0.0"));
  }

  /**
   * The constraint published to service-ui in {@code __fixtures__/request-constraints.json}:
   * {@code version} is mandatory, and a body without one is refused before anything is installed.
   * A consumer that learns the field name and not the rule earns this 400 in production.
   */
  @Test
  void anInstallWithNoVersionIsRefusedBeforeTheHandlerRuns() throws Exception {
    mockMvc.perform(post("/v1/plugins/slack/install")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());

    verify(installMarketplacePluginHandler, never()).install(any(), any(), any());
  }

  /** A blank one is not a version either: an empty string is refused like an absent field. */
  @Test
  void anInstallWithABlankVersionIsRefusedBeforeTheHandlerRuns() throws Exception {
    mockMvc.perform(post("/v1/plugins/slack/install")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"version\":\"   \"}")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());

    verify(installMarketplacePluginHandler, never()).install(any(), any(), any());
  }

  // The three the reviewer has to be able to tell apart on the wire: a version that does not
  // exist, a registry that answered unusably, and a registry that did not answer at all.

  @Test
  void aVersionTheRegistryDoesNotHaveIsNotFound() throws Exception {
    when(installMarketplacePluginHandler.install(eq("slack"), any(), any())).thenThrow(
        new ReportPortalException(ErrorType.MARKETPLACE_PLUGIN_NOT_FOUND,
            "version '9.9.9' of plugin 'slack' is not in the registry at 'registry.test'"));

    mockMvc.perform(post("/v1/plugins/slack/install")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"version\":\"9.9.9\"}")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("9.9.9")));
  }

  @Test
  void aRegistryThatAnsweredUnusablyIsABadGateway() throws Exception {
    when(installMarketplacePluginHandler.install(eq("slack"), any(), any())).thenThrow(
        new ReportPortalException(ErrorType.MARKETPLACE_REGISTRY_ERROR,
            "Unreadable artifact response for 'slack:2.0.0'"));

    mockMvc.perform(post("/v1/plugins/slack/install")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"version\":\"2.0.0\"}")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadGateway());
  }

  @Test
  void aRegistryThatCannotBeReachedIsServiceUnavailable() throws Exception {
    when(installMarketplacePluginHandler.install(eq("slack"), any(), any())).thenThrow(
        new ReportPortalException(ErrorType.MARKETPLACE_REGISTRY_UNREACHABLE,
            "Marketplace registry at 'registry.test' is unreachable: connect timed out"));

    mockMvc.perform(post("/v1/plugins/slack/install")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"version\":\"2.0.0\"}")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isServiceUnavailable());
  }

  // --- licence credentials ----------------------------------------------------------------------
  // The handler and its store are the real beans here; no @MockBean is added in this class,
  // because the @MockBean set is part of the context cache key (see BaseMvcTest).

  /** Go prints the key the registry issues as base64 of the seed followed by the public key. */
  private static String anEd25519PrivateKey() throws Exception {
    var pair = java.security.KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
    var pkcs8 = pair.getPrivate().getEncoded();
    var x509 = pair.getPublic().getEncoded();
    var raw = new byte[64];
    System.arraycopy(pkcs8, pkcs8.length - 32, raw, 0, 32);
    System.arraycopy(x509, x509.length - 32, raw, 32, 32);
    return java.util.Base64.getEncoder().encodeToString(raw);
  }

  private static String licenceBody(String customerId, String privateKey) {
    return "{\"customerId\":\"" + customerId + "\",\"privateKey\":\"" + privateKey + "\"}";
  }

  @Test
  void settingTheLicenceIsAdminOnly() throws Exception {
    mockMvc.perform(put("/v1/plugins/licence")
            .contentType(MediaType.APPLICATION_JSON)
            .content(licenceBody("acme-gmbh", anEd25519PrivateKey()))
            .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isForbidden());
  }

  @Test
  void inspectingTheLicenceIsAdminOnly() throws Exception {
    mockMvc.perform(get("/v1/plugins/licence").with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isForbidden());
  }

  @Test
  void licenceEndpointsAreNotReachableWithoutAuthentication() throws Exception {
    mockMvc.perform(get("/v1/plugins/licence")).andExpect(status().isUnauthorized());
  }

  @Test
  void anInstanceThatWasNeverGivenCredentialsSaysSo() throws Exception {
    mockMvc.perform(get("/v1/plugins/licence").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.configured").value(false))
        .andExpect(jsonPath("$.customerId").doesNotExist());
  }

  /**
   * The defect this pins is a serious one: the private key goes in and must never come back out of
   * any endpoint, in any field, encrypted or not.
   */
  @Test
  void theLicenceIsInspectableButTheKeyIsNeverReturned() throws Exception {
    var privateKey = anEd25519PrivateKey();

    var set = mockMvc.perform(put("/v1/plugins/licence")
            .contentType(MediaType.APPLICATION_JSON)
            .content(licenceBody("acme-gmbh", privateKey))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.configured").value(true))
        .andExpect(jsonPath("$.customerId").value("acme-gmbh"))
        .andReturn().getResponse().getContentAsString();

    var inspected = mockMvc.perform(
            get("/v1/plugins/licence").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.configured").value(true))
        .andExpect(jsonPath("$.customerId").value("acme-gmbh"))
        .andReturn().getResponse().getContentAsString();

    for (var body : List.of(set, inspected)) {
      assertFalse(body.contains(privateKey), body);
      // Not only the whole key: no fragment of it, and no field that could be carrying one.
      assertFalse(body.contains(privateKey.substring(0, 16)), body);
      assertFalse(body.toLowerCase(Locale.ROOT).contains("privatekey"), body);
    }
  }

  /**
   * The whole point of storing credentials: a premium plugin is locked until an admin sets them
   * and unlocked the moment they do — not when a catalogue cache happens to expire.
   */
  @Test
  void aPremiumPluginIsLockedUntilCredentialsAreSetAndUnlockedAfterwards() throws Exception {
    when(marketplaceClient.registryHost()).thenReturn("marketplace.reportportal.io");
    // getCatalogue takes (category, q): a unique q keeps this test off the shared catalogue cache.
    when(marketplaceClient.getCatalogue(any(), eq("locked-probe"))).thenReturn(List.of(
        new MarketplacePlugin("premium-jira", "Jira Premium", "3.0.0", "Tracker", "bug-tracking",
            "premium", "official", "https://reportportal.io/contact", "premium-jira")));

    mockMvc.perform(get("/v1/plugins?q=locked-probe").with(token(oAuthHelper.getDefaultToken())))
        .andExpect(jsonPath("$.available[0].locked").value(true))
        .andExpect(jsonPath("$.available[0].contactUrl")
            .value("https://reportportal.io/contact"));

    mockMvc.perform(put("/v1/plugins/licence")
            .contentType(MediaType.APPLICATION_JSON)
            .content(licenceBody("acme-gmbh", anEd25519PrivateKey()))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    mockMvc.perform(get("/v1/plugins?q=locked-probe").with(token(oAuthHelper.getDefaultToken())))
        .andExpect(jsonPath("$.available[0].locked").value(false));
  }

  @Test
  void deletingTheLicenceIsAdminOnly() throws Exception {
    mockMvc.perform(delete("/v1/plugins/licence").with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isForbidden());
  }

  @Test
  void deletingTheLicenceIsNotReachableWithoutAuthentication() throws Exception {
    mockMvc.perform(delete("/v1/plugins/licence")).andExpect(status().isUnauthorized());
  }

  /**
   * What an operator who pasted the wrong key, or whose entitlement ended, has to be able to do:
   * put the instance back to holding nothing, with premium plugins locked again.
   */
  @Test
  void deletingCredentialsUnconfiguresTheInstanceAndRelocksPremiumPlugins() throws Exception {
    when(marketplaceClient.registryHost()).thenReturn("marketplace.reportportal.io");
    // A q of its own keeps this test off the catalogue cache the other tests share.
    when(marketplaceClient.getCatalogue(any(), eq("delete-probe"))).thenReturn(List.of(
        new MarketplacePlugin("premium-jira", "Jira Premium", "3.0.0", "Tracker", "bug-tracking",
            "premium", "official", "https://reportportal.io/contact", "premium-jira")));
    mockMvc.perform(put("/v1/plugins/licence")
            .contentType(MediaType.APPLICATION_JSON)
            .content(licenceBody("acme-gmbh", anEd25519PrivateKey()))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
    mockMvc.perform(get("/v1/plugins?q=delete-probe").with(token(oAuthHelper.getDefaultToken())))
        .andExpect(jsonPath("$.available[0].locked").value(false));

    mockMvc.perform(delete("/v1/plugins/licence").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.configured").value(false))
        .andExpect(jsonPath("$.customerId").doesNotExist());

    mockMvc.perform(get("/v1/plugins/licence").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(jsonPath("$.configured").value(false));
    mockMvc.perform(get("/v1/plugins?q=delete-probe").with(token(oAuthHelper.getDefaultToken())))
        .andExpect(jsonPath("$.available[0].locked").value(true))
        .andExpect(jsonPath("$.available[0].contactUrl")
            .value("https://reportportal.io/contact"));
  }

  /** Removing what is not there is the state being asked for, so it answers rather than fails. */
  @Test
  void deletingWhenNothingIsConfiguredIsNotAnError() throws Exception {
    for (var attempt = 0; attempt < 2; attempt++) {
      mockMvc.perform(delete("/v1/plugins/licence").with(token(oAuthHelper.getSuperadminToken())))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.configured").value(false));
    }
  }

  /**
   * The other constraint published to service-ui: a blank customer id is not a customer id. The
   * key is valid here, so nothing but {@code @NotBlank} stands between this and an instance stored
   * as signing for nobody.
   */
  @Test
  void aLicenceWithABlankCustomerIdIsRefusedBeforeAnythingIsStored() throws Exception {
    // Deleting first is idempotent and makes "nothing was stored" mean something wherever this
    // test lands in the order.
    mockMvc.perform(delete("/v1/plugins/licence").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());

    mockMvc.perform(put("/v1/plugins/licence")
            .contentType(MediaType.APPLICATION_JSON)
            .content(licenceBody("   ", anEd25519PrivateKey()))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());

    mockMvc.perform(get("/v1/plugins/licence").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(jsonPath("$.configured").value(false));
  }

  @Test
  void aKeyThatIsNotAnEd25519KeyIsRefusedWhileTheAdminIsStillLookingAtIt() throws Exception {
    mockMvc.perform(put("/v1/plugins/licence")
            .contentType(MediaType.APPLICATION_JSON)
            .content(licenceBody("acme-gmbh", "bm90LWEta2V5"))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());

    mockMvc.perform(get("/v1/plugins/licence").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(jsonPath("$.configured").value(false));
  }

  @Test
  void unreachableRegistryStillRendersThePage() throws Exception {
    when(marketplaceClient.registryHost()).thenReturn("offline.reportportal.test");
    when(marketplaceClient.getCatalogue(any(), any())).thenThrow(new RegistryUnreachableException(
        "offline.reportportal.test", new SocketTimeoutException("Read timed out")));

    mockMvc.perform(get("/v1/plugins?q=jira").with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.registry.status").value("OFFLINE"))
        .andExpect(jsonPath("$.registry.host").value("offline.reportportal.test"))
        .andExpect(jsonPath("$.available").isEmpty());
  }
  @Test
  void aPluginsMarketplacePageIsReadableByAnyAuthenticatedUser() throws Exception {
    when(marketplaceClient.registryHost()).thenReturn("marketplace.reportportal.io");
    when(marketplaceClient.getPlugin("detail-jira")).thenReturn(
        new MarketplacePluginDetail("detail-jira", "Jira", "1.6.0", "Tracks issues", null, null,
            "bug-tracking", new MarketplaceCompatibility(">=25.0"), null, "public", null,
            "official", "1.6.0", "jira"));
    when(marketplaceClient.listVersions("detail-jira")).thenReturn(List.of(
        new MarketplaceVersionSummary("1.6.0", Instant.parse("2026-03-12T10:15:30Z"), false, null,
            null)));

    mockMvc.perform(get("/v1/plugins/detail-jira").with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.registry.status").value("ONLINE"))
        .andExpect(jsonPath("$.registry.host").value("marketplace.reportportal.io"))
        .andExpect(jsonPath("$.plugin.id").value("detail-jira"))
        .andExpect(jsonPath("$.plugin.latestVersion").value("1.6.0"))
        .andExpect(jsonPath("$.versions[0].version").value("1.6.0"))
        .andExpect(jsonPath("$.locked").value(false));
  }

  /**
   * The page degrades exactly as the catalogue does, because the UI reads one envelope for both.
   */
  @Test
  void anUnreachableRegistryStillRendersThePluginPage() throws Exception {
    when(marketplaceClient.registryHost()).thenReturn("offline.reportportal.test");
    when(marketplaceClient.getPlugin("detail-offline")).thenThrow(new RegistryUnreachableException(
        "offline.reportportal.test", new SocketTimeoutException("Read timed out")));

    mockMvc.perform(get("/v1/plugins/detail-offline").with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.registry.status").value("OFFLINE"))
        .andExpect(jsonPath("$.registry.host").value("offline.reportportal.test"))
        .andExpect(jsonPath("$.plugin").doesNotExist())
        .andExpect(jsonPath("$.versions").isEmpty());
  }

  @Test
  void aPluginsMarketplacePageIsNotReadableWithoutAuthentication() throws Exception {
    mockMvc.perform(get("/v1/plugins/detail-anon")).andExpect(status().isUnauthorized());
  }

  /**
   * The page has to be able to say "removed from the marketplace, still running here", so the
   * registry's 410 arrives as state on a 200 rather than as a 404.
   */
  @Test
  void aRemovedPluginIsServedAsRemovedRatherThanAsNotFound() throws Exception {
    when(marketplaceClient.registryHost()).thenReturn("marketplace.reportportal.io");
    when(marketplaceClient.getPlugin("detail-gone")).thenThrow(new PluginRemovedException(
        "detail-gone", "Vendor withdrew it", Instant.parse("2026-01-05T12:00:00Z"),
        "operator@rp.io"));

    mockMvc.perform(get("/v1/plugins/detail-gone").with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.registry.status").value("ONLINE"))
        .andExpect(jsonPath("$.plugin.id").value("detail-gone"))
        .andExpect(jsonPath("$.removed.removalReason").value("Vendor withdrew it"))
        .andExpect(jsonPath("$.removed.removedBy").value("operator@rp.io"));
  }

  /**
   * {@code /licence} is a route, not a plugin id: the detail template must not swallow it, or an
   * admin reading the licence would get a registry lookup for a plugin called "licence".
   */
  @Test
  void theLicenceRouteIsNotSwallowedByThePluginDetailTemplate() throws Exception {
    mockMvc.perform(get("/v1/plugins/licence").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.configured").exists());
    verify(marketplaceClient, never()).getPlugin("licence");
  }
}
