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

import com.epam.reportportal.base.core.marketplace.MarketplaceLicence;
import com.epam.reportportal.base.core.marketplace.MarketplaceRegistryCache;
import com.epam.reportportal.base.core.marketplace.MarketplaceState;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.marketplace.MarketplaceVersionDetail;
import com.epam.reportportal.base.model.marketplace.MarketplaceVersionSummary;
import com.epam.reportportal.base.model.marketplace.catalogue.RegistryStatus;
import com.epam.reportportal.base.model.marketplace.catalogue.RegistryStatusResource;
import com.epam.reportportal.base.model.marketplace.detail.MarketplaceChangelogResource;
import com.epam.reportportal.base.model.marketplace.detail.MarketplacePluginDetailResource;
import com.epam.reportportal.base.model.marketplace.detail.MarketplacePluginResource;
import com.epam.reportportal.base.model.marketplace.detail.MarketplaceVersionResource;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * One plugin's registry detail, assembled from the plugin, versions, version-detail and changelog
 * routes over the shared {@link MarketplaceRegistryCache}.
 *
 * <p>Everything but the plugin route itself degrades quietly: a version history or a changelog
 * that could not be read leaves that block out, exactly as an empty one does, because a page that
 * explains each missing block separately tells the user nothing they can act on. The plugin route
 * is different — without it there is no page at all, so its failure is reported.
 */
@Service
public class GetMarketplacePluginDetailHandlerImpl implements GetMarketplacePluginDetailHandler {

  private static final String PREMIUM = "premium";
  /** A changelog that arrives megabytes long is a document nobody meant to publish. */
  private static final int MAX_CHANGELOG_LINES = 200;
  private static final Pattern LINE_BREAK = Pattern.compile("\\R");

  private final MarketplaceRegistryCache registry;
  private final MarketplaceLicence licence;

  public GetMarketplacePluginDetailHandlerImpl(MarketplaceRegistryCache registry,
      MarketplaceLicence licence) {
    this.registry = registry;
    this.licence = licence;
  }

  @Override
  public MarketplacePluginDetailResource getPluginDetail(String registryId) {
    var plugin = registry.plugin(registryId);
    if (plugin == null) {
      if (registry.registryUnreachable()) {
        // The host is down, which is a state of the marketplace and not of this page. The
        // catalogue answers 200/OFFLINE for it, and the UI reads one envelope for both screens,
        // so this one says the same thing rather than inventing a second way to be degraded.
        return offline();
      }
      // The registry answered and refused this one request. It is up, so OFFLINE would be a lie,
      // and nothing about the plugin was learned, so there is no page to serve either.
      throw new ReportPortalException(ErrorType.MARKETPLACE_REGISTRY_ERROR,
          "The marketplace registry at '" + registry.registryHost()
              + "' could not be asked about plugin '" + registryId + "'");
    }
    if (plugin.notFound()) {
      throw new ReportPortalException(ErrorType.MARKETPLACE_PLUGIN_NOT_FOUND, registryId);
    }
    if (plugin.removed()) {
      // Removed is a state of the page, not an error: the plugin is gone from the marketplace and
      // still running here, and answering 404 would say the opposite of both halves. The registry
      // did answer, so the envelope stays ONLINE — the tombstone is something it told us.
      return new MarketplacePluginDetailResource(online(),
          new MarketplacePluginResource(registryId, null, null, null, null, null),
          List.of(), null, List.of(), null, null, MarketplaceState.removed(plugin.tombstone()),
          false);
    }
    var detail = plugin.detail();
    var latest = detail.latestVersion();
    var latestDetail = StringUtils.isBlank(latest) ? null
        : registry.versionDetail(registryId, latest);
    return new MarketplacePluginDetailResource(
        online(),
        new MarketplacePluginResource(detail.id(), detail.name(), detail.description(), latest,
            detail.access(), detail.tier()),
        versions(registryId),
        changelog(latest, latestDetail),
        screenshots(latestDetail),
        MarketplaceState.advisory(latestDetail),
        MarketplaceState.blocked(latestDetail),
        null,
        PREMIUM.equalsIgnoreCase(detail.access()) && !licence.isConfigured());
  }

  /**
   * The answer when the registry host could not be reached: the status, the host the operator has
   * to go and look at, and nothing registry-derived at all. Not even the id is echoed back into
   * {@code plugin} — that block is the registry's manifest, and there is no manifest here.
   */
  private MarketplacePluginDetailResource offline() {
    return new MarketplacePluginDetailResource(
        new RegistryStatusResource(RegistryStatus.OFFLINE, registry.registryHost()), null,
        List.of(), null, List.of(), null, null, null, false);
  }

  private RegistryStatusResource online() {
    return new RegistryStatusResource(RegistryStatus.ONLINE, registry.registryHost());
  }

  private List<MarketplaceVersionResource> versions(String registryId) {
    var summaries = registry.versions(registryId);
    if (summaries == null) {
      return List.of();
    }
    return summaries.stream().map(GetMarketplacePluginDetailHandlerImpl::toVersion).toList();
  }

  private static MarketplaceVersionResource toVersion(MarketplaceVersionSummary summary) {
    return new MarketplaceVersionResource(summary.version(), summary.publishedAt(),
        summary.blocked());
  }

  private static List<String> screenshots(MarketplaceVersionDetail version) {
    return version == null || version.screenshotUrls() == null ? List.of()
        : version.screenshotUrls();
  }

  /**
   * The changelog of the latest version, already split into lines. The registry publishes a URL,
   * not a body, so the document is fetched — over the same client and the same deadlines as every
   * other registry read, and cached with them.
   */
  private MarketplaceChangelogResource changelog(String version, MarketplaceVersionDetail detail) {
    if (detail == null || StringUtils.isBlank(detail.changelogUrl())) {
      return null;
    }
    var document = registry.changelog(detail.changelogUrl());
    if (document == null) {
      return null;
    }
    var lines = LINE_BREAK.splitAsStream(document)
        .map(String::strip)
        .filter(StringUtils::isNotBlank)
        .limit(MAX_CHANGELOG_LINES)
        .toList();
    return lines.isEmpty() ? null : new MarketplaceChangelogResource(version, lines);
  }
}
