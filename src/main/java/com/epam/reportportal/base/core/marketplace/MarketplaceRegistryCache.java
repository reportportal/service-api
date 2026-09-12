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

import com.epam.reportportal.base.core.marketplace.exception.MarketplaceException;
import com.epam.reportportal.base.core.marketplace.exception.PluginRemovedException;
import com.epam.reportportal.base.core.marketplace.exception.RegistryNotFoundException;
import com.epam.reportportal.base.core.marketplace.exception.RegistryUnreachableException;
import com.epam.reportportal.base.model.marketplace.MarketplacePlugin;
import com.epam.reportportal.base.model.marketplace.MarketplacePluginDetail;
import com.epam.reportportal.base.model.marketplace.MarketplaceVersionDetail;
import com.epam.reportportal.base.model.marketplace.MarketplaceVersionSummary;
import com.epam.reportportal.base.model.marketplace.PluginTombstoneBody;
import com.google.common.base.Ticker;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The one short-lived read cache in front of the registry, shared by every page that reads it.
 *
 * <p>Caching follows {@code Pf4jPluginManager}: Guava, short lived. Successes and failures are
 * held apart because they deserve different lifetimes — a stale catalogue is cheap, a stale "the
 * registry is down" is not. Failures are remembered as well as successes: a registry that serves
 * one route but not another would otherwise be asked again on every page view.
 *
 * <p>It is one bean rather than one per handler on purpose. The catalogue page and a plugin's page
 * ask the registry the same questions seconds apart, and two caches would mean the second page
 * paying the network cost the first already paid.
 */
@Component
public class MarketplaceRegistryCache {

  private static final Logger LOGGER = LoggerFactory.getLogger(MarketplaceRegistryCache.class);

  private final MarketplaceClient client;

  private final Cache<CatalogueKey, List<MarketplacePlugin>> catalogueCache;
  private final Cache<CatalogueKey, Boolean> catalogueFailureCache;
  /** One entry, keyed by host: "we cannot reach the registry" is not a fact about a filter. */
  private final Cache<String, Boolean> hostDownCache;
  private final Cache<String, MarketplaceVersionDetail> versionCache;
  private final Cache<String, RegistryPlugin> pluginCache;
  private final Cache<String, List<MarketplaceVersionSummary>> versionListCache;
  private final Cache<String, String> changelogCache;
  /** Every per-request failure, keyed by the same string as the answer it stands in for. */
  private final Cache<String, Boolean> failureCache;

  /**
   * Creates the cache.
   *
   * @param client       registry client
   * @param catalogueTtl how long a registry catalogue is reused
   * @param offlineTtl   how long a registry failure is remembered
   * @param detailTtl    how long a plugin, version or changelog answer is reused
   */
  @Autowired
  public MarketplaceRegistryCache(MarketplaceClient client,
      @Value("${marketplace.cache.catalogue-ttl:PT60S}") Duration catalogueTtl,
      @Value("${marketplace.cache.offline-ttl:PT30S}") Duration offlineTtl,
      @Value("${marketplace.cache.version-ttl:PT5M}") Duration detailTtl) {
    this(client, catalogueTtl, offlineTtl, detailTtl, Ticker.systemTicker());
  }

  /**
   * As above, with the clock the caches age against — a test can cross a TTL without sleeping.
   */
  public MarketplaceRegistryCache(MarketplaceClient client, Duration catalogueTtl,
      Duration offlineTtl, Duration detailTtl, Ticker ticker) {
    this.client = client;
    this.catalogueCache = expiring(catalogueTtl, ticker);
    this.catalogueFailureCache = expiring(offlineTtl, ticker);
    this.hostDownCache = expiring(offlineTtl, ticker);
    this.versionCache = expiring(detailTtl, ticker);
    this.pluginCache = expiring(detailTtl, ticker);
    this.versionListCache = expiring(detailTtl, ticker);
    this.changelogCache = expiring(detailTtl, ticker);
    this.failureCache = expiring(offlineTtl, ticker);
  }

  private static <K, V> Cache<K, V> expiring(Duration ttl, Ticker ticker) {
    return CacheBuilder.newBuilder()
        .expireAfterWrite(ttl.toMillis(), TimeUnit.MILLISECONDS)
        .ticker(ticker)
        .maximumSize(256)
        .build();
  }

  /**
   * What GET /api/v1/plugins/{pluginId} answered. Exactly one of the three holds: the plugin's
   * detail, the registry's tombstone for it, or the registry not knowing it at all. All three are
   * answers, not failures, which is why all three are cached — only a registry that could not be
   * asked is absent from here.
   *
   * @param detail    plugin detail, null unless the plugin is published
   * @param tombstone removal record, null unless the plugin was removed
   * @param notFound  the registry has no such plugin
   */
  public record RegistryPlugin(MarketplacePluginDetail detail, PluginTombstoneBody tombstone,
                               boolean notFound) {

    public boolean removed() {
      return tombstone != null;
    }
  }

  public String registryHost() {
    return client.registryHost();
  }

  /**
   * Whether the host itself is known to be down. Callers check this before deciding they are
   * offline, because that is a different thing from one route refusing one request.
   */
  public boolean registryUnreachable() {
    return hostDownCache.getIfPresent(client.registryHost()) != null;
  }

  /**
   * The registry's catalogue for these filters, or null when it could not be had. Every registry
   * failure is offline here: a page that cannot be told what the registry thinks leaves the user
   * in the same position whichever way the call failed.
   *
   * <p>A host known to be down short-circuits even a catalogue still inside its own TTL. Serving
   * that page would have to claim the registry is ONLINE, and the whole point of the status is
   * that it is true.
   */
  public List<MarketplacePlugin> catalogue(String q, String category) {
    var key = new CatalogueKey(StringUtils.trimToNull(q), StringUtils.trimToNull(category));
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

  /**
   * The plugin's registry detail, or its tombstone, or null when neither could be had.
   */
  public RegistryPlugin plugin(String pluginId) {
    return read(pluginCache, "plugin:" + pluginId, () -> {
      try {
        var detail = client.getPlugin(pluginId);
        return detail == null ? null : new RegistryPlugin(detail, null, false);
      } catch (PluginRemovedException e) {
        // A tombstone is what the registry knows about this plugin, so it is cached as an answer.
        return new RegistryPlugin(null,
            new PluginTombstoneBody(e.getRemovedAt(), e.getRemovalReason(), e.getRemovedBy()),
            false);
      } catch (RegistryNotFoundException e) {
        // So is "no such plugin": the registry answered, and the answer will not change in a
        // minute. Caching it keeps a wrong id in a bookmark from costing a request per page view.
        return new RegistryPlugin(null, null, true);
      }
    }, "detail of marketplace plugin '" + pluginId + "'");
  }

  /**
   * The plugin's version history, or null when it could not be had.
   */
  public List<MarketplaceVersionSummary> versions(String pluginId) {
    return read(versionListCache, "versions:" + pluginId, () -> client.listVersions(pluginId),
        "versions of marketplace plugin '" + pluginId + "'");
  }

  /**
   * One version's detail, or null when it could not be had.
   */
  public MarketplaceVersionDetail versionDetail(String pluginId, String version) {
    return read(versionCache, "version:" + pluginId + ":" + version,
        () -> client.getVersion(pluginId, version),
        "version '" + version + "' of marketplace plugin '" + pluginId + "'");
  }

  /**
   * The changelog document the registry published, or null when it could not be had. The URL is
   * the registry's own, so it is fetched on the registry client — a changelog that stalls must be
   * bounded by the same deadline every other registry read is.
   */
  public String changelog(String url) {
    if (StringUtils.isBlank(url)) {
      return null;
    }
    return read(changelogCache, "changelog:" + url, () -> client.getDocument(url),
        "changelog at '" + url + "'");
  }

  /**
   * One cached registry read. A null answer is a failure, not a value: Guava cannot hold null, and
   * a registry that says nothing where a document was promised is a registry we should stop
   * asking for the length of the offline TTL.
   */
  private <T> T read(Cache<String, T> cache, String key, Callable<T> call, String what) {
    if (registryUnreachable() || failureCache.getIfPresent(key) != null) {
      return null;
    }
    var cached = cache.getIfPresent(key);
    if (cached != null) {
      return cached;
    }
    T value;
    try {
      value = call.call();
    } catch (MarketplaceException e) {
      LOGGER.warn("Could not read the {}: {}", what, e.getMessage());
      if (!rememberIfHostIsDown(e)) {
        failureCache.put(key, Boolean.TRUE);
      }
      return null;
    } catch (Exception e) {
      throw new IllegalStateException("Unexpected failure reading the " + what, e);
    }
    if (value == null) {
      failureCache.put(key, Boolean.TRUE);
      return null;
    }
    cache.put(key, value);
    return value;
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

  private record CatalogueKey(String q, String category) {

  }
}
