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

import com.epam.reportportal.base.core.marketplace.MarketplaceClient;
import com.epam.reportportal.base.core.marketplace.MarketplaceLicence;
import com.epam.reportportal.base.core.marketplace.MarketplaceRegistryCache;
import com.epam.reportportal.base.core.marketplace.MarketplaceState;
import com.epam.reportportal.base.core.marketplace.PluginVersions;
import com.epam.reportportal.base.core.marketplace.ProductVersion;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationTypeRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.IntegrationGroupEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import com.epam.reportportal.base.model.marketplace.MarketplacePlugin;
import com.epam.reportportal.base.model.marketplace.catalogue.AvailablePluginResource;
import com.epam.reportportal.base.model.marketplace.catalogue.InstalledPluginResource;
import com.epam.reportportal.base.model.marketplace.catalogue.MarketplaceCatalogueResource;
import com.epam.reportportal.base.model.marketplace.catalogue.MarketplaceEntryResource;
import com.epam.reportportal.base.model.marketplace.catalogue.RegistryStatus;
import com.epam.reportportal.base.model.marketplace.catalogue.RegistryStatusResource;
import com.epam.reportportal.base.model.marketplace.catalogue.UpdateAvailableResource;
import com.google.common.base.Ticker;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Merges the registry catalogue with what is installed locally.
 *
 * <p>Every registry read goes through {@link MarketplaceRegistryCache}, which is where the TTLs
 * and the offline rules live.
 */
@Service
public class GetMarketplaceCatalogueHandlerImpl implements GetMarketplaceCatalogueHandler {

  private static final String VERSION_KEY = "version";
  /** Written by the install path in a later stage; read here whenever it is already there. */
  private static final String MARKETPLACE_PLUGIN_ID_KEY = "marketplacePluginId";
  private static final String PREMIUM = "premium";

  private final MarketplaceRegistryCache registry;
  private final IntegrationTypeRepository integrationTypeRepository;
  private final ProductVersion productVersion;
  private final MarketplaceLicence licence;

  /**
   * Creates the handler.
   *
   * @param registry                  cached registry reads
   * @param integrationTypeRepository local integration types
   * @param productVersion            running ReportPortal release
   * @param licence                   premium licence state of this instance
   */
  @Autowired
  public GetMarketplaceCatalogueHandlerImpl(MarketplaceRegistryCache registry,
      IntegrationTypeRepository integrationTypeRepository, ProductVersion productVersion,
      MarketplaceLicence licence) {
    this.registry = registry;
    this.integrationTypeRepository = integrationTypeRepository;
    this.productVersion = productVersion;
    this.licence = licence;
  }

  /**
   * As above, over a cache of its own with the clock it ages against — a test can cross a TTL
   * without sleeping.
   */
  GetMarketplaceCatalogueHandlerImpl(MarketplaceClient client,
      IntegrationTypeRepository integrationTypeRepository, ProductVersion productVersion,
      MarketplaceLicence licence, Duration catalogueTtl, Duration offlineTtl, Duration versionTtl,
      Ticker ticker) {
    this(new MarketplaceRegistryCache(client, catalogueTtl, offlineTtl, versionTtl, ticker),
        integrationTypeRepository, productVersion, licence);
  }

  @Override
  public MarketplaceCatalogueResource getCatalogue(String q, String category) {
    var key = new CatalogueKey(StringUtils.trimToNull(q), StringUtils.trimToNull(category));
    var registryPlugins = registry.catalogue(key.q(), key.category());
    // Read the DB outside any registry call: the merge needs current local state, and the HTTP
    // exchange must not sit inside a transaction holding a connection.
    var installedTypes = integrationTypeRepository.findAllByOrderByCreationDate();

    // Matching runs over everything installed, not only the rows that survive the filter, so a
    // plugin hidden by the filter can never reappear as something to install.
    Map<Long, MarketplacePlugin> matches = new HashMap<>();
    Set<String> installedRegistryIds = new HashSet<>();
    if (registryPlugins != null) {
      var byRegistryId = index(registryPlugins, MarketplacePlugin::id);
      var byPf4jId = index(registryPlugins, MarketplacePlugin::pf4jId);
      for (var type : installedTypes) {
        var match = match(type, byRegistryId, byPf4jId);
        if (match != null) {
          matches.put(type.getId(), match);
          installedRegistryIds.add(match.id());
        }
      }
    }

    var installed = installedTypes.stream()
        .filter(type -> visible(type, matches.get(type.getId()), key))
        .map(type -> toInstalled(type, matches.get(type.getId()), registryPlugins != null))
        .toList();
    var available = registryPlugins == null ? List.<AvailablePluginResource>of()
        : registryPlugins.stream()
            .filter(plugin -> !installedRegistryIds.contains(plugin.id()))
            .map(this::toAvailable)
            .toList();
    var status = registryPlugins == null ? RegistryStatus.OFFLINE : RegistryStatus.ONLINE;
    return new MarketplaceCatalogueResource(
        new RegistryStatusResource(status, registry.registryHost()), installed, available);
  }

  private static Map<String, MarketplacePlugin> index(List<MarketplacePlugin> plugins,
      java.util.function.Function<MarketplacePlugin, String> keyOf) {
    Map<String, MarketplacePlugin> index = new HashMap<>();
    for (var plugin : plugins) {
      var key = keyOf.apply(plugin);
      if (StringUtils.isNotBlank(key)) {
        index.putIfAbsent(key, plugin);
      }
    }
    return index;
  }

  /**
   * A registry id persisted at install time is the only key we will match on; otherwise the
   * entry's {@code pf4jId} against the PF4J id, byte for byte. Case-folding would merge
   * {@code github} and {@code GitHub}, which are two different plugins that can both be installed
   * at once.
   *
   * <p>A persisted id that no longer resolves does not fall back to the name. The id is a record
   * of where this plugin actually came from; the name is a guess that a different registry entry
   * happens to share. Falling back would replace strong evidence with weak, and could name that
   * other entry as this plugin's origin and offer its versions as updates. The catalogue is also
   * filtered, so an id can be missing merely because the filter excluded it — and the name of a
   * surviving entry would then match by accident. Not matching says what is true: with the id
   * gone, nothing about this plugin can be verified.
   */
  private MarketplacePlugin match(IntegrationType type, Map<String, MarketplacePlugin> byRegistryId,
      Map<String, MarketplacePlugin> byPf4jId) {
    var persisted = detail(type, MARKETPLACE_PLUGIN_ID_KEY);
    if (persisted != null) {
      return byRegistryId.get(persisted);
    }
    return type.getName() == null ? null : byPf4jId.get(type.getName());
  }

  private boolean visible(IntegrationType type, MarketplacePlugin match, CatalogueKey key) {
    return matchesQuery(type, match, key.q()) && matchesCategory(type, key.category());
  }

  private static boolean matchesQuery(IntegrationType type, MarketplacePlugin match, String q) {
    if (q == null) {
      return true;
    }
    var needle = q.toLowerCase(Locale.ROOT);
    return contains(type.getName(), needle) || (match != null && contains(match.name(), needle));
  }

  private static boolean contains(String value, String lowercaseNeedle) {
    return value != null && value.toLowerCase(Locale.ROOT).contains(lowercaseNeedle);
  }

  private static boolean matchesCategory(IntegrationType type, String category) {
    if (category == null) {
      return true;
    }
    return group(category).map(group -> group == type.getIntegrationGroup()).orElse(false);
  }

  /**
   * Registry categories to the integration groups the local rows carry. An unknown category maps
   * to nothing, so the installed list empties rather than quietly ignoring the filter.
   */
  private static Optional<IntegrationGroupEnum> group(String category) {
    return switch (StringUtils.trimToEmpty(category)) {
      case "bug-tracking" -> Optional.of(IntegrationGroupEnum.BTS);
      case "notifications" -> Optional.of(IntegrationGroupEnum.NOTIFICATION);
      case "authorization" -> Optional.of(IntegrationGroupEnum.AUTH);
      case "import" -> Optional.of(IntegrationGroupEnum.IMPORT);
      case "other" -> Optional.of(IntegrationGroupEnum.OTHER);
      default -> Optional.empty();
    };
  }

  private InstalledPluginResource toInstalled(IntegrationType type, MarketplacePlugin match,
      boolean registryOnline) {
    var version = detail(type, VERSION_KEY);
    var group = type.getIntegrationGroup() == null ? null : type.getIntegrationGroup().name();
    return new InstalledPluginResource(type.getId(), type.getName(), version, type.isEnabled(),
        group, entryFor(type, match, version, registryOnline));
  }

  /**
   * The registry block of an installed row.
   *
   * <p>An unmatched row is normally left with none. The one exception is a plugin that carries a
   * registry id from its own install and is no longer in the catalogue: that is what removal looks
   * like from here, and the page has to be able to say "removed from the marketplace, still
   * running here" rather than show the same blank row an offline registry produces. Only a
   * persisted id is probed — a name is a guess, and probing on a guess would ask the registry
   * about a plugin this instance never got from it.
   */
  private MarketplaceEntryResource entryFor(IntegrationType type, MarketplacePlugin match,
      String installedVersion, boolean registryOnline) {
    if (match != null) {
      return toEntry(match, installedVersion);
    }
    var persisted = detail(type, MARKETPLACE_PLUGIN_ID_KEY);
    if (!registryOnline || persisted == null) {
      return null;
    }
    var plugin = registry.plugin(persisted);
    if (plugin == null || !plugin.removed()) {
      return null;
    }
    return new MarketplaceEntryResource(persisted, null, null, null, null, null, null,
        MarketplaceState.removed(plugin.tombstone()), false);
  }

  /**
   * The registry's view of a matched plugin.
   *
   * <p>Advisory and block state are read for the version installed here, never for the latest: this
   * block is what the row's badges are drawn from, and a badge on an installed row is a statement
   * about the code this instance is running. Only installed plugins are looked up. The catalogue
   * lists everything the registry publishes and this data lives on version detail, so populating
   * it per listed plugin would turn one page view into one registry request per catalogue entry —
   * the cache exists to stop exactly that. The bound here is the number of plugins installed on
   * this instance, and every answer is cached.
   */
  private MarketplaceEntryResource toEntry(MarketplacePlugin plugin, String installedVersion) {
    var installed = StringUtils.isBlank(installedVersion) ? null
        : registry.versionDetail(plugin.id(), installedVersion);
    return new MarketplaceEntryResource(plugin.id(), plugin.access(), plugin.tier(),
        plugin.latestVersion(), updateFor(plugin, installedVersion),
        MarketplaceState.advisory(installed), MarketplaceState.blocked(installed), null,
        locked(plugin.access()));
  }

  /**
   * A registry plugin offered for install. {@code contactUrl} travels with it because a locked
   * premium row offers no install, only an enquiry, and without the URL that action is drawn and
   * does nothing.
   */
  private AvailablePluginResource toAvailable(MarketplacePlugin plugin) {
    return new AvailablePluginResource(plugin.id(), plugin.name(), plugin.latestVersion(),
        plugin.description(), plugin.contactUrl(),
        group(plugin.category()).map(Enum::name).orElse(null),
        plugin.access(), plugin.tier(), locked(plugin.access()));
  }

  private boolean locked(String access) {
    return PREMIUM.equalsIgnoreCase(access) && !licence.isConfigured();
  }

  /**
   * An update is offered only when the latest version is newer than what is installed, runs on
   * this release, and is not blocked. Only the latest version is considered: the versions list
   * carries no compatibility, so walking down it costs one request per version.
   */
  private UpdateAvailableResource updateFor(MarketplacePlugin plugin, String installedVersion) {
    var latest = plugin.latestVersion();
    if (StringUtils.isBlank(latest) || StringUtils.isBlank(installedVersion)
        || PluginVersions.compare(latest, installedVersion) <= 0) {
      return null;
    }
    var detail = registry.versionDetail(plugin.id(), latest);
    if (detail == null || detail.blocked()) {
      return null;
    }
    var range = detail.compatibility() == null ? null : detail.compatibility().reportportal();
    return productVersion.satisfies(range) ? new UpdateAvailableResource(latest) : null;
  }

  private static String detail(IntegrationType type, String attribute) {
    if (type.getDetails() == null || type.getDetails().getDetails() == null) {
      return null;
    }
    var value = type.getDetails().getDetails().get(attribute);
    return value == null ? null : StringUtils.trimToNull(String.valueOf(value));
  }

  private record CatalogueKey(String q, String category) {

  }
}
