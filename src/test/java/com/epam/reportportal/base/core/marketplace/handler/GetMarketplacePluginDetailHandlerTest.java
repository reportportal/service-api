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

package com.epam.reportportal.base.core.marketplace.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.marketplace.MarketplaceClient;
import com.epam.reportportal.base.core.marketplace.MarketplaceLicence;
import com.epam.reportportal.base.core.marketplace.MarketplaceRegistryCache;
import com.epam.reportportal.base.core.marketplace.exception.PluginRemovedException;
import com.epam.reportportal.base.core.marketplace.exception.RegistryNotFoundException;
import com.epam.reportportal.base.core.marketplace.exception.RegistryResponseException;
import com.epam.reportportal.base.core.marketplace.exception.RegistryUnreachableException;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.marketplace.MarketplaceAdvisory;
import com.epam.reportportal.base.model.marketplace.MarketplaceCompatibility;
import com.epam.reportportal.base.model.marketplace.MarketplacePluginDetail;
import com.epam.reportportal.base.model.marketplace.MarketplaceVersionDetail;
import com.epam.reportportal.base.model.marketplace.MarketplaceVersionSummary;
import com.google.common.base.Ticker;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * One plugin's marketplace page: what it says, what it leaves out, and what it refuses to answer.
 */
class GetMarketplacePluginDetailHandlerTest {

  private static final Instant WHEN = Instant.parse("2026-03-12T10:15:30Z");

  private MarketplaceClient client;
  private MarketplaceLicence licence;
  private GetMarketplacePluginDetailHandlerImpl handler;

  @BeforeEach
  void setUp() {
    client = mock(MarketplaceClient.class);
    licence = mock(MarketplaceLicence.class);
    when(client.registryHost()).thenReturn("marketplace.reportportal.io");
    when(licence.isConfigured()).thenReturn(false);
    handler = new GetMarketplacePluginDetailHandlerImpl(
        new MarketplaceRegistryCache(client, Duration.ofSeconds(60), Duration.ofSeconds(30),
            Duration.ofMinutes(5), Ticker.systemTicker()), licence);
  }

  private static MarketplacePluginDetail plugin(String id, String latestVersion, String access) {
    return new MarketplacePluginDetail(id, "Jira", latestVersion, "Tracks issues", null, null,
        "bug-tracking", new MarketplaceCompatibility(">=25.0"), null, access, null, "official",
        latestVersion, "jira");
  }

  private static MarketplaceVersionDetail version(String id, String version,
      MarketplaceAdvisory advisory, boolean blocked, String changelogUrl,
      List<String> screenshotUrls) {
    return new MarketplaceVersionDetail(id, "Jira", version, "Tracks issues", null, null,
        "bug-tracking", new MarketplaceCompatibility(">=25.0"), null, "public", null, "official",
        "jira", blocked, blocked ? WHEN : null, blocked ? "Signed with a revoked key" : null,
        advisory, "sha", changelogUrl, screenshotUrls);
  }

  @Test
  void theRegistrysViewOfAPublishedPluginIsWhatThePageGets() {
    when(client.getPlugin("jira")).thenReturn(plugin("jira", "1.6.0", "public"));
    when(client.listVersions("jira")).thenReturn(List.of(
        new MarketplaceVersionSummary("1.5.2", WHEN, true, WHEN, "Signed with a revoked key"),
        new MarketplaceVersionSummary("1.6.0", WHEN, false, null, null)));
    when(client.getVersion("jira", "1.6.0")).thenReturn(version("jira", "1.6.0", null, false, null,
        List.of("https://cdn.rp.io/jira/1.png")));

    var detail = handler.getPluginDetail("jira");

    assertEquals("jira", detail.id());
    assertEquals("Jira", detail.name());
    assertEquals("Tracks issues", detail.description());
    assertEquals("1.6.0", detail.latestVersion());
    assertEquals("public", detail.access());
    assertEquals("official", detail.tier());
    assertEquals(List.of("1.5.2", "1.6.0"),
        detail.versions().stream().map(entry -> entry.version()).toList());
    assertEquals(WHEN, detail.versions().get(0).publishedAt());
    assertTrue(detail.versions().get(0).blocked());
    assertFalse(detail.versions().get(1).blocked());
    assertEquals(List.of("https://cdn.rp.io/jira/1.png"), detail.screenshots());
    assertNull(detail.advisory());
    assertNull(detail.blocked());
    assertNull(detail.removed());
    assertFalse(detail.locked());
  }

  @Test
  void theAdvisoryAndBlockOfTheLatestVersionAreOnThePage() {
    when(client.getPlugin("jira")).thenReturn(plugin("jira", "1.6.0", "public"));
    when(client.getVersion("jira", "1.6.0")).thenReturn(version("jira", "1.6.0",
        new MarketplaceAdvisory("high", "Leaks the API key into the log", WHEN), true, null, null));

    var detail = handler.getPluginDetail("jira");

    assertEquals("high", detail.advisory().severity());
    assertEquals("Leaks the API key into the log", detail.advisory().text());
    assertEquals(WHEN, detail.advisory().attachedAt());
    assertEquals("1.6.0", detail.blocked().version());
    assertEquals(WHEN, detail.blocked().blockedAt());
    assertEquals("Signed with a revoked key", detail.blocked().reason());
  }

  @Test
  void theChangelogIsFetchedFromTheUrlTheRegistryPublishedAndSplitIntoLines() {
    when(client.getPlugin("jira")).thenReturn(plugin("jira", "1.6.0", "public"));
    when(client.getVersion("jira", "1.6.0")).thenReturn(version("jira", "1.6.0", null, false,
        "https://cdn.rp.io/jira/1.6.0/CHANGELOG.md", null));
    when(client.getDocument("https://cdn.rp.io/jira/1.6.0/CHANGELOG.md"))
        .thenReturn("Fixed a crash on an empty summary\n\n  Dropped the legacy field  \n");

    var changelog = handler.getPluginDetail("jira").changelog();

    assertEquals("1.6.0", changelog.version());
    assertEquals(List.of("Fixed a crash on an empty summary", "Dropped the legacy field"),
        changelog.lines());
  }

  @Test
  void aChangelogThatCouldNotBeReadLeavesTheBlockOutRatherThanFailingThePage() {
    when(client.getPlugin("jira")).thenReturn(plugin("jira", "1.6.0", "public"));
    when(client.getVersion("jira", "1.6.0")).thenReturn(version("jira", "1.6.0", null, false,
        "https://cdn.rp.io/jira/1.6.0/CHANGELOG.md", null));
    when(client.getDocument(anyString()))
        .thenThrow(new RegistryResponseException(500, "internal", "boom"));

    var detail = handler.getPluginDetail("jira");

    assertNull(detail.changelog());
    assertEquals("1.6.0", detail.latestVersion());
  }

  @Test
  void aVersionHistoryThatCouldNotBeReadIsEmptyRatherThanNull() {
    when(client.getPlugin("jira")).thenReturn(plugin("jira", "1.6.0", "public"));
    when(client.listVersions("jira"))
        .thenThrow(new RegistryResponseException(500, "internal", "boom"));

    var detail = handler.getPluginDetail("jira");

    assertEquals(List.of(), detail.versions());
    assertEquals(List.of(), detail.screenshots());
  }

  @Test
  void aRemovedPluginIsAnswered200WithItsTombstoneRatherThanAsNotFound() {
    // The plugin is gone from the marketplace and still running here. 404 would say the opposite
    // of both halves, and the page has to be able to say both.
    when(client.getPlugin("jira")).thenThrow(
        new PluginRemovedException("jira", "Vendor withdrew it", WHEN, "operator@rp.io"));

    var detail = handler.getPluginDetail("jira");

    assertEquals("jira", detail.id());
    assertEquals(WHEN, detail.removed().removed());
    assertEquals("Vendor withdrew it", detail.removed().removalReason());
    assertEquals("operator@rp.io", detail.removed().removedBy());
    assertEquals(List.of(), detail.versions());
    assertNull(detail.latestVersion());
    // Nothing else is worth asking for: every other route answers 410 too.
    verify(client, never()).listVersions(anyString());
    verify(client, never()).getVersion(anyString(), anyString());
  }

  @Test
  void aPluginTheRegistryDoesNotKnowIsNotFound() {
    when(client.getPlugin("nope"))
        .thenThrow(new RegistryNotFoundException("nope", null, "not_found", "Plugin not found"));

    var thrown = assertThrows(ReportPortalException.class, () -> handler.getPluginDetail("nope"));

    assertEquals(ErrorType.MARKETPLACE_PLUGIN_NOT_FOUND, thrown.getErrorType());
  }

  @Test
  void anUnreachableRegistryIsReportedAsUnreachableAndNotAsAMissingPlugin() {
    when(client.getPlugin("jira")).thenThrow(new RegistryUnreachableException(
        "marketplace.reportportal.io", new SocketTimeoutException("Read timed out")));

    var thrown = assertThrows(ReportPortalException.class, () -> handler.getPluginDetail("jira"));

    assertEquals(ErrorType.MARKETPLACE_REGISTRY_UNREACHABLE, thrown.getErrorType());
  }

  @Test
  void aRegistryThatAnsweredButRefusedIsAnErrorAboutTheRegistryNotAboutTheHost() {
    when(client.getPlugin("jira"))
        .thenThrow(new RegistryResponseException(503, "unavailable", "maintenance"));

    var thrown = assertThrows(ReportPortalException.class, () -> handler.getPluginDetail("jira"));

    assertEquals(ErrorType.MARKETPLACE_REGISTRY_ERROR, thrown.getErrorType());
  }

  @Test
  void aPremiumPluginIsLockedUntilThisInstanceHasALicence() {
    when(client.getPlugin("jira")).thenReturn(plugin("jira", "1.6.0", "premium"));

    assertTrue(handler.getPluginDetail("jira").locked());

    when(licence.isConfigured()).thenReturn(true);
    assertFalse(handler.getPluginDetail("jira").locked());
  }

  @Test
  void thePageIsAssembledFromCachedReadsRatherThanAskingTheRegistryAgain() {
    when(client.getPlugin("jira")).thenReturn(plugin("jira", "1.6.0", "public"));
    when(client.listVersions("jira")).thenReturn(List.of());
    when(client.getVersion("jira", "1.6.0")).thenReturn(version("jira", "1.6.0", null, false, null,
        null));

    handler.getPluginDetail("jira");
    handler.getPluginDetail("jira");

    verify(client, times(1)).getPlugin("jira");
    verify(client, times(1)).getVersion("jira", "1.6.0");
  }
}
