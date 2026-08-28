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
import com.epam.reportportal.base.core.marketplace.PluginVersions;
import com.epam.reportportal.base.core.marketplace.ProductVersion;
import com.epam.reportportal.base.core.marketplace.exception.MarketplaceException;
import com.epam.reportportal.base.core.marketplace.exception.RegistryUnreachableException;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationTypeRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.IntegrationGroupEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import com.epam.reportportal.base.model.marketplace.MarketplacePlugin;
import com.epam.reportportal.base.model.marketplace.MarketplaceVersionDetail;
import com.epam.reportportal.base.model.marketplace.catalogue.AvailablePluginResource;
import com.epam.reportportal.base.model.marketplace.catalogue.InstalledPluginResource;
import com.epam.reportportal.base.model.marketplace.catalogue.MarketplaceCatalogueResource;
import com.epam.reportportal.base.model.marketplace.catalogue.MarketplaceEntryResource;
import com.epam.reportportal.base.model.marketplace.catalogue.RegistryStatus;
import com.epam.reportportal.base.model.marketplace.catalogue.RegistryStatusResource;
import com.epam.reportportal.base.model.marketplace.catalogue.UpdateAvailableResource;
import com.google.common.base.Ticker;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Merges the registry catalogue with what is installed locally.
 *
 * <p>Caching follows {@code Pf4jPluginManager}: Guava, short lived. Successes and failures are
 * held apart because they deserve different lifetimes — a stale catalogue is cheap, a stale "the
 * registry is down" is not.
 */
@Service
public class GetMarketplaceCatalogueHandlerImpl implements GetMarketplaceCatalogueHandler {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(GetMarketplaceCatalogueHandlerImpl.class);

  private static final String VERSION_KEY = "version";
  /** Written by the install path in a later stage; read here whenever it is already there. */
  private static final String MARKETPLACE_PLUGIN_ID_KEY = "marketplacePluginId";
  private static final String PREMIUM = "premium";

  private final MarketplaceClient client;
  private final IntegrationTypeRepository integrationTypeRepository;
  private final ProductVersion productVersion;
  private final MarketplaceLicence licence;

  private final Cache<CatalogueKey, List<MarketplacePlugin>> catalogueCache;
  /** One entry, keyed by host: "we cannot reach the registry" is not a fact about a filter. */
  private final Cache<String, Boolean> hostDownCache;
  private final Cache<CatalogueKey, Boolean> catalogueFailureCache;
  private final Cache<String, MarketplaceVersionDetail> versionCache;
  private final Cache<String, Boolean> versionFailureCache;

  /**
   * Creates the handler.
   *
   * @param client                    registry client
   * @param integrationTypeRepository local integration types
   * @param productVersion            running ReportPortal release
   * @param licence                   premium licence state of this instance
   * @param catalogueTtl              how long a registry catalogue is reused
   * @param offlineTtl                how long a registry failure is remembered
   * @param versionTtl                how long a version detail is reused
   */
  @Autowired
  public GetMarketplaceCatalogueHandlerImpl(MarketplaceClient client,
      IntegrationTypeRepository integrationTypeRepository, ProductVersion productVersion,
      MarketplaceLicence licence,
      @Value("${marketplace.cache.catalogue-ttl:PT60S}") Duration catalogueTtl,
      @Value("${marketplace.cache.offline-ttl:PT30S}") Duration offlineTtl,
      @Value("${marketplace.cache.version-ttl:PT5M}") Duration versionTtl) {
    this(client, integrationTypeRepository, productVersion, licence, catalogueTtl, offlineTtl,
        versionTtl, Ticker.systemTicker());
  }

  /**
   * As above, with the clock the caches age against — a test can cross a TTL without sleeping.
   */
  GetMarketplaceCatalogueHandlerImpl(MarketplaceClient client,
      IntegrationTypeRepository integrationTypeRepository, ProductVersion productVersion,
      MarketplaceLicence licence, Duration catalogueTtl, Duration offlineTtl, Duration versionTtl,
      Ticker ticker) {
    this.client = client;
    this.integrationTypeRepository = integrationTypeRepository;
    this.productVersion = productVersion;
    this.licence = licence;
    this.catalogueCache = expiring(catalogueTtl, ticker);
    this.hostDownCache = expiring(offlineTtl, ticker);
    this.catalogueFailureCache = expiring(offlineTtl, ticker);
    this.versionCache = expiring(versionTtl, ticker);
    this.versionFailureCache = expiring(offlineTtl, ticker);
  }

  private static <K, V> Cache<K, V> expiring(Duration ttl, Ticker ticker) {
    return CacheBuilder.newBuilder()
        .expireAfterWrite(ttl.toMillis(), TimeUnit.MILLISECONDS)
        .ticker(ticker)
        .maximumSize(256)
        .build();
  }

  @Override
  public MarketplaceCatalogueResource getCatalogue(String q, String category) {
    var key = new CatalogueKey(StringUtils.trimToNull(q), StringUtils.trimToNull(category));
    var registryPlugins = registryCatalogue(key);
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
        .map(type -> toInstalled(type, matches.get(type.getId())))
        .toList();
    var available = registryPlugins == null ? List.<AvailablePluginResource>of()
        : registryPlugins.stream()
            .filter(plugin -> !installedRegistryIds.contains(plugin.id()))
            .map(this::toAvailable)
            .toList();
    var status = registryPlugins == null ? RegistryStatus.OFFLINE : RegistryStatus.ONLINE;
    return new MarketplaceCatalogueResource(
        new RegistryStatusResource(status, client.registryHost()), installed, available);
  }

  /**
   * The registry's answer for these filters, or null when it could not be had. Every registry
   * failure is offline here: a page that cannot be told what the registry thinks leaves the user
   * in the same position whichever way the call failed.
   *
   * <p>A host known to be down short-circuits even a catalogue still inside its own TTL. Serving
   * that page would have to claim the registry is ONLINE, and the whole point of the status is
   * that it is true.
   */
  private List<MarketplacePlugin> registryCatalogue(CatalogueKey key) {
    if (registryUnreachable() || catalogueFailureCache.getIfPresent(key) != null) {
      return null;
    }
    var cached = catalogueCache.getIfPresent(key);
    if (cached != null) {
      return cached;
    }
    try {
      var plugins = client.getCatalogue(key.category(), key.q());
      catalogueCache.put(key, plugins);
      return plugins;
    } catch (MarketplaceException e) {
      LOGGER.warn("Marketplace registry at '{}' could not be read; serving the plugins page"
          + " offline: {}", client.registryHost(), e.getMessage());
      if (!rememberIfHostIsDown(e)) {
        catalogueFailureCache.put(key, Boolean.TRUE);
      }
      return null;
    }
  }

  private boolean registryUnreachable() {
    return hostDownCache.getIfPresent(client.registryHost()) != null;
  }

  /**
   * Records a failure that is about the host rather than about one request, and says whether it
   * was one.
   *
   * <p>An unreachable host is the expensive case: every probe of it costs the whole request
   * deadline, and the answer is the same for every filter string a user types and for every
   * plugin whose version detail we wanted. A registry that answered at all is up, and its refusal
   * is only evidence about the request that drew it.
   */
  private boolean rememberIfHostIsDown(MarketplaceException e) {
    if (e instanceof RegistryUnreachableException) {
      hostDownCache.put(client.registryHost(), Boolean.TRUE);
      return true;
    }
    return false;
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

  private InstalledPluginResource toInstalled(IntegrationType type, MarketplacePlugin match) {
    var version = detail(type, VERSION_KEY);
    var group = type.getIntegrationGroup() == null ? null : type.getIntegrationGroup().name();
    return new InstalledPluginResource(type.getId(), type.getName(), version, type.isEnabled(),
        group, match == null ? null : toEntry(match, version));
  }

  private MarketplaceEntryResource toEntry(MarketplacePlugin plugin, String installedVersion) {
    return new MarketplaceEntryResource(plugin.id(), plugin.access(), plugin.tier(),
        plugin.latestVersion(), updateFor(plugin, installedVersion), locked(plugin.access()));
  }

  private AvailablePluginResource toAvailable(MarketplacePlugin plugin) {
    return new AvailablePluginResource(plugin.id(), plugin.name(), plugin.latestVersion(),
        plugin.description(), group(plugin.category()).map(Enum::name).orElse(null),
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
    var detail = versionDetail(plugin.id(), latest);
    if (detail == null || detail.blocked()) {
      return null;
    }
    var range = detail.compatibility() == null ? null : detail.compatibility().reportportal();
    return productVersion.satisfies(range) ? new UpdateAvailableResource(latest) : null;
  }

  /**
   * The version detail, or null when it could not be had. Failures are remembered as well as
   * successes: a registry that serves the catalogue but not the version route would otherwise be
   * asked again for every plugin with a pending update, on every page view.
   */
  private MarketplaceVersionDetail versionDetail(String pluginId, String version) {
    var key = pluginId + ":" + version;
    if (registryUnreachable() || versionFailureCache.getIfPresent(key) != null) {
      return null;
    }
    var cached = versionCache.getIfPresent(key);
    if (cached != null) {
      return cached;
    }
    try {
      var detail = client.getVersion(pluginId, version);
      if (detail == null) {
        versionFailureCache.put(key, Boolean.TRUE);
      } else {
        versionCache.put(key, detail);
      }
      return detail;
    } catch (MarketplaceException e) {
      LOGGER.warn("Could not read version '{}' of marketplace plugin '{}', offering no update: {}",
          version, pluginId, e.getMessage());
      if (!rememberIfHostIsDown(e)) {
        versionFailureCache.put(key, Boolean.TRUE);
      }
      return null;
    }
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
