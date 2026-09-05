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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.marketplace.MarketplaceClient;
import com.epam.reportportal.base.core.marketplace.MarketplaceLicence;
import com.epam.reportportal.base.core.marketplace.ProductVersion;
import com.epam.reportportal.base.core.marketplace.exception.PluginRemovedException;
import com.epam.reportportal.base.core.marketplace.exception.RegistryResponseException;
import com.epam.reportportal.base.core.marketplace.exception.RegistryUnreachableException;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationTypeRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.IntegrationGroupEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationTypeDetails;
import com.epam.reportportal.base.model.marketplace.MarketplaceAdvisory;
import com.epam.reportportal.base.model.marketplace.MarketplaceCompatibility;
import com.epam.reportportal.base.model.marketplace.MarketplaceAuthor;
import com.epam.reportportal.base.model.marketplace.MarketplacePlugin;
import com.epam.reportportal.base.model.marketplace.MarketplacePluginDetail;
import com.epam.reportportal.base.model.marketplace.MarketplaceVersionDetail;
import com.epam.reportportal.base.model.marketplace.catalogue.InstalledPluginResource;
import com.epam.reportportal.base.model.marketplace.catalogue.MarketplaceCatalogueResource;
import com.epam.reportportal.base.model.marketplace.catalogue.RegistryStatus;
import com.google.common.base.Ticker;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * The merge matrix, the derivations and the offline degradation of the catalogue endpoint.
 */
class GetMarketplaceCatalogueHandlerTest {

  private MarketplaceClient client;
  private IntegrationTypeRepository integrationTypeRepository;
  private MarketplaceLicence licence;
  private FakeTicker ticker;
  private GetMarketplaceCatalogueHandlerImpl handler;

  @BeforeEach
  void setUp() {
    client = mock(MarketplaceClient.class);
    integrationTypeRepository = mock(IntegrationTypeRepository.class);
    licence = mock(MarketplaceLicence.class);
    ticker = new FakeTicker();
    when(client.registryHost()).thenReturn("marketplace.reportportal.io");
    when(licence.isConfigured()).thenReturn(false);
    handler = newHandler(new ProductVersion("25.2"));
  }

  private GetMarketplaceCatalogueHandlerImpl newHandler(ProductVersion productVersion) {
    var created = new GetMarketplaceCatalogueHandlerImpl(client, integrationTypeRepository,
        productVersion, licence, Duration.ofSeconds(60), Duration.ofSeconds(30),
        Duration.ofMinutes(5), ticker);
    // A @Value field is null on a hand-built handler; the property's own default is true.
    ReflectionTestUtils.setField(created, "uploadAllowed", true);
    return created;
  }

  /** Guava reads expiry off the ticker, so the TTLs can be crossed without sleeping. */
  private static final class FakeTicker extends Ticker {

    private long nanos;

    @Override
    public long read() {
      return nanos;
    }

    void advance(Duration duration) {
      nanos += duration.toNanos();
    }
  }

  private static IntegrationType installed(long id, String name, IntegrationGroupEnum group,
      String version, String marketplacePluginId) {
    var type = new IntegrationType();
    type.setId(id);
    type.setName(name);
    type.setIntegrationGroup(group);
    type.setEnabled(true);
    var details = new IntegrationTypeDetails();
    Map<String, Object> blob = new HashMap<>();
    if (version != null) {
      blob.put("version", version);
    }
    if (marketplacePluginId != null) {
      blob.put("marketplacePluginId", marketplacePluginId);
    }
    details.setDetails(blob);
    type.setDetails(details);
    return type;
  }

  private static MarketplacePlugin registryPlugin(String id, String name, String latestVersion,
      String category, String access, String pf4jId) {
    return new MarketplacePlugin(id, name, latestVersion, name + " description", category, access,
        "official", null, new MarketplaceAuthor(name + " Team", null, null), pf4jId);
  }

  private static MarketplaceVersionDetail versionDetail(String id, String version, String range,
      boolean blocked) {
    return new MarketplaceVersionDetail(id, id, version, null, null, null, "bug-tracking",
        new MarketplaceCompatibility(range), null, "public", null, "official", null, blocked, null,
        null, null, "sha", null, null);
  }

  private InstalledPluginResource installedNamed(MarketplaceCatalogueResource catalogue,
      String name) {
    return catalogue.installed().stream().filter(row -> name.equals(row.name())).findFirst()
        .orElseThrow(() -> new AssertionError("no installed row named " + name));
  }

  @Test
  void registryEntryIsMatchedToTheInstalledPluginByExactPf4jId() {
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(
        List.of(installed(7L, "jira", IntegrationGroupEnum.BTS, "1.4.1", null)));
    when(client.getCatalogue(null, null)).thenReturn(List.of(
        registryPlugin("plugin-bts-jira", "Jira", "1.4.1", "bug-tracking", "public", "jira")));

    var catalogue = handler.getCatalogue(null, null);

    assertEquals(RegistryStatus.ONLINE, catalogue.registry().status());
    assertEquals("marketplace.reportportal.io", catalogue.registry().host());
    assertEquals(1, catalogue.installed().size());
    var row = catalogue.installed().get(0);
    assertEquals(7L, row.integrationTypeId());
    assertEquals("1.4.1", row.version());
    assertEquals("BTS", row.groupType());
    assertNotNull(row.marketplace());
    assertEquals("plugin-bts-jira", row.marketplace().pluginId());
    // Matched, so it is installed here and must not also be on offer.
    assertTrue(catalogue.available().isEmpty());
  }

  @Test
  void pf4jIdMatchIsCaseSensitiveSoTheTwoGithubPluginsStayApart() {
    // plugin-auth-github declares 'github', plugin-bts-github declares 'GitHub'. Case-folding
    // would merge two different plugins into one catalogue entry.
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(
        List.of(installed(1L, "github", IntegrationGroupEnum.AUTH, "1.0.0", null)));
    when(client.getCatalogue(null, null)).thenReturn(List.of(
        registryPlugin("bts-github", "GitHub BTS", "2.0.0", "bug-tracking", "public", "GitHub")));

    var catalogue = handler.getCatalogue(null, null);

    assertNull(installedNamed(catalogue, "github").marketplace());
    assertEquals(1, catalogue.available().size());
    assertEquals("bts-github", catalogue.available().get(0).id());
  }

  @Test
  void persistedRegistryIdWinsOverThePf4jIdOfAnotherEntry() {
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(
        List.of(installed(5L, "jira", IntegrationGroupEnum.BTS, "1.0.0", "renamed-jira")));
    when(client.getCatalogue(null, null)).thenReturn(List.of(
        registryPlugin("renamed-jira", "Jira", "1.0.0", "bug-tracking", "public", "jira-next"),
        registryPlugin("legacy-jira", "Old Jira", "1.0.0", "bug-tracking", "public", "jira")));

    var catalogue = handler.getCatalogue(null, null);

    assertEquals("renamed-jira", installedNamed(catalogue, "jira").marketplace().pluginId());
    // The pf4jId-matching entry is a different plugin and stays on offer.
    assertEquals(List.of("legacy-jira"),
        catalogue.available().stream().map(entry -> entry.id()).toList());
  }

  @Test
  void aPersistedRegistryIdTheRegistryNoLongerKnowsIsNotDowngradedToANameGuess() {
    // The persisted id was written by the install path — hard evidence of where this plugin came
    // from. A pf4jId name match is a guess. When the id no longer resolves we cannot verify
    // anything about this plugin, and saying so beats naming a different registry entry as its
    // origin and offering that entry's versions as updates.
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(
        List.of(installed(5L, "jira", IntegrationGroupEnum.BTS, "1.0.0", "renamed-jira")));
    when(client.getCatalogue(null, null)).thenReturn(List.of(
        registryPlugin("legacy-jira", "Old Jira", "9.9.9", "bug-tracking", "public", "jira")));

    var catalogue = handler.getCatalogue(null, null);

    assertNull(installedNamed(catalogue, "jira").marketplace());
    // Unmatched, so the entry the name happened to hit is still just something on offer.
    assertEquals(List.of("legacy-jira"),
        catalogue.available().stream().map(entry -> entry.id()).toList());
    verify(client, never()).getVersion(anyString(), anyString());
  }

  @Test
  void unmatchedInstalledPluginIsReturnedWithoutAMarketplaceBlock() {
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(
        List.of(installed(3L, "homegrown", IntegrationGroupEnum.OTHER, "0.1.0", null)));
    when(client.getCatalogue(null, null)).thenReturn(List.of());

    var catalogue = handler.getCatalogue(null, null);

    assertEquals(RegistryStatus.ONLINE, catalogue.registry().status());
    assertNull(installedNamed(catalogue, "homegrown").marketplace());
  }

  @Test
  void registryPluginsNotInstalledHereAreOffered() {
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(List.of());
    when(client.getCatalogue(null, null)).thenReturn(List.of(
        registryPlugin("slack", "Slack", "2.0.0", "notifications", "public", "slack")));

    var catalogue = handler.getCatalogue(null, null);

    assertEquals(1, catalogue.available().size());
    var entry = catalogue.available().get(0);
    assertEquals("slack", entry.id());
    assertEquals("Slack", entry.name());
    assertEquals("2.0.0", entry.latestVersion());
    assertEquals("NOTIFICATION", entry.groupType());
    assertFalse(entry.locked());
  }

  /**
   * A locked premium row offers nothing but "get in touch", and that action opens this URL. Drop
   * it and the button is still drawn and still does nothing.
   */
  @Test
  void anAvailableEntryCarriesTheContactUrlTheDiscoverPremiumActionOpens() {
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(List.of());
    when(client.getCatalogue(null, null)).thenReturn(List.of(
        new MarketplacePlugin("premium-bts", "Premium BTS", "1.0.0", "Tracker", "bug-tracking",
            "premium", "official", "https://reportportal.io/contact", new MarketplaceAuthor("Premium BTS" + " Team", null, null), "premium-bts")));

    var entry = handler.getCatalogue(null, null).available().get(0);

    assertTrue(entry.locked());
    assertEquals("https://reportportal.io/contact", entry.contactUrl());
  }

  @Test
  void premiumIsLockedWithoutLicenceCredentialsAndNeverAsksTheRegistry() {
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(
        List.of(installed(2L, "premium-bts", IntegrationGroupEnum.BTS, "1.0.0", null)));
    when(client.getCatalogue(null, null)).thenReturn(List.of(
        registryPlugin("premium-bts", "Premium BTS", "1.0.0", "bug-tracking", "premium",
            "premium-bts"),
        registryPlugin("premium-auth", "Premium Auth", "1.0.0", "authorization", "premium",
            "premium-auth")));

    var catalogue = handler.getCatalogue(null, null);

    assertTrue(installedNamed(catalogue, "premium-bts").marketplace().locked());
    assertTrue(catalogue.available().get(0).locked());
    verify(client, never()).getPlugin(anyString());
  }

  @Test
  void premiumIsUnlockedOnceLicenceCredentialsExist() {
    when(licence.isConfigured()).thenReturn(true);
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(List.of());
    when(client.getCatalogue(null, null)).thenReturn(List.of(
        registryPlugin("premium-bts", "Premium BTS", "1.0.0", "bug-tracking", "premium", null)));

    assertFalse(handler.getCatalogue(null, null).available().get(0).locked());
  }

  @Test
  void updateIsOfferedWhenTheLatestVersionIsNewerCompatibleAndUnblocked() {
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(
        List.of(installed(7L, "jira", IntegrationGroupEnum.BTS, "1.4.9", null)));
    when(client.getCatalogue(null, null)).thenReturn(List.of(
        registryPlugin("jira", "Jira", "1.4.10", "bug-tracking", "public", "jira")));
    when(client.getVersion("jira", "1.4.10"))
        .thenReturn(versionDetail("jira", "1.4.10", ">=25.1, <26.0", false));

    var update = installedNamed(handler.getCatalogue(null, null), "jira").marketplace()
        .updateAvailable();

    assertNotNull(update);
    assertEquals("1.4.10", update.version());
  }

  @Test
  void noUpdateWhenTheLatestVersionIsNotNewer() {
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(
        List.of(installed(7L, "jira", IntegrationGroupEnum.BTS, "1.4.2", null)));
    when(client.getCatalogue(null, null)).thenReturn(List.of(
        registryPlugin("jira", "Jira", "1.4.2", "bug-tracking", "public", "jira")));

    assertNull(installedNamed(handler.getCatalogue(null, null), "jira").marketplace()
        .updateAvailable());
    // Nothing newer to consider, so no update probe is made. The one call is the badge read for
    // the version installed here, which happens to be the latest one.
    verify(client, times(1)).getVersion("jira", "1.4.2");
  }

  @Test
  void noUpdateWhenTheNewerVersionIsIncompatibleWithThisRelease() {
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(
        List.of(installed(7L, "jira", IntegrationGroupEnum.BTS, "1.4.2", null)));
    when(client.getCatalogue(null, null)).thenReturn(List.of(
        registryPlugin("jira", "Jira", "2.0.0", "bug-tracking", "public", "jira")));
    when(client.getVersion("jira", "2.0.0"))
        .thenReturn(versionDetail("jira", "2.0.0", ">=27.0", false));

    assertNull(installedNamed(handler.getCatalogue(null, null), "jira").marketplace()
        .updateAvailable());
  }

  @Test
  void noUpdateWhenThisReleaseIsAboveTheUpperBoundOfTheDeclaredWindow() {
    // 26.0 is past the '<26.0' end of the window, so the jar would not run here.
    handler = newHandler(new ProductVersion("26.0"));
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(
        List.of(installed(7L, "jira", IntegrationGroupEnum.BTS, "1.4.9", null)));
    when(client.getCatalogue(null, null)).thenReturn(List.of(
        registryPlugin("jira", "Jira", "1.4.10", "bug-tracking", "public", "jira")));
    when(client.getVersion("jira", "1.4.10"))
        .thenReturn(versionDetail("jira", "1.4.10", ">=25.1, <26.0", false));

    assertNull(installedNamed(handler.getCatalogue(null, null), "jira").marketplace()
        .updateAvailable());
  }

  @Test
  void noUpdateWhenTheDeclaredCompatibilityRangeCannotBeRead() {
    // An unreadable claim of compatibility is not a claim of compatibility.
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(
        List.of(installed(7L, "jira", IntegrationGroupEnum.BTS, "1.4.2", null)));
    when(client.getCatalogue(null, null)).thenReturn(List.of(
        registryPlugin("jira", "Jira", "1.4.3", "bug-tracking", "public", "jira")));
    when(client.getVersion("jira", "1.4.3"))
        .thenReturn(versionDetail("jira", "1.4.3", ">=25.x", false));

    assertNull(installedNamed(handler.getCatalogue(null, null), "jira").marketplace()
        .updateAvailable());
  }

  @Test
  void noUpdateWhenTheNewerVersionDeclaresNoCompatibilityAtAll() {
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(
        List.of(installed(7L, "jira", IntegrationGroupEnum.BTS, "1.4.2", null)));
    when(client.getCatalogue(null, null)).thenReturn(List.of(
        registryPlugin("jira", "Jira", "1.4.3", "bug-tracking", "public", "jira")));
    when(client.getVersion("jira", "1.4.3"))
        .thenReturn(versionDetail("jira", "1.4.3", null, false));

    assertNull(installedNamed(handler.getCatalogue(null, null), "jira").marketplace()
        .updateAvailable());
  }

  @Test
  void noUpdateWhenTheNewerVersionIsBlocked() {
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(
        List.of(installed(7L, "jira", IntegrationGroupEnum.BTS, "1.4.2", null)));
    when(client.getCatalogue(null, null)).thenReturn(List.of(
        registryPlugin("jira", "Jira", "1.4.3", "bug-tracking", "public", "jira")));
    when(client.getVersion("jira", "1.4.3"))
        .thenReturn(versionDetail("jira", "1.4.3", ">=25.1", true));

    assertNull(installedNamed(handler.getCatalogue(null, null), "jira").marketplace()
        .updateAvailable());
  }

  @Test
  void noUpdateWhenTheProductVersionIsUnknown() {
    // Offering an upgrade we cannot check is worse than offering none.
    handler = newHandler(new ProductVersion("  "));
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(
        List.of(installed(7L, "jira", IntegrationGroupEnum.BTS, "1.4.2", null)));
    when(client.getCatalogue(null, null)).thenReturn(List.of(
        registryPlugin("jira", "Jira", "1.4.3", "bug-tracking", "public", "jira")));
    when(client.getVersion("jira", "1.4.3"))
        .thenReturn(versionDetail("jira", "1.4.3", ">=25.1", false));

    assertNull(installedNamed(handler.getCatalogue(null, null), "jira").marketplace()
        .updateAvailable());
  }

  @Test
  void unreachableRegistryStillAnswersWithTheInstalledListAndNoMarketplaceBlocks() {
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(
        List.of(installed(7L, "jira", IntegrationGroupEnum.BTS, "1.4.1", "plugin-bts-jira")));
    when(client.getCatalogue(null, null)).thenThrow(new RegistryUnreachableException(
        "marketplace.reportportal.io", new SocketTimeoutException("Read timed out")));

    var catalogue = handler.getCatalogue(null, null);

    assertEquals(RegistryStatus.OFFLINE, catalogue.registry().status());
    assertEquals("marketplace.reportportal.io", catalogue.registry().host());
    assertEquals(1, catalogue.installed().size());
    assertNull(catalogue.installed().get(0).marketplace());
    assertTrue(catalogue.available().isEmpty());
  }

  @Test
  void registryServerErrorIsOfflineToo() {
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(List.of());
    when(client.getCatalogue(null, null))
        .thenThrow(new RegistryResponseException(500, "INTERNAL_ERROR", "boom"));

    assertEquals(RegistryStatus.OFFLINE, handler.getCatalogue(null, null).registry().status());
  }

  @Test
  void aDeadRegistryIsNotProbedOnEveryPageView() {
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(List.of());
    when(client.getCatalogue(null, null)).thenThrow(new RegistryUnreachableException(
        "marketplace.reportportal.io", new SocketTimeoutException("Read timed out")));

    handler.getCatalogue(null, null);
    handler.getCatalogue(null, null);
    handler.getCatalogue(null, null);

    verify(client, times(1)).getCatalogue(null, null);
  }

  @Test
  void catalogueIsCachedPerFilterCombination() {
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(List.of());
    when(client.getCatalogue(any(), any())).thenReturn(List.of());

    handler.getCatalogue("jira", null);
    handler.getCatalogue("jira", null);
    handler.getCatalogue("slack", null);
    handler.getCatalogue(null, "bug-tracking");

    verify(client, times(1)).getCatalogue(null, "jira");
    verify(client, times(1)).getCatalogue(null, "slack");
    verify(client, times(1)).getCatalogue("bug-tracking", null);
  }

  @Test
  void installedListIsFilteredByTheSameQueryAsTheRegistry() {
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(List.of(
        installed(1L, "jira", IntegrationGroupEnum.BTS, "1.0.0", null),
        installed(2L, "slack", IntegrationGroupEnum.NOTIFICATION, "1.0.0", null)));
    when(client.getCatalogue(null, "jir")).thenReturn(List.of());

    var catalogue = handler.getCatalogue("jir", null);

    assertEquals(List.of("jira"), catalogue.installed().stream().map(row -> row.name()).toList());
  }

  @Test
  void installedListIsFilteredByTheSameCategoryAsTheRegistry() {
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(List.of(
        installed(1L, "jira", IntegrationGroupEnum.BTS, "1.0.0", null),
        installed(2L, "slack", IntegrationGroupEnum.NOTIFICATION, "1.0.0", null)));
    when(client.getCatalogue("notifications", null)).thenReturn(List.of());

    var catalogue = handler.getCatalogue(null, "notifications");

    assertEquals(List.of("slack"), catalogue.installed().stream().map(row -> row.name()).toList());
  }

  @Test
  void theInstanceSaysWhetherAJarMayBeUploadedByHand() {
    // Not a registry fact and not a permission: the capability is switched off by environment,
    // and the page then leaves the control out rather than drawing a disabled one. It is
    // reported separately from the registry because the two fail independently — manual upload
    // is the escape valve precisely when the registry cannot be reached.
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(List.of());
    when(client.getCatalogue(null, null)).thenReturn(List.of());

    assertTrue(handler.getCatalogue(null, null).instance().uploadAllowed());

    ReflectionTestUtils.setField(handler, "uploadAllowed", false);
    assertFalse(handler.getCatalogue(null, null).instance().uploadAllowed());
  }

  @Test
  void theCategoryFilterSpeaksTheGroupNamesTheRowsCarry() {
    // Every row this endpoint returns carries groupType — an IntegrationGroupEnum name — so that
    // is what a caller filtering on the result sends back. The registry's own vocabulary is a
    // different one, and translating between them is this handler's job, not the UI's. Sending
    // "BTS" straight through emptied both halves of the page: no available group at all, and
    // installed rows stripped of every badge, because their marketplace block went missing too.
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(List.of(
        installed(1L, "jira", IntegrationGroupEnum.BTS, "1.0.0", null),
        installed(2L, "slack", IntegrationGroupEnum.NOTIFICATION, "1.0.0", null)));
    when(client.getCatalogue("bug-tracking", null)).thenReturn(List.of(
        registryPlugin("azure-devops", "Azure DevOps", "2.1.0", "bug-tracking", "public", null)));

    var catalogue = handler.getCatalogue(null, "BTS");

    assertEquals(List.of("jira"), catalogue.installed().stream().map(row -> row.name()).toList());
    assertEquals(List.of("Azure DevOps"),
        catalogue.available().stream().map(row -> row.name()).toList());
  }

  @Test
  void aCategoryNobodyRecognisesNarrowsToNothingRatherThanWidensToEverything() {
    // A filter that is not understood must not quietly become "no filter". It reaches the
    // registry untouched, which knows no such category either, and the page comes back empty.
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(
        List.of(installed(1L, "jira", IntegrationGroupEnum.BTS, "1.0.0", null)));
    when(client.getCatalogue("ANALYZER", null)).thenReturn(List.of());

    var catalogue = handler.getCatalogue(null, "ANALYZER");

    assertTrue(catalogue.installed().isEmpty());
    assertTrue(catalogue.available().isEmpty());
  }

  @Test
  void anInstalledRowCarriesTheRegistrysNameAndDescription() {
    // A PF4J plugin is identified by an id like "jira", and without these two fields that id is
    // all the list has to print — while the available half beside it shows "Jira Server".
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(
        List.of(installed(1L, "jira", IntegrationGroupEnum.BTS, "1.0.0", null)));
    when(client.getCatalogue(null, null)).thenReturn(List.of(
        registryPlugin("jira", "Jira Server", "1.0.0", "bug-tracking", "public", "jira")));

    var entry = handler.getCatalogue(null, null).installed().get(0).marketplace();

    assertEquals("Jira Server", entry.name());
    assertEquals("Jira Server description", entry.description());
  }

  @Test
  void aFilteredOutInstalledPluginIsStillNotOfferedAsAvailable() {
    // Filtering decides what is shown, never what is installed. The local group and the registry
    // category are two independent declarations and can disagree, so a category filter can hide
    // the installed row while the registry still returns its entry.
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(
        List.of(installed(1L, "jira", IntegrationGroupEnum.OTHER, "1.0.0", null)));
    when(client.getCatalogue("bug-tracking", null)).thenReturn(List.of(
        registryPlugin("jira", "Jira", "1.0.0", "bug-tracking", "public", "jira")));

    var catalogue = handler.getCatalogue(null, "bug-tracking");

    assertTrue(catalogue.installed().isEmpty());
    // It is installed, so it is not something to install, filter or no filter.
    assertTrue(catalogue.available().isEmpty());
  }

  @Test
  void aRegistryThatAnsweredOneFilterBadlyIsStillAskedAboutAnother() {
    // The registry answered, so it is up; the refusal is about this request, not about the host.
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(List.of());
    when(client.getCatalogue(null, "jira"))
        .thenThrow(new RegistryResponseException(400, "BAD_REQUEST", "boom"));
    when(client.getCatalogue(null, "slack")).thenReturn(List.of(
        registryPlugin("slack", "Slack", "2.0.0", "notifications", "public", "slack")));

    assertEquals(RegistryStatus.OFFLINE, handler.getCatalogue("jira", null).registry().status());
    assertEquals(RegistryStatus.ONLINE, handler.getCatalogue("slack", null).registry().status());
  }

  @Test
  void aDeadHostIsNotProbedAgainForEveryFilterTheUserTypes() {
    // Each probe of an unreachable host costs the whole request deadline, so keying that verdict
    // by filter lets a user pay it once per keystroke.
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(List.of());
    when(client.getCatalogue(any(), any())).thenThrow(new RegistryUnreachableException(
        "marketplace.reportportal.io", new SocketTimeoutException("Read timed out")));

    assertEquals(RegistryStatus.OFFLINE, handler.getCatalogue("j", null).registry().status());
    assertEquals(RegistryStatus.OFFLINE, handler.getCatalogue("ji", null).registry().status());
    assertEquals(RegistryStatus.OFFLINE, handler.getCatalogue("jir", null).registry().status());
    assertEquals(RegistryStatus.OFFLINE,
        handler.getCatalogue(null, "bug-tracking").registry().status());

    verify(client, times(1)).getCatalogue(any(), any());
  }

  @Test
  void aRecoveredRegistryIsProbedAgainOnceTheOfflineVerdictExpires() {
    // A suppression that never lapses is strictly worse than probing: "down" becomes permanent.
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(List.of());
    when(client.getCatalogue(null, null))
        .thenThrow(new RegistryUnreachableException("marketplace.reportportal.io",
            new SocketTimeoutException("Read timed out")))
        .thenReturn(List.of(
            registryPlugin("slack", "Slack", "2.0.0", "notifications", "public", "slack")));

    assertEquals(RegistryStatus.OFFLINE, handler.getCatalogue(null, null).registry().status());
    ticker.advance(Duration.ofSeconds(29));
    assertEquals(RegistryStatus.OFFLINE, handler.getCatalogue(null, null).registry().status());
    verify(client, times(1)).getCatalogue(null, null);

    ticker.advance(Duration.ofSeconds(2));

    assertEquals(RegistryStatus.ONLINE, handler.getCatalogue(null, null).registry().status());
    verify(client, times(2)).getCatalogue(null, null);
  }

  @Test
  void aCataloguePageIsNotRebuiltFromTheRegistryUntilItsOwnTtlExpires() {
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(List.of());
    when(client.getCatalogue(null, null)).thenReturn(List.of());

    handler.getCatalogue(null, null);
    ticker.advance(Duration.ofSeconds(59));
    handler.getCatalogue(null, null);
    verify(client, times(1)).getCatalogue(null, null);

    ticker.advance(Duration.ofSeconds(2));
    handler.getCatalogue(null, null);

    verify(client, times(2)).getCatalogue(null, null);
  }

  @Test
  void aFailingVersionDetailIsNotRefetchedOnEveryPageView() {
    // The catalogue is served but the version route is not, so without a negative cache this
    // costs one probe per pending-update plugin per page view.
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(
        List.of(installed(7L, "jira", IntegrationGroupEnum.BTS, "1.4.2", null)));
    when(client.getCatalogue(null, null)).thenReturn(List.of(
        registryPlugin("jira", "Jira", "1.4.3", "bug-tracking", "public", "jira")));
    when(client.getVersion("jira", "1.4.3"))
        .thenThrow(new RegistryResponseException(500, "INTERNAL_ERROR", "boom"));

    assertNull(installedNamed(handler.getCatalogue(null, null), "jira").marketplace()
        .updateAvailable());
    assertNull(installedNamed(handler.getCatalogue(null, null), "jira").marketplace()
        .updateAvailable());
    assertNull(installedNamed(handler.getCatalogue(null, null), "jira").marketplace()
        .updateAvailable());

    verify(client, times(1)).getVersion("jira", "1.4.3");
  }

  @Test
  void aFailingVersionDetailIsRetriedOnceTheNegativeVerdictExpires() {
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(
        List.of(installed(7L, "jira", IntegrationGroupEnum.BTS, "1.4.2", null)));
    when(client.getCatalogue(null, null)).thenReturn(List.of(
        registryPlugin("jira", "Jira", "1.4.3", "bug-tracking", "public", "jira")));
    when(client.getVersion("jira", "1.4.3"))
        .thenThrow(new RegistryResponseException(500, "INTERNAL_ERROR", "boom"))
        .thenReturn(versionDetail("jira", "1.4.3", ">=25.1, <26.0", false));

    assertNull(installedNamed(handler.getCatalogue(null, null), "jira").marketplace()
        .updateAvailable());
    ticker.advance(Duration.ofSeconds(31));

    var update = installedNamed(handler.getCatalogue(null, null), "jira").marketplace()
        .updateAvailable();

    assertNotNull(update);
    assertEquals("1.4.3", update.version());
  }

  @Test
  void oneUnreachableVersionCallStopsTheRestOfThePageProbingTheSameDeadHost() {
    // Both plugins have a pending update, so both want a version detail. The first call proves
    // the host is unreachable; paying the deadline again for the second is time already known to
    // be wasted, and a page with ten pending updates would pay it ten times.
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(List.of(
        installed(1L, "jira", IntegrationGroupEnum.BTS, "1.4.2", null),
        installed(2L, "slack", IntegrationGroupEnum.NOTIFICATION, "1.0.0", null)));
    when(client.getCatalogue(null, null)).thenReturn(List.of(
        registryPlugin("jira", "Jira", "1.4.3", "bug-tracking", "public", "jira"),
        registryPlugin("slack", "Slack", "2.0.0", "notifications", "public", "slack")));
    when(client.getVersion("jira", "1.4.3")).thenThrow(new RegistryUnreachableException(
        "marketplace.reportportal.io", new SocketTimeoutException("Read timed out")));

    var catalogue = handler.getCatalogue(null, null);

    assertNull(installedNamed(catalogue, "jira").marketplace().updateAvailable());
    assertNull(installedNamed(catalogue, "slack").marketplace().updateAvailable());
    verify(client, never()).getVersion("slack", "2.0.0");
  }

  @Test
  void anUnreachableVersionRouteMarksTheWholeHostDown() {
    // The host verdict is one verdict wherever it was learned: a version call that times out has
    // already proved the catalogue call for the next filter will time out too.
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(
        List.of(installed(7L, "jira", IntegrationGroupEnum.BTS, "1.4.2", null)));
    when(client.getCatalogue(any(), any())).thenReturn(List.of(
        registryPlugin("jira", "Jira", "1.4.3", "bug-tracking", "public", "jira")));
    when(client.getVersion("jira", "1.4.3")).thenThrow(new RegistryUnreachableException(
        "marketplace.reportportal.io", new SocketTimeoutException("Read timed out")));

    assertEquals(RegistryStatus.ONLINE, handler.getCatalogue(null, null).registry().status());

    assertEquals(RegistryStatus.OFFLINE, handler.getCatalogue("jira", null).registry().status());
    verify(client, never()).getCatalogue(null, "jira");
  }
  private static MarketplaceVersionDetail advisedVersion(String id, String version,
      MarketplaceAdvisory advisory, boolean blocked, Instant blockedAt, String blockReason) {
    return new MarketplaceVersionDetail(id, id, version, null, null, null, "bug-tracking",
        new MarketplaceCompatibility(">=25.0"), null, "public", null, "official", null, blocked,
        blockedAt, blockReason, advisory, "sha", null, null);
  }

  @Test
  void theAdvisoryOnTheInstalledVersionIsWhatTheRowCarries() {
    // The badge on an installed row is a statement about the code this instance runs, so it is
    // read for 1.4.1 — what is installed — and not for the latest version nobody is running.
    var attachedAt = Instant.parse("2026-02-01T09:00:00Z");
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(
        List.of(installed(7L, "jira", IntegrationGroupEnum.BTS, "1.4.1", null)));
    when(client.getCatalogue(null, null)).thenReturn(List.of(
        registryPlugin("jira", "Jira", "1.5.0", "bug-tracking", "public", "jira")));
    when(client.getVersion("jira", "1.4.1")).thenReturn(advisedVersion("jira", "1.4.1",
        new MarketplaceAdvisory("high", "Leaks the API key into the log", attachedAt), false, null,
        null));
    // The latest version is clean; reading it instead would report no advisory at all.
    when(client.getVersion("jira", "1.5.0")).thenReturn(advisedVersion("jira", "1.5.0", null, false,
        null, null));

    var row = installedNamed(handler.getCatalogue(null, null), "jira");

    assertNotNull(row.marketplace().advisory());
    assertEquals("high", row.marketplace().advisory().severity());
    assertEquals("Leaks the API key into the log", row.marketplace().advisory().text());
    assertEquals(attachedAt, row.marketplace().advisory().attachedAt());
    assertNull(row.marketplace().blocked());
    assertNull(row.marketplace().removed());
  }

  @Test
  void theBlockOnTheInstalledVersionIsWhatTheRowCarries() {
    var blockedAt = Instant.parse("2026-02-02T09:00:00Z");
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(
        List.of(installed(7L, "jira", IntegrationGroupEnum.BTS, "1.4.1", null)));
    when(client.getCatalogue(null, null)).thenReturn(List.of(
        registryPlugin("jira", "Jira", "1.5.0", "bug-tracking", "public", "jira")));
    when(client.getVersion("jira", "1.4.1")).thenReturn(advisedVersion("jira", "1.4.1", null, true,
        blockedAt, "Signed with a revoked key"));
    // The latest version is servable; reading it instead would report no block at all.
    when(client.getVersion("jira", "1.5.0")).thenReturn(advisedVersion("jira", "1.5.0", null, false,
        null, null));

    var row = installedNamed(handler.getCatalogue(null, null), "jira");

    assertNotNull(row.marketplace().blocked());
    assertEquals("1.4.1", row.marketplace().blocked().version());
    assertEquals(blockedAt, row.marketplace().blocked().blockedAt());
    assertEquals("Signed with a revoked key", row.marketplace().blocked().reason());
    assertNull(row.marketplace().advisory());
  }

  @Test
  void anUnblockedVersionCarriesNoBlockEvenWhenTheRegistrySentTheOtherTwoFields() {
    // blockedAt and blockReason merely describe; only 'blocked' decides.
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(
        List.of(installed(7L, "jira", IntegrationGroupEnum.BTS, "1.4.1", null)));
    when(client.getCatalogue(null, null)).thenReturn(List.of(
        registryPlugin("jira", "Jira", "1.4.1", "bug-tracking", "public", "jira")));
    when(client.getVersion("jira", "1.4.1")).thenReturn(advisedVersion("jira", "1.4.1", null, false,
        Instant.parse("2026-02-02T09:00:00Z"), "lifted"));

    assertNull(installedNamed(handler.getCatalogue(null, null), "jira")
        .marketplace().blocked());
  }

  @Test
  void aPluginTheRegistryRemovedIsSaidToBeRemovedRatherThanLeftBlank() {
    // Removal is how a plugin leaves the catalogue, so the id no longer matches anything. Leaving
    // the row blank would make it indistinguishable from an offline registry, and the user must be
    // told the difference: this one keeps running here but can never be updated again.
    var removedAt = Instant.parse("2026-01-05T12:00:00Z");
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(
        List.of(installed(9L, "jira", IntegrationGroupEnum.BTS, "1.4.1", "plugin-bts-jira")));
    when(client.getCatalogue(null, null)).thenReturn(List.of(
        registryPlugin("slack", "Slack", "2.0.0", "notifications", "public", "slack")));
    when(client.getPlugin("plugin-bts-jira")).thenThrow(new PluginRemovedException(
        "plugin-bts-jira", "Vendor withdrew it", removedAt, "operator@rp.io"));

    var row = installedNamed(handler.getCatalogue(null, null), "jira");

    assertNotNull(row.marketplace());
    assertEquals("plugin-bts-jira", row.marketplace().pluginId());
    assertNotNull(row.marketplace().removed());
    assertEquals(removedAt, row.marketplace().removed().removed());
    assertEquals("Vendor withdrew it", row.marketplace().removed().removalReason());
    assertEquals("operator@rp.io", row.marketplace().removed().removedBy());
  }

  @Test
  void aPluginMissingFromTheCatalogueForAnyOtherReasonIsStillLeftBlank() {
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(
        List.of(installed(9L, "jira", IntegrationGroupEnum.BTS, "1.4.1", "still-there")));
    when(client.getCatalogue(null, null)).thenReturn(List.of(
        registryPlugin("slack", "Slack", "2.0.0", "notifications", "public", "slack")));
    when(client.getPlugin("still-there")).thenReturn(
        new MarketplacePluginDetail("still-there", "Jira", "1.4.1", null, null, null,
            "bug-tracking", null, null, "public", null, "official", "1.4.1", "jira"));

    assertNull(installedNamed(handler.getCatalogue(null, null), "jira").marketplace());
  }

  @Test
  void aPluginThatIsOnlyOnOfferCostsNoVersionCall() {
    // The registry publishes hundreds of plugins and this data lives on version detail; asking per
    // listed plugin would turn one page view into one registry request per catalogue entry.
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(List.of());
    when(client.getCatalogue(null, null)).thenReturn(List.of(
        registryPlugin("slack", "Slack", "2.0.0", "notifications", "public", "slack"),
        registryPlugin("teams", "Teams", "3.0.0", "notifications", "public", "teams")));

    assertEquals(2, handler.getCatalogue(null, null).available().size());
    verify(client, never()).getVersion(anyString(), anyString());
    verify(client, never()).getPlugin(anyString());
  }

  @Test
  void anInstalledVersionIsAskedForOnceAcrossPageViews() {
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(
        List.of(installed(7L, "jira", IntegrationGroupEnum.BTS, "1.4.1", null)));
    when(client.getCatalogue(null, null)).thenReturn(List.of(
        registryPlugin("jira", "Jira", "1.4.1", "bug-tracking", "public", "jira")));
    when(client.getVersion("jira", "1.4.1")).thenReturn(advisedVersion("jira", "1.4.1", null, false,
        null, null));

    handler.getCatalogue(null, null);
    handler.getCatalogue(null, null);

    verify(client, times(1)).getVersion("jira", "1.4.1");
  }
}
