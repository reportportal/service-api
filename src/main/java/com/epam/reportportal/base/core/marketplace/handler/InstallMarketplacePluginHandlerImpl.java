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

import com.epam.reportportal.base.core.events.domain.PluginUploadedEvent;
import com.epam.reportportal.base.core.marketplace.CompatibilityRange;
import com.epam.reportportal.base.core.marketplace.MarketplaceArtifactFetcher;
import com.epam.reportportal.base.core.marketplace.MarketplaceClient;
import com.epam.reportportal.base.core.marketplace.MarketplaceLicence;
import com.epam.reportportal.base.core.marketplace.ProductVersion;
import com.epam.reportportal.base.core.marketplace.exception.LicenceFailure;
import com.epam.reportportal.base.core.marketplace.exception.LicenceRejectedException;
import com.epam.reportportal.base.core.marketplace.exception.MarketplaceException;
import com.epam.reportportal.base.core.marketplace.exception.PluginRemovedException;
import com.epam.reportportal.base.core.marketplace.exception.RegistryNotFoundException;
import com.epam.reportportal.base.core.marketplace.exception.RegistryProtocolException;
import com.epam.reportportal.base.core.marketplace.exception.RegistryResponseException;
import com.epam.reportportal.base.core.marketplace.exception.RegistryUnreachableException;
import com.epam.reportportal.base.core.marketplace.exception.VersionBlockedException;
import com.epam.reportportal.base.core.plugin.Pf4jPluginBox;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationTypeRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationTypeDetails;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.activity.PluginActivityResource;
import com.epam.reportportal.base.model.marketplace.MarketplaceInstallRQ;
import com.epam.reportportal.base.model.marketplace.MarketplaceInstallResource;
import com.epam.reportportal.base.model.marketplace.MarketplaceVersionDetail;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Locale;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Makes one registry version of a plugin the active one — install, update and rollback alike.
 *
 * <p>The order of the steps is the safety property, not an implementation detail. Every refusal
 * happens while the instance is still untouched, and the checksum is verified against what the
 * registry published <em>before</em> anything is handed to PF4J. What follows is the existing
 * upload path, which already unloads the previous plugin, loads and starts the new one, and
 * reloads the previous one if that fails; none of it is reimplemented here.
 */
@Service
public class InstallMarketplacePluginHandlerImpl implements InstallMarketplacePluginHandler {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(InstallMarketplacePluginHandlerImpl.class);

  /** Registry id persisted at install time; the catalogue merge matches on it first. */
  private static final String MARKETPLACE_PLUGIN_ID_KEY = "marketplacePluginId";
  private static final String MARKETPLACE_VERSION_KEY = "marketplaceVersion";
  private static final String PREMIUM = "premium";

  /**
   * Both identifiers become part of a file name under the plugins temp directory, so neither may
   * carry a separator or a dot segment. The registry's own id grammar is stricter still; this only
   * has to stop what the caller sends from leaving the directory.
   */
  private static final Pattern SAFE_IDENTIFIER =
      Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]{0,126}");

  private final MarketplaceClient client;
  private final MarketplaceArtifactFetcher fetcher;
  private final ProductVersion productVersion;
  private final MarketplaceLicence licence;
  private final Pf4jPluginBox pluginBox;
  private final IntegrationTypeRepository integrationTypeRepository;
  private final ApplicationEventPublisher eventPublisher;

  /**
   * Creates the handler.
   *
   * @param client                    registry client
   * @param fetcher                   artifact downloader
   * @param productVersion            running ReportPortal release
   * @param licence                   premium licence state of this instance
   * @param pluginBox                 the existing PF4J upload path
   * @param integrationTypeRepository local integration types
   * @param eventPublisher            publisher of the plugin upload event
   */
  public InstallMarketplacePluginHandlerImpl(MarketplaceClient client,
      MarketplaceArtifactFetcher fetcher, ProductVersion productVersion, MarketplaceLicence licence,
      Pf4jPluginBox pluginBox, IntegrationTypeRepository integrationTypeRepository,
      ApplicationEventPublisher eventPublisher) {
    this.client = client;
    this.fetcher = fetcher;
    this.productVersion = productVersion;
    this.licence = licence;
    this.pluginBox = pluginBox;
    this.integrationTypeRepository = integrationTypeRepository;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public MarketplaceInstallResource install(String registryId, MarketplaceInstallRQ request,
      ReportPortalUser user) {
    var version = request == null ? null : StringUtils.trimToEmpty(request.version());
    safe(registryId, "plugin id");
    safe(version, "version");

    // 1. What the registry says about this exact version. Not cached: a block or a removal that
    // landed a minute ago has to be seen now, and so does the checksum.
    var detail = registry(() -> client.getVersion(registryId, version));
    if (detail == null) {
      // An empty 200 is the registry answering unusably, not a registry that cannot be reached.
      throw new ReportPortalException(ErrorType.MARKETPLACE_REGISTRY_ERROR,
          "no answer for '" + registryId + ":" + version + "' from '" + client.registryHost()
              + "'");
    }

    // 2. Compatibility, decided locally and never guessed.
    verifyCompatible(registryId, version, detail);

    // 3. A version an operator blocked is refused with their reason, before it is downloaded.
    if (detail.blocked()) {
      throw new ReportPortalException(ErrorType.MARKETPLACE_VERSION_BLOCKED,
          reason(registryId, version, detail.blockReason()));
    }

    // 4. Premium needs a licence. Signed here and used immediately: both the token and the URL the
    // registry hands back live about a minute, so there is nothing to cache or queue.
    final String licenceJwt = PREMIUM.equalsIgnoreCase(detail.access())
        ? licence.signArtifactToken(registryId).orElseThrow(
            () -> new ReportPortalException(ErrorType.MARKETPLACE_LICENCE_NOT_CONFIGURED,
                "'" + registryId + "' is a premium plugin and this instance holds no licence"
                    + " credentials"))
        : null;

    var artifact = registry(() -> client.resolveArtifact(registryId, version, licenceJwt));
    if (artifact == null || StringUtils.isBlank(artifact.downloadUrl())) {
      throw new ReportPortalException(ErrorType.MARKETPLACE_REGISTRY_ERROR,
          "no download URL for '" + registryId + ":" + version + "' from '" + client.registryHost()
              + "'");
    }

    Path downloaded = null;
    try {
      downloaded = tempFile(registryId, version);
      // 5. Download, then verify. Nothing below this line runs on unverified bytes.
      var artifactPath = downloaded;
      registry(() -> {
        fetcher.fetch(artifact.downloadUrl(), artifactPath);
        return null;
      });
      verifyChecksum(registryId, version, detail, downloaded);

      // 6. The existing upload path, which owns the unload/load/rollback that makes this atomic.
      IntegrationType installed;
      try (InputStream jar = Files.newInputStream(downloaded)) {
        installed = uploaded(registryId, version, downloaded, jar);
      }

      // 7. and 8. — the same event an upload publishes, and the id-mapping record.
      var persisted = persistOrigin(installed, registryId, version);
      publish(persisted, user);
      return new MarketplaceInstallResource(persisted.getId(), persisted.getName(), registryId,
          version);
    } catch (IOException e) {
      throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
          "could not read the downloaded artifact of '" + registryId + ":" + version + "': "
              + e.getMessage());
    } finally {
      delete(downloaded);
    }
  }

  /**
   * The upload, with anything the loader throws by surprise turned into a plugin-upload error
   * that names what was being installed.
   *
   * <p>The loader is written for a jar a human chose and can look at; here the bytes came from
   * the registry, and the operator never saw them. A jar with no manifest, for instance, reaches
   * {@code Manifest.getMainAttributes()} on a null and the page ends up showing
   * "Unclassified error [Cannot invoke ...]" — true, and useless. A ReportPortalException is left
   * alone: the loader's own refusals already say what is wrong.
   */
  private IntegrationType uploaded(String registryId, String version, Path artifact,
      InputStream jar) {
    try {
      return pluginBox.uploadPlugin(fileName(registryId, version), jar);
    } catch (ReportPortalException e) {
      throw e;
    } catch (RuntimeException e) {
      LOGGER.error("Loading the artifact of '{}:{}' downloaded to '{}' failed", registryId, version,
          artifact, e);
      throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
          "'" + registryId + ":" + version + "' downloaded from '" + client.registryHost()
              + "' is not a plugin this ReportPortal can load. The artifact is intact — its"
              + " checksum matched — so this is a problem with the plugin itself, not with the"
              + " download.");
    }
  }

  /**
   * Refuses anything that could name a file outside the plugins directory. Checked before the
   * registry is called, so a probe cannot be turned into a request either.
   */
  private static void safe(String value, String what) {
    if (value == null || !SAFE_IDENTIFIER.matcher(value).matches()) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "marketplace " + what + " '" + value + "'");
    }
  }

  /**
   * The gate. An unknown product version or an unreadable range is a refusal of its own kind: it
   * says the question could not be answered, which is different from answering no, and it is the
   * operator rather than the caller who has something to fix.
   */
  private void verifyCompatible(String registryId, String version,
      MarketplaceVersionDetail detail) {
    var declared = detail.compatibility() == null ? null : detail.compatibility().reportportal();
    if (!productVersion.isKnown()) {
      throw new ReportPortalException(ErrorType.MARKETPLACE_COMPATIBILITY_UNKNOWN,
          "the ReportPortal release this instance runs is unknown (rp.product.version is not set),"
              + " so '" + registryId + ":" + version + "' cannot be checked against its declared"
              + " range '" + declared + "'");
    }
    var range = CompatibilityRange.parse(declared);
    if (range.isEmpty()) {
      throw new ReportPortalException(ErrorType.MARKETPLACE_COMPATIBILITY_UNKNOWN,
          "'" + registryId + ":" + version + "' declares the compatibility range '" + declared
              + "', which cannot be read, so it is not installed on a guess");
    }
    var failed = range.get().failedBounds(productVersion.value());
    if (!failed.isEmpty()) {
      throw new ReportPortalException(ErrorType.MARKETPLACE_PLUGIN_INCOMPATIBLE,
          "this instance runs ReportPortal " + productVersion.value() + ", '" + registryId + ":"
              + version + "' requires '" + declared + "' and fails " + String.join(", ", failed));
    }
  }

  /**
   * The one step that must never be reordered or skipped. A checksum the registry did not publish
   * is not a pass: it means the artifact cannot be verified at all.
   */
  private void verifyChecksum(String registryId, String version, MarketplaceVersionDetail detail,
      Path artifact) throws IOException {
    var expected = StringUtils.trimToNull(detail.sha256());
    if (expected == null) {
      throw new ReportPortalException(ErrorType.MARKETPLACE_CHECKSUM_MISMATCH,
          "the registry published no sha256 for '" + registryId + ":" + version + "', so the"
              + " download cannot be verified");
    }
    var actual = sha256(artifact);
    if (!expected.toLowerCase(Locale.ROOT).equals(actual)) {
      throw new ReportPortalException(ErrorType.MARKETPLACE_CHECKSUM_MISMATCH,
          "'" + registryId + ":" + version + "' downloaded as sha256 " + actual + ", the registry"
              + " published " + expected.toLowerCase(Locale.ROOT) + "; nothing was installed");
    }
  }

  private static String sha256(Path artifact) throws IOException {
    MessageDigest digest;
    try {
      digest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is not available", e);
    }
    try (InputStream in = Files.newInputStream(artifact)) {
      var buffer = new byte[8192];
      for (int read = in.read(buffer); read > 0; read = in.read(buffer)) {
        digest.update(buffer, 0, read);
      }
    }
    return HexFormat.of().formatHex(digest.digest());
  }

  /**
   * Part (b) of the id-mapping decision: an instance always knows which registry entry a plugin
   * came from, whatever a later manifest says. The PF4J {@code version} the upload path wrote is
   * left alone — it is read from the jar's own manifest and is the stronger claim of the two.
   */
  private IntegrationType persistOrigin(IntegrationType installed, String registryId,
      String version) {
    var details = installed.getDetails();
    if (details == null) {
      details = new IntegrationTypeDetails();
      installed.setDetails(details);
    }
    if (details.getDetails() == null) {
      details.setDetails(new HashMap<>());
    }
    details.getDetails().put(MARKETPLACE_PLUGIN_ID_KEY, registryId);
    details.getDetails().put(MARKETPLACE_VERSION_KEY, version);
    return integrationTypeRepository.save(installed);
  }

  /** The event an upload publishes, so existing listeners cannot tell the two apart. */
  private void publish(IntegrationType installed, ReportPortalUser user) {
    var activity = new PluginActivityResource();
    activity.setId(installed.getId());
    activity.setName(installed.getName());
    eventPublisher.publishEvent(
        new PluginUploadedEvent(activity, user.getUserId(), user.getUsername()));
  }

  /**
   * Turns a registry failure into the one the caller can act on, and above all names the right
   * actor. A 404 is a healthy registry answering that it holds no such thing — sending an operator
   * to check DNS for a version they mistyped wastes the one thing an incident costs. A garbled
   * body, a bad download URL or a CDN that refused is the registry's own fault (502); only a
   * registry that could not be talked to at all is unreachable (503), and that one names the host.
   */
  private <T> T registry(RegistryCall<T> call) {
    try {
      return call.execute();
    } catch (PluginRemovedException e) {
      throw new ReportPortalException(ErrorType.MARKETPLACE_PLUGIN_REMOVED,
          reason(e.getPluginId(), null, e.getRemovalReason()));
    } catch (VersionBlockedException e) {
      throw new ReportPortalException(ErrorType.MARKETPLACE_VERSION_BLOCKED,
          reason(e.getPluginId(), e.getVersion(), e.getReason()));
    } catch (LicenceRejectedException e) {
      throw new ReportPortalException(ErrorType.MARKETPLACE_LICENCE_REJECTED, rejected(e));
    } catch (RegistryNotFoundException e) {
      throw new ReportPortalException(ErrorType.MARKETPLACE_PLUGIN_NOT_FOUND, missing(e));
    } catch (RegistryProtocolException | RegistryResponseException e) {
      throw new ReportPortalException(ErrorType.MARKETPLACE_REGISTRY_ERROR, e.getMessage());
    } catch (RegistryUnreachableException e) {
      throw new ReportPortalException(ErrorType.MARKETPLACE_REGISTRY_UNREACHABLE, e.getMessage());
    } catch (MarketplaceException e) {
      // A subtype added after this mapping was written. Absorbing it into one of the arms above
      // would blame an actor at random, so it is named instead — in the log and to the caller.
      LOGGER.error("Unmapped marketplace failure {}", e.getClass().getName(), e);
      throw new ReportPortalException(ErrorType.MARKETPLACE_REGISTRY_ERROR,
          e.getClass().getSimpleName() + ": " + e.getMessage());
    }
  }

  /**
   * Says what a licence rejection actually establishes and no more. The registry answers unknown
   * customer, a signature its stored public keys do not match, an expired entitlement and one that
   * does not cover this plugin with the same 403, so it cannot be asked which happened. Naming one
   * of the four would send an operator to rotate a key that is fine, so all four are named — the
   * honest answer, and the one that stops being needed the day the registry sends distinct codes,
   * which {@link LicenceFailure} already reads.
   */
  private static String rejected(LicenceRejectedException e) {
    if (e.getFailure() != LicenceFailure.UNSPECIFIED) {
      return e.getMessage();
    }
    return "the registry rejected this instance's licence for '" + e.getPluginId() + ":"
        + e.getVersion() + "' without saying why: the customer id may be unknown to it, the key"
        + " may not match the entitlement's public keys, the entitlement may have expired, or it"
        + " may not cover this plugin"
        + (e.getRegistryMessage() == null ? "" : " (registry said: " + e.getRegistryMessage()
            + ")");
  }

  /** Names whichever of plugin and version the registry said was missing, and no more. */
  private String missing(RegistryNotFoundException e) {
    var subject = switch (e.getSubject()) {
      case PLUGIN -> "plugin '" + e.getPluginId() + "'";
      case VERSION -> "version '" + e.getVersion() + "' of plugin '" + e.getPluginId() + "'";
      case UNSPECIFIED -> "'" + e.getPluginId()
          + (e.getVersion() == null ? "" : ":" + e.getVersion()) + "'";
    };
    return subject + " is not in the registry at '" + client.registryHost() + "'";
  }

  private static String reason(String pluginId, String version, String operatorReason) {
    var subject = "'" + pluginId + (version == null ? "" : ":" + version) + "'";
    return operatorReason == null ? subject + ", no reason given"
        : subject + ": " + operatorReason;
  }

  private static String fileName(String registryId, String version) {
    return registryId + "-" + version + ".jar";
  }

  private static Path tempFile(String registryId, String version) throws IOException {
    return Files.createTempFile("rp-marketplace-" + registryId + "-" + version + "-", ".jar");
  }

  private static void delete(Path file) {
    if (file == null) {
      return;
    }
    try {
      Files.deleteIfExists(file);
    } catch (IOException e) {
      LOGGER.warn("Could not delete the downloaded marketplace artifact '{}': {}", file,
          e.getMessage());
    }
  }

  @FunctionalInterface
  private interface RegistryCall<T> {

    T execute();
  }
}
