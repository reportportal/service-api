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

package com.epam.reportportal.base.model.marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.reportportal.base.core.configs.JacksonConfiguration;
import com.epam.reportportal.base.model.marketplace.catalogue.AvailablePluginResource;
import com.epam.reportportal.base.model.marketplace.catalogue.InstalledPluginResource;
import com.epam.reportportal.base.model.marketplace.catalogue.MarketplaceAdvisoryResource;
import com.epam.reportportal.base.model.marketplace.catalogue.MarketplaceBlockedResource;
import com.epam.reportportal.base.model.marketplace.catalogue.MarketplaceCatalogueResource;
import com.epam.reportportal.base.model.marketplace.catalogue.MarketplaceEntryResource;
import com.epam.reportportal.base.model.marketplace.catalogue.MarketplaceRemovedResource;
import com.epam.reportportal.base.model.marketplace.catalogue.RegistryStatus;
import com.epam.reportportal.base.model.marketplace.catalogue.RegistryStatusResource;
import com.epam.reportportal.base.model.marketplace.catalogue.UpdateAvailableResource;
import com.epam.reportportal.base.model.marketplace.detail.MarketplaceChangelogResource;
import com.epam.reportportal.base.model.marketplace.detail.MarketplacePluginDetailResource;
import com.epam.reportportal.base.model.marketplace.detail.MarketplaceVersionResource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

/**
 * The names service-api actually puts on the wire for the plugins page, pinned.
 *
 * <p>The UI destructures these responses by field name; nothing else does. Renaming or dropping
 * one compiles, passes every other test, and breaks a badge or a block silently, because the UI's
 * own tests are fed hand-made fixtures. So the names are checked in here, and the real records are
 * serialised with the real {@link JacksonConfiguration} mapper to produce them — deriving the
 * expectation from the record definition would only prove the record equals itself.
 *
 * <p>When this fails: if the UI changed with you, update the list in the same commit; if it did
 * not, the change is a break and the field goes back.
 */
class MarketplaceWireContractTest {

  private final ObjectMapper objectMapper = new JacksonConfiguration().objectMapper();

  /**
   * GET /v1/plugins. Nested objects are listed by path, because the UI reads them by path too.
   */
  private static final List<String> CATALOGUE_FIELDS = List.of(
      "available[].access",
      "available[].description",
      "available[].groupType",
      "available[].id",
      "available[].latestVersion",
      "available[].locked",
      "available[].name",
      "available[].tier",
      "installed[].enabled",
      "installed[].groupType",
      "installed[].integrationTypeId",
      "installed[].marketplace.access",
      "installed[].marketplace.advisory.attachedAt",
      "installed[].marketplace.advisory.severity",
      "installed[].marketplace.advisory.text",
      "installed[].marketplace.blocked.blockedAt",
      "installed[].marketplace.blocked.reason",
      "installed[].marketplace.blocked.version",
      "installed[].marketplace.latestVersion",
      "installed[].marketplace.locked",
      "installed[].marketplace.pluginId",
      "installed[].marketplace.removed.removalReason",
      "installed[].marketplace.removed.removed",
      "installed[].marketplace.removed.removedBy",
      "installed[].marketplace.tier",
      "installed[].marketplace.updateAvailable.version",
      "installed[].name",
      "installed[].version",
      "registry.host",
      "registry.status"
  );

  /** GET /v1/plugins/{registryId}. */
  private static final List<String> PLUGIN_DETAIL_FIELDS = List.of(
      "access",
      "advisory.attachedAt",
      "advisory.severity",
      "advisory.text",
      "blocked.blockedAt",
      "blocked.reason",
      "blocked.version",
      "changelog.lines[]",
      "changelog.version",
      "description",
      "id",
      "latestVersion",
      "locked",
      "name",
      "removed.removalReason",
      "removed.removed",
      "removed.removedBy",
      "screenshots[]",
      "tier",
      "versions[].blocked",
      "versions[].publishedAt",
      "versions[].version"
  );

  private static final Instant WHEN = Instant.parse("2026-03-12T10:15:30Z");

  private static MarketplaceCatalogueResource catalogue() {
    var entry = new MarketplaceEntryResource("plugin-bts-jira", "premium", "official", "1.6.0",
        new UpdateAvailableResource("1.6.0"),
        new MarketplaceAdvisoryResource("high", "Leaks the API key into the log", WHEN),
        new MarketplaceBlockedResource("1.5.2", WHEN, "Signed with a revoked key"),
        new MarketplaceRemovedResource(WHEN, "Vendor withdrew it", "operator@rp.io"),
        true);
    return new MarketplaceCatalogueResource(
        new RegistryStatusResource(RegistryStatus.ONLINE, "marketplace.reportportal.io"),
        List.of(new InstalledPluginResource(7L, "jira", "1.5.2", true, "BTS", entry)),
        List.of(new AvailablePluginResource("plugin-notify-slack", "Slack", "2.0.0", "Notifier",
            "NOTIFICATION", "public", "official", false)));
  }

  private static MarketplacePluginDetailResource pluginDetail() {
    return new MarketplacePluginDetailResource("plugin-bts-jira", "Jira", "Tracks issues", "1.6.0",
        "premium", "official",
        List.of(new MarketplaceVersionResource("1.5.2", WHEN, true)),
        new MarketplaceChangelogResource("1.6.0", List.of("Fixed a crash on an empty summary")),
        List.of("https://cdn.rp.io/jira/1.png"),
        new MarketplaceAdvisoryResource("high", "Leaks the API key into the log", WHEN),
        new MarketplaceBlockedResource("1.6.0", WHEN, "Signed with a revoked key"),
        new MarketplaceRemovedResource(WHEN, "Vendor withdrew it", "operator@rp.io"),
        true);
  }

  @Test
  void theCatalogueEmitsExactlyTheFieldNamesTheUiReads() {
    assertFields(CATALOGUE_FIELDS, catalogue(), "GET /v1/plugins");
  }

  @Test
  void thePluginDetailEmitsExactlyTheFieldNamesTheUiReads() {
    assertFields(PLUGIN_DETAIL_FIELDS, pluginDetail(), "GET /v1/plugins/{registryId}");
  }

  /**
   * Serialises the resource and compares the paths it actually emitted with the checked-in list,
   * naming what appeared and what went missing.
   */
  private void assertFields(List<String> expected, Object resource, String route) {
    var actual = new TreeSet<String>();
    collect(objectMapper.valueToTree(resource), "", actual);
    var missing = new ArrayList<>(expected);
    missing.removeAll(actual);
    var unexpected = new ArrayList<>(actual);
    unexpected.removeAll(expected);
    assertEquals(new TreeSet<>(expected), actual,
        route + " no longer emits the fields the UI reads."
            + " Dropped or renamed: " + missing + ". Not in the contract: " + unexpected
            + ". Update service-ui in the same commit, or put the field back.");
  }

  /**
   * Every leaf path of the serialised tree. An array contributes {@code []} once rather than one
   * path per element: the contract is about names, not about how many rows a fixture happens to
   * carry.
   */
  private static void collect(JsonNode node, String path, TreeSet<String> into) {
    if (node.isObject()) {
      node.properties().forEach(field ->
          collect(field.getValue(), path.isEmpty() ? field.getKey() : path + "." + field.getKey(),
              into));
    } else if (node.isArray()) {
      if (node.isEmpty()) {
        into.add(path + "[]");
      }
      node.forEach(element -> collect(element, path + "[]", into));
    } else {
      into.add(path);
    }
  }
}
