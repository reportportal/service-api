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
import com.epam.reportportal.base.core.marketplace.exception.RegistryResponseException;
import com.epam.reportportal.base.core.marketplace.exception.RegistryUnreachableException;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationTypeRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.IntegrationGroupEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationTypeDetails;
import com.epam.reportportal.base.model.marketplace.MarketplaceCompatibility;
import com.epam.reportportal.base.model.marketplace.MarketplacePlugin;
import com.epam.reportportal.base.model.marketplace.MarketplaceVersionDetail;
import com.epam.reportportal.base.model.marketplace.catalogue.InstalledPluginResource;
import com.epam.reportportal.base.model.marketplace.catalogue.MarketplaceCatalogueResource;
import com.epam.reportportal.base.model.marketplace.catalogue.RegistryStatus;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The merge matrix, the derivations and the offline degradation of the catalogue endpoint.
 */
class GetMarketplaceCatalogueHandlerTest {

  private MarketplaceClient client;
  private IntegrationTypeRepository integrationTypeRepository;
  private MarketplaceLicence licence;
  private GetMarketplaceCatalogueHandlerImpl handler;

  @BeforeEach
  void setUp() {
    client = mock(MarketplaceClient.class);
    integrationTypeRepository = mock(IntegrationTypeRepository.class);
    licence = mock(MarketplaceLicence.class);
    when(client.registryHost()).thenReturn("marketplace.reportportal.io");
    when(licence.isConfigured()).thenReturn(false);
    handler = newHandler(new ProductVersion("25.2"));
  }

  private GetMarketplaceCatalogueHandlerImpl newHandler(ProductVersion productVersion) {
    return new GetMarketplaceCatalogueHandlerImpl(client, integrationTypeRepository, productVersion,
        licence, Duration.ofSeconds(60), Duration.ofSeconds(30), Duration.ofMinutes(5));
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
        "official", pf4jId);
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
    // Nothing newer to consider, so the version detail is never fetched.
    verify(client, never()).getVersion(anyString(), anyString());
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
  void offlineFailureIsCachedPerFilterCombinationNotGlobally() {
    when(integrationTypeRepository.findAllByOrderByCreationDate()).thenReturn(List.of());
    when(client.getCatalogue(null, "jira")).thenThrow(new RegistryUnreachableException(
        "marketplace.reportportal.io", new SocketTimeoutException("Read timed out")));
    when(client.getCatalogue(null, "slack")).thenReturn(List.of(
        registryPlugin("slack", "Slack", "2.0.0", "notifications", "public", "slack")));

    assertEquals(RegistryStatus.OFFLINE, handler.getCatalogue("jira", null).registry().status());
    assertEquals(RegistryStatus.ONLINE, handler.getCatalogue("slack", null).registry().status());
  }
}
