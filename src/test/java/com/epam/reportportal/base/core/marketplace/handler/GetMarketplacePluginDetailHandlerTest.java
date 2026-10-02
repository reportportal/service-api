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
import com.epam.reportportal.base.core.marketplace.ProductVersion;
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
import com.epam.reportportal.base.model.marketplace.catalogue.RegistryStatus;
import com.epam.reportportal.base.model.marketplace.detail.MarketplaceVersionResource;
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
            Duration.ofMinutes(5), Ticker.systemTicker()), licence, new ProductVersion("26.1"));
  }

  private static MarketplacePluginDetail plugin(String id, String latestVersion, String access) {
    return new MarketplacePluginDetail(id, "Jira", latestVersion, "Tracks issues", null, null,
        "bug-tracking", new MarketplaceCompatibility(">=25.0"), null, access, null, "official",
        latestVersion);
  }

  private static MarketplaceVersionDetail version(String id, String version,
      MarketplaceAdvisory advisory, boolean blocked, String changelogUrl,
      List<String> screenshotUrls) {
    return new MarketplaceVersionDetail(id, "Jira", version, "Tracks issues", null, null,
        "bug-tracking", new MarketplaceCompatibility(">=25.0"), null, "public", null, "official",
        blocked, blocked ? WHEN : null, blocked ? "Signed with a revoked key" : null,
        advisory, "sha", changelogUrl, screenshotUrls);
  }

  @Test
  void theRegistrysViewOfThePublishedPluginIsWhatThePageGets() {
    when(client.getPlugin("jira")).thenReturn(plugin("jira", "1.6.0", "public"));
    when(client.listVersions("jira")).thenReturn(List.of(
        new MarketplaceVersionSummary("1.5.2", WHEN, true, WHEN, "Signed with a revoked key",
            null, null),
        new MarketplaceVersionSummary("1.6.0", WHEN, false, null, null, null, null)));
    when(client.getVersion("jira", "1.6.0")).thenReturn(version("jira", "1.6.0", null, false, null,
        List.of("https://cdn.rp.io/jira/1.png")));

    var detail = handler.getPluginDetail("jira");

    assertEquals("jira", detail.plugin().id());
    assertEquals("Jira", detail.plugin().name());
    assertEquals("Tracks issues", detail.plugin().description());
    assertEquals("1.6.0", detail.plugin().latestVersion());
    assertEquals("public", detail.plugin().access());
    assertEquals("official", detail.plugin().tier());
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

  /**
   * The page and the catalogue share one degradation rule in the UI, so they have to share the
   * envelope that rule reads.
   */
  @Test
  void thePageCarriesTheSameRegistryEnvelopeTheCatalogueDoes() {
    when(client.getPlugin("jira")).thenReturn(plugin("jira", "1.6.0", "public"));

    var detail = handler.getPluginDetail("jira");

    assertEquals(RegistryStatus.ONLINE, detail.registry().status());
    assertEquals("marketplace.reportportal.io", detail.registry().host());
  }

  /**
   * A host that is down is a state of the marketplace, not a failure of this page: the catalogue
   * answers 200/OFFLINE for it, and the page has to be able to say the same thing or the UI's one
   * degradation rule cannot fire here.
   */
  @Test
  void anUnreachableRegistryAnswersTheOfflineEnvelopeRatherThanFailingThePage() {
    when(client.getPlugin("jira")).thenThrow(new RegistryUnreachableException(
        "marketplace.reportportal.io", new SocketTimeoutException("Read timed out")));

    var detail = handler.getPluginDetail("jira");

    assertEquals(RegistryStatus.OFFLINE, detail.registry().status());
    assertEquals("marketplace.reportportal.io", detail.registry().host());
    assertNull(detail.plugin());
    assertEquals(List.of(), detail.versions());
    assertEquals(List.of(), detail.screenshots());
    assertNull(detail.changelog());
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
  void changelogThatCouldNotBeReadLeavesTheBlockOutRatherThanFailingThePage() {
    when(client.getPlugin("jira")).thenReturn(plugin("jira", "1.6.0", "public"));
    when(client.getVersion("jira", "1.6.0")).thenReturn(version("jira", "1.6.0", null, false,
        "https://cdn.rp.io/jira/1.6.0/CHANGELOG.md", null));
    when(client.getDocument(anyString()))
        .thenThrow(new RegistryResponseException(500, "internal", "boom"));

    var detail = handler.getPluginDetail("jira");

    assertNull(detail.changelog());
    assertEquals("1.6.0", detail.plugin().latestVersion());
  }

  @Test
  void versionHistoryThatCouldNotBeReadIsEmptyRatherThanNull() {
    when(client.getPlugin("jira")).thenReturn(plugin("jira", "1.6.0", "public"));
    when(client.listVersions("jira"))
        .thenThrow(new RegistryResponseException(500, "internal", "boom"));

    var detail = handler.getPluginDetail("jira");

    assertEquals(List.of(), detail.versions());
    assertEquals(List.of(), detail.screenshots());
  }

  @Test
  void removedPluginIsAnswered200WithItsTombstoneRatherThanAsNotFound() {
    // The plugin is gone from the marketplace and still running here. 404 would say the opposite
    // of both halves, and the page has to be able to say both.
    when(client.getPlugin("jira")).thenThrow(
        new PluginRemovedException("jira", "Vendor withdrew it", WHEN, "operator@rp.io"));

    var detail = handler.getPluginDetail("jira");

    // The registry answered, so the envelope is ONLINE: the tombstone is a fact it told us.
    assertEquals(RegistryStatus.ONLINE, detail.registry().status());
    assertEquals("jira", detail.plugin().id());
    assertNull(detail.plugin().latestVersion());
    assertEquals(WHEN, detail.removed().removed());
    assertEquals("Vendor withdrew it", detail.removed().removalReason());
    assertEquals("operator@rp.io", detail.removed().removedBy());
    assertEquals(List.of(), detail.versions());
    // Nothing else is worth asking for: every other route answers 410 too.
    verify(client, never()).listVersions(anyString());
    verify(client, never()).getVersion(anyString(), anyString());
  }

  @Test
  void pluginTheRegistryDoesNotKnowIsNotFound() {
    when(client.getPlugin("nope"))
        .thenThrow(new RegistryNotFoundException("nope", null, "not_found", "Plugin not found"));

    var thrown = assertThrows(ReportPortalException.class, () -> handler.getPluginDetail("nope"));

    assertEquals(ErrorType.MARKETPLACE_PLUGIN_NOT_FOUND, thrown.getErrorType());
  }

  /**
   * A refusal is not the host being down. The registry is up and said no to this one request, so
   * nothing about this page could be learned — that is a failure, and OFFLINE would claim the
   * marketplace as a whole is unavailable when it is not.
   */
  @Test
  void registryThatAnsweredButRefusedIsAnErrorAboutTheRegistryNotAboutTheHost() {
    when(client.getPlugin("jira"))
        .thenThrow(new RegistryResponseException(503, "unavailable", "maintenance"));

    var thrown = assertThrows(ReportPortalException.class, () -> handler.getPluginDetail("jira"));

    assertEquals(ErrorType.MARKETPLACE_REGISTRY_ERROR, thrown.getErrorType());
  }

  @Test
  void premiumPluginIsLockedUntilThisInstanceIsLicensed() {
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

  /**
   * A version row answers one of three things, and the third is not a formatting accident. The
   * install handler already refuses "unknown" rather than guessing; a table that rendered unknown
   * as incompatible would be lying about a version that may well run, and as compatible would
   * offer an install this service is about to refuse.
   */
  @Test
  void versionRowSaysCompatibleIncompatibleOrNeitherOfThem() {
    when(client.getPlugin("jira")).thenReturn(plugin("jira", "1.6.0", "public"));
    when(client.listVersions("jira")).thenReturn(List.of(
        new MarketplaceVersionSummary("1.6.0", WHEN, false, null, null,
            new MarketplaceCompatibility(">=26.0"), null),
        new MarketplaceVersionSummary("1.5.2", WHEN, false, null, null,
            new MarketplaceCompatibility(">=27.0"), null),
        // published before the registry recorded a range
        new MarketplaceVersionSummary("1.4.0", WHEN, false, null, null, null, null),
        // recorded, but not as anything a range parser can read
        new MarketplaceVersionSummary("1.3.0", WHEN, false, null, null,
            new MarketplaceCompatibility("whenever"), null)));

    var verdicts = handler.getPluginDetail("jira").versions().stream()
        .collect(java.util.stream.Collectors.toMap(
            MarketplaceVersionResource::version,
            v -> String.valueOf(v.compatible())));

    assertEquals("true", verdicts.get("1.6.0"), "26.1 satisfies >=26.0");
    assertEquals("false", verdicts.get("1.5.2"), "26.1 does not satisfy >=27.0");
    assertEquals("null", verdicts.get("1.4.0"), "a version declaring no range is undecided");
    assertEquals("null", verdicts.get("1.3.0"), "a range that will not parse is undecided");
  }

  /**
   * The range goes out beside the verdict, verbatim, because the page has to say what the version
   * wants — "needs 26.2 or later" is the half of the sentence the verdict cannot carry. It is for
   * reading, not for deciding: the decision was already made here.
   */
  @Test
  void theDeclaredRangeTravelsWithTheVerdictSoTheReasonCanBeStated() {
    when(client.getPlugin("jira")).thenReturn(plugin("jira", "1.6.0", "public"));
    when(client.listVersions("jira")).thenReturn(List.of(
        new MarketplaceVersionSummary("1.6.0", WHEN, false, null, null,
            new MarketplaceCompatibility(">=27.0"), null),
        new MarketplaceVersionSummary("1.5.2", WHEN, false, null, null, null, null)));

    var versions = handler.getPluginDetail("jira").versions();

    assertEquals(">=27.0", versions.get(0).requires());
    assertFalse(versions.get(0).compatible());
    assertNull(versions.get(1).requires(), "a version declaring no range states none");
  }

  /** The instance not knowing its own release makes every row undecided, not every row bad. */
  @Test
  void anInstanceThatCannotNameItsReleaseDecidesNothing() {
    var blind = new GetMarketplacePluginDetailHandlerImpl(
        new MarketplaceRegistryCache(client, Duration.ofSeconds(60), Duration.ofSeconds(30),
            Duration.ofMinutes(5), Ticker.systemTicker()), licence, new ProductVersion(""));
    when(client.getPlugin("jira")).thenReturn(plugin("jira", "1.6.0", "public"));
    when(client.listVersions("jira")).thenReturn(List.of(
        new MarketplaceVersionSummary("1.6.0", WHEN, false, null, null,
            new MarketplaceCompatibility(">=26.0"), null)));

    assertNull(blind.getPluginDetail("jira").versions().get(0).compatible());
  }

  /**
   * One malformed entry should cost that entry, not the page. Our own registry appends values and
   * cannot emit a null element, but this is an HTTP boundary and the alternative to filtering is a
   * 500 where a list of the versions that did arrive would do.
   */
  @Test
  void nullEntryCostsItselfAndNotTheWholeListing() {
    when(client.getPlugin("jira")).thenReturn(plugin("jira", "1.6.0", "public"));
    var versions = new java.util.ArrayList<MarketplaceVersionSummary>();
    versions.add(null);
    versions.add(new MarketplaceVersionSummary("1.6.0", WHEN, false, null, null, null, null));
    when(client.listVersions("jira")).thenReturn(versions);

    var rows = handler.getPluginDetail("jira").versions();

    assertEquals(1, rows.size());
    assertEquals("1.6.0", rows.get(0).version());
  }

  /** An advisory belongs to the version it was attached to, and the row is where it is shown. */
  @Test
  void anAdvisoryRidesTheVersionItWasAttachedTo() {
    var advisory = new MarketplaceAdvisory("high", "CVE-2026-1234", WHEN);
    when(client.getPlugin("jira")).thenReturn(plugin("jira", "1.6.0", "public"));
    when(client.listVersions("jira")).thenReturn(List.of(
        new MarketplaceVersionSummary("1.6.0", WHEN, false, null, null, null, advisory),
        new MarketplaceVersionSummary("1.5.2", WHEN, false, null, null, null, null)));

    var versions = handler.getPluginDetail("jira").versions();

    assertEquals(advisory, versions.get(0).advisory());
    assertNull(versions.get(1).advisory(), "an advisory on one version is not on every version");
  }
}
