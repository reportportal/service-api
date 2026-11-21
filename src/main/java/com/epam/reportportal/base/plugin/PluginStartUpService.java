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

package com.epam.reportportal.base.plugin;

import static java.util.Optional.ofNullable;

import com.epam.reportportal.base.core.marketplace.MarketplaceArtifactFetcher;
import com.epam.reportportal.base.core.marketplace.MarketplaceClient;
import com.epam.reportportal.base.core.marketplace.PluginArtifacts;
import com.epam.reportportal.base.core.marketplace.PluginVersions;
import com.epam.reportportal.base.core.marketplace.ProductVersion;
import com.epam.reportportal.base.core.marketplace.exception.MarketplaceException;
import com.epam.reportportal.base.core.marketplace.handler.InstallMarketplacePluginHandler;
import com.epam.reportportal.base.core.plugin.Pf4jPluginBox;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationTypeRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import com.epam.reportportal.base.model.marketplace.MarketplaceInstallRQ;
import com.epam.reportportal.extension.common.IntegrationTypeProperties;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Loads the installed plugins and keeps the default plugin installed and current, on application
 * startup.
 *
 * <p>Runs after the application context has finished refreshing and readiness probes can pass, on
 * the single plugin-startup thread rather than the one that raised the event — nothing here delays
 * readiness, however slow the registry or the download.
 *
 * <p>The default plugin comes from one of two sources, tried in this order:
 * <ol>
 *   <li><b>The marketplace registry</b> ({@code rp.plugins.default.registry-id}, switched by
 *   {@code rp.plugins.default.auto-update}). Its latest version is installed when it is newer than
 *   what is installed, through the administrator's marketplace install path and every check that
 *   path makes: compatibility with this release, blocked versions, a SHA-256 the registry published
 *   and the download matches. A refusal is logged, never worked around.</li>
 *   <li><b>The pinned coordinates</b> ({@code rp.plugins.default.id}, {@code .version}, {@code .url},
 *   {@code .sha256}), when the registry did not install anything: the jar is fetched over HTTPS with
 *   the marketplace's bounded downloader and loaded only if its SHA-256 is the configured one. This
 *   is what puts the plugin on an instance that cannot reach the registry.</li>
 * </ol>
 * Neither source ever downgrades: an installed version equal to or newer than what a source offers
 * is left alone. Nothing here fails the startup, and every run ends with one log line naming the
 * source used and the version installed, or why nothing was.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Component
public class PluginStartUpService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PluginStartUpService.class);

  /** Written by the marketplace install path; finds a registry install filed under another name. */
  private static final String MARKETPLACE_PLUGIN_ID_KEY = "marketplacePluginId";

  private final Pf4jPluginBox pluginBox;
  private final IntegrationTypeRepository integrationTypeRepository;
  private final MarketplaceArtifactFetcher fetcher;
  private final MarketplaceClient marketplaceClient;
  private final InstallMarketplacePluginHandler marketplaceInstaller;
  private final ProductVersion productVersion;
  private final boolean defaultPluginsLoad;
  private final boolean autoUpdate;
  private final String registryId;
  private final String pluginId;
  private final String pluginVersion;
  private final String pluginUrl;
  private final String pluginSha256;
  private final String pluginsTempPath;

  /**
   * Creates the service.
   *
   * @param pluginBox                 the plugin loader
   * @param integrationTypeRepository installed integration types
   * @param fetcher                   the marketplace's bounded artifact downloader
   * @param marketplaceClient         the registry client, with its own timeouts
   * @param marketplaceInstaller      the marketplace install path
   * @param productVersion            the release this instance runs
   * @param defaultPluginsLoad        whether to install the default plugin at all
   * @param autoUpdate                whether to ask the registry for a newer version
   * @param registryId                registry id of the default plugin
   * @param pluginId                  plugin id of the default plugin
   * @param pluginVersion             pinned version
   * @param pluginUrl                 immutable HTTPS download URL of that version
   * @param pluginSha256              expected SHA-256 of the jar, hex
   * @param pluginsTempPath           staging directory for the unverified download
   */
  public PluginStartUpService(Pf4jPluginBox pluginBox,
      IntegrationTypeRepository integrationTypeRepository, MarketplaceArtifactFetcher fetcher,
      MarketplaceClient marketplaceClient, InstallMarketplacePluginHandler marketplaceInstaller,
      ProductVersion productVersion,
      @Value("${rp.plugins.default.load}") boolean defaultPluginsLoad,
      @Value("${rp.plugins.default.auto-update:true}") boolean autoUpdate,
      @Value("${rp.plugins.default.registry-id:}") String registryId,
      @Value("${rp.plugins.default.id:}") String pluginId,
      @Value("${rp.plugins.default.version:}") String pluginVersion,
      @Value("${rp.plugins.default.url:}") String pluginUrl,
      @Value("${rp.plugins.default.sha256:}") String pluginSha256,
      @Value("${rp.plugins.temp.path}") String pluginsTempPath) {
    this.pluginBox = pluginBox;
    this.integrationTypeRepository = integrationTypeRepository;
    this.fetcher = fetcher;
    this.marketplaceClient = marketplaceClient;
    this.marketplaceInstaller = marketplaceInstaller;
    this.productVersion = productVersion;
    this.defaultPluginsLoad = defaultPluginsLoad;
    this.autoUpdate = autoUpdate;
    this.registryId = StringUtils.trimToEmpty(registryId);
    this.pluginId = StringUtils.trimToEmpty(pluginId);
    this.pluginVersion = StringUtils.trimToEmpty(pluginVersion);
    this.pluginUrl = StringUtils.trimToEmpty(pluginUrl);
    this.pluginSha256 = StringUtils.trimToEmpty(pluginSha256);
    this.pluginsTempPath = pluginsTempPath;
  }

  /**
   * Loads every installed plugin, then installs or updates the default one. Never throws: an
   * exception here would only reach the async executor's handler, and the service is healthy
   * without the plugin.
   */
  @Async("pluginStartupExecutor")
  @EventListener(ApplicationReadyEvent.class)
  public void loadPlugins() {
    try {
      pluginBox.startUp();
    } catch (RuntimeException | LinkageError e) {
      LOGGER.error("Loading the installed plugins failed: {}", e.toString());
    }
    if (!defaultPluginsLoad) {
      return;
    }
    try {
      LOGGER.info("Default plugin '{}': {}", pluginId, installDefaultPlugin());
    } catch (RuntimeException | LinkageError e) {
      LOGGER.error("Default plugin '{}': nothing installed, unexpected failure: {}", pluginId,
          e.toString());
    }
  }

  /**
   * The decision. Returns the one-line account that is logged.
   */
  private String installDefaultPlugin() {
    if (!PluginArtifacts.isSafeIdentifier(pluginId)) {
      return "nothing installed: rp.plugins.default.id '" + pluginId
          + "' must be set to a plain identifier";
    }
    var before = installed();
    if (before.present() && before.version() == null) {
      return "installed without a readable version, left alone";
    }

    String registryOutcome;
    if (!autoUpdate) {
      registryOutcome = "registry not asked (rp.plugins.default.auto-update=false)";
    } else {
      var registry = fromRegistry(before);
      if (registry.done()) {
        return registry.message();
      }
      registryOutcome = registry.message();
    }

    // Read again: a refused install may have been rolled back, and another node starting at the
    // same time may have installed the plugin while this one was talking to the registry.
    var now = installed();
    if (now.present() && now.version() == null) {
      return "installed without a readable version, left alone; " + registryOutcome;
    }
    return fromPin(now) + "; " + registryOutcome;
  }

  /**
   * The registry step. {@code done} means the default plugin is where the registry says it should
   * be — just installed, or already current — and the pin is not consulted.
   */
  private Outcome fromRegistry(Installed installed) {
    if (!PluginArtifacts.isSafeIdentifier(registryId)) {
      return Outcome.notDone("registry not asked (rp.plugins.default.registry-id is not set)");
    }
    if (!productVersion.isKnown()) {
      // The install path refuses on this before downloading anything; not asking at all saves a
      // request whose answer could not be used.
      return Outcome.notDone("registry not asked (rp.product.version is not set, so"
          + " compatibility of a registry version cannot be checked)");
    }
    var host = marketplaceClient.registryHost();
    String latest;
    try {
      var plugin = marketplaceClient.getPlugin(registryId);
      latest = plugin == null ? null : StringUtils.trimToNull(plugin.latestVersion());
    } catch (MarketplaceException e) {
      return Outcome.notDone("registry '" + host + "' not used for '" + registryId + "': "
          + e.getMessage());
    }
    if (latest == null) {
      return Outcome.notDone("registry '" + host + "' lists no version of '" + registryId + "'");
    }
    if (installed.present() && PluginVersions.compare(latest, installed.version()) <= 0) {
      return Outcome.done(installed.version() + " is installed and current (registry '" + host
          + "' latest " + latest + ")");
    }
    try {
      var result = marketplaceInstaller.installUnattended(registryId,
          new MarketplaceInstallRQ(latest));
      var name = result.name();
      var mismatch = pluginId.equals(name) ? ""
          : " (the jar names itself '" + name + "', not '" + pluginId + "')";
      return Outcome.done("installed " + result.version() + " from the marketplace registry '"
          + host + "'" + (installed.present() ? ", was " + installed.version() : "") + mismatch);
    } catch (RuntimeException | LinkageError e) {
      return Outcome.notDone("registry '" + host + "' version " + latest + " of '" + registryId
          + "' not installed: " + e.getMessage());
    }
  }

  /**
   * The pinned step. Installs when nothing is installed, upgrades an older install to the pin,
   * leaves an equal or newer one alone.
   */
  private String fromPin(Installed installed) {
    if (!PluginArtifacts.isSafeIdentifier(pluginVersion)) {
      return describe(installed) + "; pinned source not usable: rp.plugins.default.version '"
          + pluginVersion + "' must be set to a plain identifier";
    }
    if (installed.present() && PluginVersions.compare(installed.version(), pluginVersion) >= 0) {
      return installed.version() + " kept, pinned " + pluginVersion + " is not newer";
    }
    if (!isHttps(pluginUrl)) {
      return describe(installed) + "; pinned " + pluginVersion + " not installed:"
          + " rp.plugins.default.url must be an absolute https URL";
    }
    var expected = pluginSha256.toLowerCase(Locale.ROOT);
    if (expected.isEmpty()) {
      return describe(installed) + "; pinned " + pluginVersion + " not installed:"
          + " rp.plugins.default.sha256 is not set, so the download cannot be verified";
    }

    Path downloaded = null;
    try {
      downloaded = PluginArtifacts.createOwnerOnlyTempFile(Path.of(pluginsTempPath),
          "rp-default-" + pluginId + "-" + pluginVersion + "-");
      fetcher.fetch(pluginUrl, downloaded);
      var actual = PluginArtifacts.sha256(downloaded);
      if (!expected.equals(actual)) {
        return describe(installed) + "; pinned " + pluginVersion + " not installed: the download"
            + " from '" + host(pluginUrl) + "' has sha256 " + actual + " but " + expected
            + " was configured";
      }
      try (InputStream jar = Files.newInputStream(downloaded)) {
        pluginBox.uploadPlugin(pluginId + "-" + pluginVersion + ".jar", jar);
      }
      return "installed pinned " + pluginVersion + " from '" + host(pluginUrl) + "'"
          + (installed.present() ? ", was " + installed.version() : "");
    } catch (IOException | RuntimeException e) {
      // No outbound access, a CDN refusal, an oversize body, a jar the loader rejects, a record
      // another node wrote first: the service is healthy without the plugin and it is retried at
      // the next start.
      return describe(installed) + "; pinned " + pluginVersion + " from '" + host(pluginUrl)
          + "' not installed: " + e.getMessage();
    } catch (LinkageError e) {
      return describe(installed) + "; pinned " + pluginVersion + " cannot run on this"
          + " ReportPortal: " + e;
    } finally {
      deleteQuietly(downloaded);
    }
  }

  private static String describe(Installed installed) {
    return installed.present() ? installed.version() + " kept" : "nothing installed";
  }

  /**
   * What is installed: the type named by the plugin id, or else one the marketplace installed
   * from the registry id — so a registry jar that calls itself something else is still found and
   * not reinstalled on every start.
   */
  private Installed installed() {
    var existing = integrationTypeRepository.findByName(pluginId);
    if (existing.isEmpty() && !registryId.isEmpty()) {
      existing = integrationTypeRepository.findAll().stream()
          .filter(type -> registryId.equals(detail(type, MARKETPLACE_PLUGIN_ID_KEY)))
          .findFirst();
    }
    if (existing.isEmpty()) {
      return new Installed(false, null);
    }
    Optional<String> version = existing.flatMap(it -> ofNullable(it.getDetails()))
        .flatMap(details -> ofNullable(details.getDetails()))
        .flatMap(IntegrationTypeProperties.VERSION::getValue)
        .map(String::valueOf)
        .map(StringUtils::trimToNull);
    return new Installed(true, version.orElse(null));
  }

  private static String detail(IntegrationType type, String key) {
    if (type.getDetails() == null || type.getDetails().getDetails() == null) {
      return null;
    }
    var value = type.getDetails().getDetails().get(key);
    return value == null ? null : String.valueOf(value);
  }

  private static boolean isHttps(String url) {
    try {
      var uri = URI.create(url);
      return "https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  private static String host(String url) {
    try {
      return ofNullable(URI.create(url).getHost()).orElse("<none>");
    } catch (IllegalArgumentException e) {
      return "<invalid url>";
    }
  }

  private static void deleteQuietly(Path file) {
    if (file != null) {
      try {
        Files.deleteIfExists(file);
      } catch (IOException e) {
        LOGGER.warn("Could not delete the staged plugin artifact '{}': {}", file, e.getMessage());
      }
    }
  }

  /** Whether the plugin is installed, and its version when that can be read. */
  private record Installed(boolean present, String version) {

  }

  private record Outcome(boolean done, String message) {

    static Outcome done(String message) {
      return new Outcome(true, message);
    }

    static Outcome notDone(String message) {
      return new Outcome(false, message);
    }
  }
}
