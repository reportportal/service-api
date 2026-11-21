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
import com.epam.reportportal.base.core.marketplace.PluginArtifacts;
import com.epam.reportportal.base.core.marketplace.PluginVersions;
import com.epam.reportportal.base.core.plugin.Pf4jPluginBox;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationTypeRepository;
import com.epam.reportportal.extension.common.IntegrationTypeProperties;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Optional install of the default plugin on application startup.
 *
 * <p>Runs after the application context has finished refreshing and readiness probes can pass,
 * rather than blocking context startup ({@code @PostConstruct}) - loading every enabled plugin
 * eagerly on the same thread as the rest of the bean graph stacks its memory footprint on top of
 * Spring's own startup peak.
 *
 * <p>The default plugin is described by pinned coordinates ({@code rp.plugins.default.id},
 * {@code .version}, {@code .url}, {@code .sha256}) rather than by a mutable remote index. The jar
 * is fetched over HTTPS with the marketplace's bounded downloader and its SHA-256 is verified
 * before the bytes reach the plugin loader. Any refusal or failure is logged and never fails the
 * startup.
 *
 * <p>An installed version of the same plugin that is older than the pinned one is upgraded; an
 * equal or newer one (for instance installed from the marketplace) is left alone, so the pin can
 * never downgrade an instance.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Component
public class PluginStartUpService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PluginStartUpService.class);

  private final Pf4jPluginBox pluginBox;
  private final IntegrationTypeRepository integrationTypeRepository;
  private final MarketplaceArtifactFetcher fetcher;
  private final boolean defaultPluginsLoad;
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
   * @param defaultPluginsLoad        whether to install the default plugin
   * @param pluginId                  plugin id of the default plugin
   * @param pluginVersion             pinned version
   * @param pluginUrl                 immutable HTTPS download URL of that version
   * @param pluginSha256              expected SHA-256 of the jar, hex
   * @param pluginsTempPath           staging directory for the unverified download
   */
  public PluginStartUpService(Pf4jPluginBox pluginBox,
      IntegrationTypeRepository integrationTypeRepository, MarketplaceArtifactFetcher fetcher,
      @Value("${rp.plugins.default.load}") boolean defaultPluginsLoad,
      @Value("${rp.plugins.default.id:}") String pluginId,
      @Value("${rp.plugins.default.version:}") String pluginVersion,
      @Value("${rp.plugins.default.url:}") String pluginUrl,
      @Value("${rp.plugins.default.sha256:}") String pluginSha256,
      @Value("${rp.plugins.temp.path}") String pluginsTempPath) {
    this.pluginBox = pluginBox;
    this.integrationTypeRepository = integrationTypeRepository;
    this.fetcher = fetcher;
    this.defaultPluginsLoad = defaultPluginsLoad;
    this.pluginId = pluginId == null ? "" : pluginId.trim();
    this.pluginVersion = pluginVersion == null ? "" : pluginVersion.trim();
    this.pluginUrl = pluginUrl == null ? "" : pluginUrl.trim();
    this.pluginSha256 = pluginSha256 == null ? "" : pluginSha256.trim();
    this.pluginsTempPath = pluginsTempPath;
  }

  @Async("pluginStartupExecutor")
  @EventListener(ApplicationReadyEvent.class)
  public void loadPlugins() {
    pluginBox.startUp();
    if (defaultPluginsLoad) {
      installDefaultPlugin();
    }
  }

  private void installDefaultPlugin() {
    Path downloaded = null;
    try {
      if (!PluginArtifacts.isSafeIdentifier(pluginId) || !PluginArtifacts.isSafeIdentifier(
          pluginVersion)) {
        LOGGER.error("Default plugin not installed: rp.plugins.default.id '{}' and"
            + " rp.plugins.default.version '{}' must both be set to plain identifiers", pluginId,
            pluginVersion);
        return;
      }
      if (!isHttps(pluginUrl)) {
        LOGGER.error("Default plugin '{}:{}' not installed: rp.plugins.default.url must be an"
            + " absolute https URL", pluginId, pluginVersion);
        return;
      }
      var expected = pluginSha256.toLowerCase(Locale.ROOT);
      if (expected.isEmpty()) {
        LOGGER.error("Default plugin '{}:{}' not installed: rp.plugins.default.sha256 is not set,"
            + " so the download cannot be verified", pluginId, pluginVersion);
        return;
      }
      if (!shouldInstall()) {
        return;
      }

      downloaded = PluginArtifacts.createOwnerOnlyTempFile(Path.of(pluginsTempPath),
          "rp-default-" + pluginId + "-" + pluginVersion + "-");
      fetcher.fetch(pluginUrl, downloaded);
      var actual = PluginArtifacts.sha256(downloaded);
      if (!expected.equals(actual)) {
        LOGGER.error("Default plugin '{}:{}' not installed: the download from '{}' has sha256 {}"
            + " but {} was configured", pluginId, pluginVersion, host(pluginUrl), actual,
            expected);
        return;
      }
      try (InputStream jar = Files.newInputStream(downloaded)) {
        pluginBox.uploadPlugin(pluginId + "-" + pluginVersion + ".jar", jar);
      }
      LOGGER.info("Default plugin '{}' version {} installed", pluginId, pluginVersion);
    } catch (IOException | RuntimeException e) {
      // No outbound access, a CDN refusal, an oversize body, a jar the loader rejects: the service
      // is healthy without the plugin and it is retried at the next start.
      LOGGER.warn("Can't install default plugin '{}:{}' from '{}'. Error: {}", pluginId,
          pluginVersion, host(pluginUrl), e.getMessage());
    } catch (LinkageError e) {
      LOGGER.error("Default plugin '{}:{}' cannot run on this ReportPortal: {}", pluginId,
          pluginVersion, e.toString());
    } finally {
      deleteQuietly(downloaded);
    }
  }

  /**
   * Nothing installed: install. Installed older: upgrade to the pin. Installed equal or newer, or
   * with a version that cannot be read: leave alone.
   */
  private boolean shouldInstall() {
    var existing = integrationTypeRepository.findByName(pluginId);
    if (existing.isEmpty()) {
      return true;
    }
    Optional<String> installed = existing.flatMap(it -> ofNullable(it.getDetails()))
        .flatMap(details -> IntegrationTypeProperties.VERSION.getValue(details.getDetails())
            .map(String::valueOf));
    if (installed.isEmpty()) {
      LOGGER.warn("Default plugin '{}' is installed without a readable version; leaving it alone",
          pluginId);
      return false;
    }
    if (PluginVersions.compare(installed.get(), pluginVersion) >= 0) {
      LOGGER.info("Plugin '{}' version {} is already installed (pinned default {})", pluginId,
          installed.get(), pluginVersion);
      return false;
    }
    LOGGER.info("Upgrading plugin '{}' from {} to the pinned default {}", pluginId,
        installed.get(), pluginVersion);
    return true;
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
}
