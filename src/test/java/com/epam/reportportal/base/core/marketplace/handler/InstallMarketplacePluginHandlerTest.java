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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.events.domain.PluginUploadedEvent;
import com.epam.reportportal.base.core.marketplace.Ed25519MarketplaceLicence;
import com.epam.reportportal.base.core.marketplace.MarketplaceArtifactFetcher;
import com.epam.reportportal.base.core.marketplace.MarketplaceClient;
import com.epam.reportportal.base.core.marketplace.MarketplaceLicence;
import com.epam.reportportal.base.core.marketplace.MarketplaceLicenceCredentials;
import com.epam.reportportal.base.core.marketplace.MarketplaceLicenceStore;
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
import com.epam.reportportal.base.model.marketplace.MarketplaceArtifact;
import com.epam.reportportal.base.model.marketplace.MarketplaceCompatibility;
import com.epam.reportportal.base.model.marketplace.MarketplaceInstallRQ;
import com.epam.reportportal.base.model.marketplace.MarketplaceVersionDetail;
import io.jsonwebtoken.Jwts;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

/**
 * The install flow, whose order is the safety property: nothing reaches PF4J until a downloaded
 * artifact has matched the checksum the registry published for it.
 */
class InstallMarketplacePluginHandlerTest {

  private static final String PLUGIN_ID = "jira";
  private static final byte[] JAR = "a plugin jar".getBytes(StandardCharsets.UTF_8);
  private static final byte[] TAMPERED = "a tampered jar".getBytes(StandardCharsets.UTF_8);

  private MarketplaceClient client;
  private MarketplaceArtifactFetcher fetcher;
  private MarketplaceLicence licence;
  private Pf4jPluginBox pluginBox;
  private IntegrationTypeRepository integrationTypeRepository;
  private ApplicationEventPublisher eventPublisher;
  private ReportPortalUser user;
  private byte[] uploaded;

  @BeforeEach
  void setUp() {
    client = mock(MarketplaceClient.class);
    fetcher = mock(MarketplaceArtifactFetcher.class);
    licence = mock(MarketplaceLicence.class);
    pluginBox = mock(Pf4jPluginBox.class);
    integrationTypeRepository = mock(IntegrationTypeRepository.class);
    eventPublisher = mock(ApplicationEventPublisher.class);
    user = mock(ReportPortalUser.class);
    uploaded = null;
    when(client.registryHost()).thenReturn("marketplace.reportportal.io");
    when(user.getUserId()).thenReturn(7L);
    when(user.getUsername()).thenReturn("admin");
    when(integrationTypeRepository.save(any(IntegrationType.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    // The jar arrives as a stream that is closed before any verify() can read it, so its bytes are
    // captured here — "reached PF4J with the verified bytes" is the claim under test.
    when(pluginBox.uploadPlugin(anyString(), any(InputStream.class))).thenAnswer(invocation -> {
      uploaded = invocation.getArgument(1, InputStream.class).readAllBytes();
      return installedType();
    });
    downloads(JAR);
  }

  private InstallMarketplacePluginHandlerImpl handler(String productVersion) {
    return new InstallMarketplacePluginHandlerImpl(client, fetcher,
        new ProductVersion(productVersion), licence, pluginBox, integrationTypeRepository,
        eventPublisher);
  }

  private InstallMarketplacePluginHandlerImpl handler() {
    return handler("25.2");
  }

  /** Makes the fetcher write the given bytes to whatever path the handler chose. */
  private void downloads(byte[] bytes) {
    org.mockito.Mockito.doAnswer(invocation -> {
      Files.write(invocation.getArgument(1, Path.class), bytes);
      return null;
    }).when(fetcher).fetch(anyString(), any(Path.class));
  }

  private static IntegrationType installedType() {
    var type = new IntegrationType();
    type.setId(42L);
    type.setName("jira");
    var details = new IntegrationTypeDetails();
    details.setDetails(new HashMap<>());
    type.setDetails(details);
    return type;
  }

  private static String sha256(byte[] bytes) {
    try {
      var digest = MessageDigest.getInstance("SHA-256").digest(bytes);
      var hex = new StringBuilder();
      for (var b : digest) {
        hex.append(String.format("%02x", b));
      }
      return hex.toString();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private static MarketplaceVersionDetail version(String access, String range, String sha256,
      boolean blocked, String blockReason) {
    return new MarketplaceVersionDetail(PLUGIN_ID, "JIRA", "1.4.2", "desc", null, null, null,
        range == null ? null : new MarketplaceCompatibility(range), null, access, null, "official",
        "jira", blocked, blocked ? Instant.EPOCH : null, blockReason, null, sha256, null,
        List.of());
  }

  private void registryServes(MarketplaceVersionDetail detail) {
    when(client.getVersion(PLUGIN_ID, "1.4.2")).thenReturn(detail);
  }

  private void publicPluginAt(String url) {
    when(client.resolveArtifact(eq(PLUGIN_ID), eq("1.4.2"), any()))
        .thenReturn(new MarketplaceArtifact(url, null));
  }

  private ReportPortalException install() {
    return assertThrows(ReportPortalException.class,
        () -> handler().install(PLUGIN_ID, new MarketplaceInstallRQ("1.4.2"), user));
  }

  // --- the one step that must never be reordered ------------------------------------------------

  @Test
  void checksumMismatchAbortsBeforePf4jIsTouchedAndLeavesNoTamperedFile() {
    registryServes(version("public", ">=25.1", sha256(JAR), false, null));
    publicPluginAt("https://cdn.example/jira-1.4.2.jar");
    downloads(TAMPERED);
    var target = ArgumentCaptor.forClass(Path.class);

    var thrown = install();

    assertEquals(ErrorType.MARKETPLACE_CHECKSUM_MISMATCH, thrown.getErrorType());
    verifyNoInteractions(pluginBox);
    verifyNoInteractions(eventPublisher);
    verify(integrationTypeRepository, never()).save(any(IntegrationType.class));
    // The bytes that failed verification are the ones that must not survive on disk.
    verify(fetcher).fetch(anyString(), target.capture());
    assertTrue(Files.notExists(target.getValue()), "tampered artifact left on disk");
  }

  @Test
  void checksumTheRegistryDidNotPublishRefusesRatherThanSkippingVerification() {
    registryServes(version("public", ">=25.1", " ", false, null));
    publicPluginAt("https://cdn.example/jira-1.4.2.jar");

    var thrown = install();

    assertEquals(ErrorType.MARKETPLACE_CHECKSUM_MISMATCH, thrown.getErrorType());
    verifyNoInteractions(pluginBox);
  }

  @Test
  void checksumComparisonIgnoresHexCase() {
    registryServes(
        version("public", ">=25.1", sha256(JAR).toUpperCase(java.util.Locale.ROOT), false, null));
    publicPluginAt("https://cdn.example/jira-1.4.2.jar");

    handler().install(PLUGIN_ID, new MarketplaceInstallRQ("1.4.2"), user);

    verify(pluginBox, times(1)).uploadPlugin(anyString(), any(InputStream.class));
  }

  // --- the compatibility gate -------------------------------------------------------------------

  @Test
  void incompatibleVersionIsRefusedNamingTheFailedBound() {
    registryServes(version("public", ">=25.1, <26.0", sha256(JAR), false, null));

    var thrown = assertThrows(ReportPortalException.class,
        () -> handler("26.4").install(PLUGIN_ID, new MarketplaceInstallRQ("1.4.2"), user));

    assertEquals(ErrorType.MARKETPLACE_PLUGIN_INCOMPATIBLE, thrown.getErrorType());
    assertTrue(thrown.getMessage().contains("26.4"), thrown.getMessage());
    assertTrue(thrown.getMessage().contains(">=25.1, <26.0"), thrown.getMessage());
    assertTrue(thrown.getMessage().contains("<26.0"), thrown.getMessage());
    verifyNoInteractions(pluginBox);
    verify(client, never()).resolveArtifact(anyString(), anyString(), any());
  }

  @Test
  void unknownProductVersionIsRefusedRatherThanGuessed() {
    registryServes(version("public", ">=25.1", sha256(JAR), false, null));

    var thrown = assertThrows(ReportPortalException.class,
        () -> handler(null).install(PLUGIN_ID, new MarketplaceInstallRQ("1.4.2"), user));

    assertEquals(ErrorType.MARKETPLACE_COMPATIBILITY_UNKNOWN, thrown.getErrorType());
    verifyNoInteractions(pluginBox);
    verify(client, never()).resolveArtifact(anyString(), anyString(), any());
  }

  @Test
  void unreadableCompatibilityRangeIsRefusedRatherThanGuessed() {
    registryServes(version("public", ">=next", sha256(JAR), false, null));

    var thrown = install();

    assertEquals(ErrorType.MARKETPLACE_COMPATIBILITY_UNKNOWN, thrown.getErrorType());
    verifyNoInteractions(pluginBox);
  }

  @Test
  void absentCompatibilityRangeIsRefusedRatherThanGuessed() {
    registryServes(version("public", null, sha256(JAR), false, null));

    var thrown = install();

    assertEquals(ErrorType.MARKETPLACE_COMPATIBILITY_UNKNOWN, thrown.getErrorType());
    verifyNoInteractions(pluginBox);
  }

  // --- premium and licence ----------------------------------------------------------------------

  @Test
  void premiumWithoutCredentialsIsRefusedBeforeTheRegistryIsAsked() {
    registryServes(version("premium", ">=25.1", sha256(JAR), false, null));
    when(licence.signArtifactToken(PLUGIN_ID)).thenReturn(Optional.empty());

    var thrown = install();

    assertEquals(ErrorType.MARKETPLACE_LICENCE_NOT_CONFIGURED, thrown.getErrorType());
    verify(client, never()).resolveArtifact(anyString(), anyString(), any());
    verifyNoInteractions(pluginBox);
  }

  @Test
  void premiumPresentsTheSignedTokenAndPublicPresentsNone() {
    registryServes(version("premium", ">=25.1", sha256(JAR), false, null));
    when(licence.signArtifactToken(PLUGIN_ID)).thenReturn(Optional.of("signed.jwt"));
    publicPluginAt("https://cdn.example/signed");

    handler().install(PLUGIN_ID, new MarketplaceInstallRQ("1.4.2"), user);
    verify(client).resolveArtifact(PLUGIN_ID, "1.4.2", "signed.jwt");

    registryServes(version("public", ">=25.1", sha256(JAR), false, null));
    handler().install(PLUGIN_ID, new MarketplaceInstallRQ("1.4.2"), user);
    verify(client).resolveArtifact(PLUGIN_ID, "1.4.2", null);
  }

  @Test
  void licenceRejectionIsMappedAsItsOwnFailure() {
    registryServes(version("premium", ">=25.1", sha256(JAR), false, null));
    when(licence.signArtifactToken(PLUGIN_ID)).thenReturn(Optional.of("signed.jwt"));
    when(client.resolveArtifact(eq(PLUGIN_ID), eq("1.4.2"), any())).thenThrow(
        new LicenceRejectedException(PLUGIN_ID, "1.4.2", 403, LicenceFailure.UNSPECIFIED, null,
            "Invalid license"));

    var thrown = install();

    assertEquals(ErrorType.MARKETPLACE_LICENCE_REJECTED, thrown.getErrorType());
    assertTrue(thrown.getMessage().contains("Invalid license"), thrown.getMessage());
    verifyNoInteractions(pluginBox);
  }

  /**
   * The registry answers an unknown customer, a signature it cannot match, an expired entitlement
   * and one that does not cover this plugin with the same 403. Naming one of the four would send
   * an operator to fix something that is not broken, so the message names all four.
   */
  @Test
  void anUnexplainedLicenceRejectionNamesEveryModeTheRegistryCannotTellApart() {
    registryServes(version("premium", ">=25.1", sha256(JAR), false, null));
    when(licence.signArtifactToken(PLUGIN_ID)).thenReturn(Optional.of("signed.jwt"));
    when(client.resolveArtifact(eq(PLUGIN_ID), eq("1.4.2"), any())).thenThrow(
        new LicenceRejectedException(PLUGIN_ID, "1.4.2", 403, LicenceFailure.UNSPECIFIED,
            "FORBIDDEN", "Invalid license"));

    var message = install().getMessage();

    assertTrue(message.contains("customer id"), message);
    assertTrue(message.contains("public keys"), message);
    assertTrue(message.contains("expired"), message);
    assertTrue(message.contains("cover this plugin"), message);
  }

  /** A reason the registry does give is reported as itself, not flattened back into all four. */
  @Test
  void aRejectionTheRegistryDidExplainIsReportedAsWhatItSaid() {
    registryServes(version("premium", ">=25.1", sha256(JAR), false, null));
    when(licence.signArtifactToken(PLUGIN_ID)).thenReturn(Optional.of("signed.jwt"));
    when(client.resolveArtifact(eq(PLUGIN_ID), eq("1.4.2"), any())).thenThrow(
        new LicenceRejectedException(PLUGIN_ID, "1.4.2", 403, LicenceFailure.EXPIRED,
            "LICENSE_EXPIRED", "License entitlement has expired"));

    var message = install().getMessage();

    assertTrue(message.contains("EXPIRED"), message);
    assertFalse(message.contains("customer id"), message);
  }

  /**
   * The whole chain with nothing stubbed in the middle: stored credentials, a real signature, and
   * a token the registry's own verification would accept. Every other premium test here stubs
   * {@link MarketplaceLicence}, which cannot catch an install that presents a token nobody signed.
   */
  @Test
  void premiumInstallPresentsATokenTheRegistryCanVerify() throws Exception {
    var keyPair = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
    var pkcs8 = keyPair.getPrivate().getEncoded();
    var seed = Base64.getEncoder()
        .encodeToString(Arrays.copyOfRange(pkcs8, pkcs8.length - 32, pkcs8.length));
    var store = new HeldCredentials(new MarketplaceLicenceCredentials("acme-gmbh", seed));
    licence = new Ed25519MarketplaceLicence(store, Duration.ofSeconds(60), Clock.systemUTC());
    registryServes(version("premium", ">=25.1", sha256(JAR), false, null));
    publicPluginAt("https://cdn.example/signed");

    handler().install(PLUGIN_ID, new MarketplaceInstallRQ("1.4.2"), user);

    var token = ArgumentCaptor.forClass(String.class);
    verify(client).resolveArtifact(eq(PLUGIN_ID), eq("1.4.2"), token.capture());
    var claims = Jwts.parser().verifyWith(keyPair.getPublic()).build()
        .parseSignedClaims(token.getValue()).getPayload();
    assertEquals("acme-gmbh", claims.get("customerId"));
    assertEquals(PLUGIN_ID, claims.get("pluginId"));
  }

  /** The same real chain with nothing stored: refused here, before the registry is asked. */
  @Test
  void premiumInstallWithNoStoredCredentialsIsRefusedByTheRealLicence() {
    licence = new Ed25519MarketplaceLicence(new HeldCredentials(null), Duration.ofSeconds(60),
        Clock.systemUTC());
    registryServes(version("premium", ">=25.1", sha256(JAR), false, null));

    var thrown = install();

    assertEquals(ErrorType.MARKETPLACE_LICENCE_NOT_CONFIGURED, thrown.getErrorType());
    verify(client, never()).resolveArtifact(anyString(), anyString(), any());
  }

  /** Whatever an admin set, held in memory — the storage round trip has its own test. */
  private record HeldCredentials(MarketplaceLicenceCredentials held)
      implements MarketplaceLicenceStore {

    @Override
    public Optional<String> customerId() {
      return Optional.ofNullable(held).map(MarketplaceLicenceCredentials::customerId);
    }

    @Override
    public Optional<MarketplaceLicenceCredentials> credentials() {
      return Optional.ofNullable(held);
    }

    @Override
    public void save(String customerId, String privateKey) {
      throw new UnsupportedOperationException();
    }
  }

  // --- what the operator did --------------------------------------------------------------------

  @Test
  void blockedVersionCarriesTheOperatorReason() {
    registryServes(version("public", ">=25.1", sha256(JAR), true, "CVE-2026-1"));

    var thrown = install();

    assertEquals(ErrorType.MARKETPLACE_VERSION_BLOCKED, thrown.getErrorType());
    assertTrue(thrown.getMessage().contains("CVE-2026-1"), thrown.getMessage());
    verify(client, never()).resolveArtifact(anyString(), anyString(), any());
    verifyNoInteractions(pluginBox);
  }

  @Test
  void blockedAtTheArtifactRouteCarriesTheOperatorReasonToo() {
    registryServes(version("public", ">=25.1", sha256(JAR), false, null));
    when(client.resolveArtifact(eq(PLUGIN_ID), eq("1.4.2"), any())).thenThrow(
        new VersionBlockedException(PLUGIN_ID, "1.4.2", "blocked while we investigate",
            Instant.EPOCH));

    var thrown = install();

    assertEquals(ErrorType.MARKETPLACE_VERSION_BLOCKED, thrown.getErrorType());
    assertTrue(thrown.getMessage().contains("blocked while we investigate"), thrown.getMessage());
    verifyNoInteractions(pluginBox);
  }

  @Test
  void removedPluginCarriesTheRemovalReason() {
    when(client.getVersion(PLUGIN_ID, "1.4.2")).thenThrow(
        new PluginRemovedException(PLUGIN_ID, "superseded by jira-cloud", Instant.EPOCH, "ops"));

    var thrown = install();

    assertEquals(ErrorType.MARKETPLACE_PLUGIN_REMOVED, thrown.getErrorType());
    assertTrue(thrown.getMessage().contains("superseded by jira-cloud"), thrown.getMessage());
    verifyNoInteractions(pluginBox);
  }

  // --- who is actually at fault -----------------------------------------------------------------

  @Test
  void anUnknownVersionIsNotFoundRatherThanAnUnreachableRegistry() {
    when(client.getVersion(PLUGIN_ID, "1.4.2")).thenThrow(
        new RegistryNotFoundException(PLUGIN_ID, "1.4.2", "NOT_FOUND", "Version not found"));

    var thrown = install();

    assertEquals(ErrorType.MARKETPLACE_PLUGIN_NOT_FOUND, thrown.getErrorType());
    assertTrue(thrown.getMessage().contains("version '1.4.2'"), thrown.getMessage());
    assertTrue(thrown.getMessage().contains(PLUGIN_ID), thrown.getMessage());
    verifyNoInteractions(pluginBox);
  }

  @Test
  void anUnknownPluginIdNamesThePluginRatherThanTheVersion() {
    when(client.getVersion(PLUGIN_ID, "1.4.2")).thenThrow(
        new RegistryNotFoundException(PLUGIN_ID, "1.4.2", "NOT_FOUND", "Plugin not found"));

    var thrown = install();

    assertEquals(ErrorType.MARKETPLACE_PLUGIN_NOT_FOUND, thrown.getErrorType());
    assertTrue(thrown.getMessage().contains("plugin '" + PLUGIN_ID + "'"), thrown.getMessage());
    assertFalse(thrown.getMessage().contains("version '1.4.2'"), thrown.getMessage());
  }

  @Test
  void aNotFoundTheRegistryDidNotAttributeNamesBoth() {
    when(client.getVersion(PLUGIN_ID, "1.4.2")).thenThrow(
        new RegistryNotFoundException(PLUGIN_ID, "1.4.2", "NOT_FOUND",
            "Plugin or version not found"));

    var thrown = install();

    assertEquals(ErrorType.MARKETPLACE_PLUGIN_NOT_FOUND, thrown.getErrorType());
    assertTrue(thrown.getMessage().contains("'jira:1.4.2'"), thrown.getMessage());
  }

  @Test
  void aGarbledRegistryAnswerIsTheRegistrysFaultNotTheNetworks() {
    registryServes(version("public", ">=25.1", sha256(JAR), false, null));
    when(client.resolveArtifact(eq(PLUGIN_ID), eq("1.4.2"), any())).thenThrow(
        new RegistryProtocolException("Unreadable artifact response for 'jira:1.4.2'"));

    var thrown = install();

    assertEquals(ErrorType.MARKETPLACE_REGISTRY_ERROR, thrown.getErrorType());
    verifyNoInteractions(pluginBox);
  }

  @Test
  void aCdnThatRefusesTheDownloadIsTheRegistrysFaultNotTheNetworks() {
    registryServes(version("public", ">=25.1", sha256(JAR), false, null));
    publicPluginAt("https://cdn.example/jira-1.4.2.jar");
    org.mockito.Mockito.doThrow(new RegistryProtocolException(
            "Marketplace artifact could not be downloaded from 'cdn.example'"))
        .when(fetcher).fetch(anyString(), any());

    var thrown = install();

    assertEquals(ErrorType.MARKETPLACE_REGISTRY_ERROR, thrown.getErrorType());
    verifyNoInteractions(pluginBox);
  }

  @Test
  void aRegistryStatusNobodyCanActOnIsReportedAsTheRegistryAnsweringUnusably() {
    when(client.getVersion(PLUGIN_ID, "1.4.2")).thenThrow(
        new RegistryResponseException(500, "INTERNAL_ERROR", "boom"));

    var thrown = install();

    assertEquals(ErrorType.MARKETPLACE_REGISTRY_ERROR, thrown.getErrorType());
    verifyNoInteractions(pluginBox);
  }

  @Test
  void anEmptyRegistryAnswerIsReportedAsTheRegistryAnsweringUnusably() {
    when(client.getVersion(PLUGIN_ID, "1.4.2")).thenReturn(null);

    var thrown = install();

    assertEquals(ErrorType.MARKETPLACE_REGISTRY_ERROR, thrown.getErrorType());
    verifyNoInteractions(pluginBox);
  }

  /** A subtype the mapping has never seen; the catch-all must not file it under something else. */
  private static class FutureMarketplaceException extends MarketplaceException {

    FutureMarketplaceException() {
      super("a failure mode added after this mapping was written");
    }
  }

  @Test
  void anUnmappedFailureIsNamedRatherThanSwallowedAsUnreachable() {
    when(client.getVersion(PLUGIN_ID, "1.4.2")).thenThrow(new FutureMarketplaceException());

    var thrown = install();

    assertEquals(ErrorType.MARKETPLACE_REGISTRY_ERROR, thrown.getErrorType());
    assertTrue(thrown.getMessage().contains("FutureMarketplaceException"), thrown.getMessage());
    verifyNoInteractions(pluginBox);
  }

  @Test
  void unreachableRegistryNamesTheHost() {
    when(client.getVersion(PLUGIN_ID, "1.4.2")).thenThrow(new RegistryUnreachableException(
        "marketplace.reportportal.io", new SocketTimeoutException("read timed out")));

    var thrown = install();

    assertEquals(ErrorType.MARKETPLACE_REGISTRY_UNREACHABLE, thrown.getErrorType());
    assertTrue(thrown.getMessage().contains("marketplace.reportportal.io"), thrown.getMessage());
    verifyNoInteractions(pluginBox);
  }

  @Test
  void aDownloadThatCannotBeCompletedNeverReachesPf4jAndLeavesNoPartialFile() {
    registryServes(version("public", ">=25.1", sha256(JAR), false, null));
    publicPluginAt("https://cdn.example/jira-1.4.2.jar");
    org.mockito.Mockito.doThrow(new RegistryUnreachableException("cdn.example",
        new SocketTimeoutException("read timed out"))).when(fetcher).fetch(anyString(), any());
    var target = ArgumentCaptor.forClass(Path.class);

    var thrown = install();

    assertEquals(ErrorType.MARKETPLACE_REGISTRY_UNREACHABLE, thrown.getErrorType());
    verifyNoInteractions(pluginBox);
    // The temp file exists from the moment it is named, so a failed download still leaves one.
    verify(fetcher).fetch(anyString(), target.capture());
    assertTrue(Files.notExists(target.getValue()), "partial artifact left on disk");
  }

  // --- the happy path ---------------------------------------------------------------------------

  @Test
  void happyPathUploadsTheVerifiedBytesOnceAndPersistsRegistryIdAndVersion() {
    registryServes(version("public", ">=25.1, <26.0", sha256(JAR), false, null));
    publicPluginAt("https://cdn.example/jira-1.4.2.jar");

    var result = handler().install(PLUGIN_ID, new MarketplaceInstallRQ("1.4.2"), user);

    verify(pluginBox, times(1)).uploadPlugin(anyString(), any(InputStream.class));
    assertArrayEquals(JAR, uploaded);

    var saved = ArgumentCaptor.forClass(IntegrationType.class);
    verify(integrationTypeRepository).save(saved.capture());
    var details = saved.getValue().getDetails().getDetails();
    assertEquals(PLUGIN_ID, details.get("marketplacePluginId"));
    assertEquals("1.4.2", details.get("marketplaceVersion"));

    assertEquals(42L, result.integrationTypeId());
    assertEquals(PLUGIN_ID, result.pluginId());
    assertEquals("1.4.2", result.version());
  }

  @Test
  void happyPathPublishesTheSameEventAnUploadPublishes() {
    registryServes(version("public", ">=25.1", sha256(JAR), false, null));
    publicPluginAt("https://cdn.example/jira-1.4.2.jar");

    handler().install(PLUGIN_ID, new MarketplaceInstallRQ("1.4.2"), user);

    var event = ArgumentCaptor.forClass(PluginUploadedEvent.class);
    verify(eventPublisher).publishEvent(event.capture());
    assertEquals(42L, event.getValue().getPluginActivityResource().getId());
    assertEquals("jira", event.getValue().getPluginActivityResource().getName());
    assertEquals(7L, event.getValue().getUserId());
    assertEquals("admin", event.getValue().getUserLogin());
  }

  @Test
  void pf4jIsHandedTheRegistryIdAndVersionAsTheJarFileName() {
    // PF4J derives the file it places in the plugins directory from this name, so both halves and
    // the extension are part of the contract, not a label.
    registryServes(version("public", ">=25.1", sha256(JAR), false, null));
    publicPluginAt("https://cdn.example/jira-1.4.2.jar");
    var fileName = ArgumentCaptor.forClass(String.class);

    handler().install(PLUGIN_ID, new MarketplaceInstallRQ("1.4.2"), user);

    verify(pluginBox).uploadPlugin(fileName.capture(), any(InputStream.class));
    assertEquals("jira-1.4.2.jar", fileName.getValue());
  }

  @Test
  void theDownloadedFileIsNotLeftBehind() {
    registryServes(version("public", ">=25.1", sha256(JAR), false, null));
    publicPluginAt("https://cdn.example/jira-1.4.2.jar");
    var target = ArgumentCaptor.forClass(Path.class);

    handler().install(PLUGIN_ID, new MarketplaceInstallRQ("1.4.2"), user);

    verify(fetcher).fetch(eq("https://cdn.example/jira-1.4.2.jar"), target.capture());
    assertTrue(Files.notExists(target.getValue()), "temp artifact left on disk");
  }

  // --- what the caller is allowed to ask for ----------------------------------------------------

  @Test
  void aVersionThatCouldEscapeThePluginDirectoryIsRefusedBeforeAnyRegistryCall() {
    var thrown = assertThrows(ReportPortalException.class,
        () -> handler().install(PLUGIN_ID, new MarketplaceInstallRQ("../../../etc/passwd"), user));

    assertEquals(ErrorType.BAD_REQUEST_ERROR, thrown.getErrorType());
    verify(client, never()).getVersion(anyString(), anyString());
    verifyNoInteractions(pluginBox);
  }

  @Test
  void aRegistryIdThatCouldEscapeThePluginDirectoryIsRefusedBeforeAnyRegistryCall() {
    var thrown = assertThrows(ReportPortalException.class,
        () -> handler().install("../evil", new MarketplaceInstallRQ("1.4.2"), user));

    assertEquals(ErrorType.BAD_REQUEST_ERROR, thrown.getErrorType());
    verify(client, never()).getVersion(anyString(), anyString());
    verifyNoInteractions(pluginBox);
  }
}
