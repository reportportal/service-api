/*
 * Copyright 2026 EPAM Systems
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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.events.domain.PluginUploadedEvent;
import com.epam.reportportal.base.core.marketplace.MarketplaceArtifactFetcher;
import com.epam.reportportal.base.core.marketplace.MarketplaceClient;
import com.epam.reportportal.base.core.marketplace.MarketplaceLicence;
import com.epam.reportportal.base.core.marketplace.ProductVersion;
import com.epam.reportportal.base.core.marketplace.exception.RegistryNotFoundException;
import com.epam.reportportal.base.core.marketplace.exception.RegistryUnreachableException;
import com.epam.reportportal.base.core.marketplace.handler.InstallMarketplacePluginHandlerImpl;
import com.epam.reportportal.base.core.plugin.Pf4jPluginBox;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationTypeRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationTypeDetails;
import com.epam.reportportal.base.model.marketplace.MarketplaceArtifact;
import com.epam.reportportal.base.model.marketplace.MarketplaceCompatibility;
import com.epam.reportportal.base.model.marketplace.MarketplacePluginDetail;
import com.epam.reportportal.base.model.marketplace.MarketplaceVersionDetail;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * The startup decision, driven through the real marketplace install path with the registry, the
 * downloader and the plugin loader mocked — no network.
 */
class PluginStartUpServiceTest {

  private static final byte[] PINNED_JAR = "pinned jar 6.0.0".getBytes(StandardCharsets.UTF_8);
  private static final byte[] REGISTRY_JAR = "registry jar 6.1.0".getBytes(StandardCharsets.UTF_8);
  private static final String PINNED_URL = "https://example.test/plugin-junit-6.0.0.jar";
  private static final String CDN_URL = "https://cdn.example.test/junit-6.1.0.jar";

  @TempDir
  Path temp;

  private Pf4jPluginBox pluginBox;
  private IntegrationTypeRepository repository;
  private MarketplaceArtifactFetcher fetcher;
  private MarketplaceClient client;
  private ApplicationEventPublisher events;
  private String pinnedSha;
  private byte[] uploadedBytes;
  private String uploadedName;

  @BeforeEach
  void setUp() {
    pluginBox = mock(Pf4jPluginBox.class);
    repository = mock(IntegrationTypeRepository.class);
    fetcher = mock(MarketplaceArtifactFetcher.class);
    client = mock(MarketplaceClient.class);
    events = mock(ApplicationEventPublisher.class);
    pinnedSha = sha256(PINNED_JAR);
    uploadedBytes = null;
    uploadedName = null;

    when(client.registryHost()).thenReturn("marketplace.example.test");
    when(repository.findByName("junit")).thenReturn(Optional.empty());
    when(repository.findAll()).thenReturn(List.of());
    when(repository.save(any(IntegrationType.class))).thenAnswer(inv -> inv.getArgument(0));
    when(pluginBox.uploadPlugin(anyString(), any(InputStream.class))).thenAnswer(inv -> {
      uploadedName = inv.getArgument(0);
      uploadedBytes = inv.getArgument(1, InputStream.class).readAllBytes();
      var type = new IntegrationType();
      type.setId(5L);
      type.setName("junit");
      var details = new IntegrationTypeDetails();
      details.setDetails(new HashMap<>());
      type.setDetails(details);
      return type;
    });
    serves(PINNED_URL, PINNED_JAR);
    serves(CDN_URL, REGISTRY_JAR);
  }

  // ---- fixtures ------------------------------------------------------------------------------

  private void serves(String url, byte[] bytes) {
    doAnswer(invocation -> {
      Files.write(invocation.<Path>getArgument(1), bytes);
      return null;
    }).when(fetcher).fetch(eq(url), any(Path.class));
  }

  private InstallMarketplacePluginHandlerImpl installer(String productVersion) {
    var handler = new InstallMarketplacePluginHandlerImpl(client, fetcher,
        new ProductVersion(productVersion), mock(MarketplaceLicence.class), pluginBox, repository,
        events);
    ReflectionTestUtils.setField(handler, "pluginsTempPath", temp.toString());
    return handler;
  }

  private PluginStartUpService service(boolean load, boolean autoUpdate, String productVersion) {
    return new PluginStartUpService(pluginBox, repository, fetcher, client,
        installer(productVersion), new ProductVersion(productVersion), load, autoUpdate, "junit",
        "junit", "6.0.0", PINNED_URL, pinnedSha, temp.toString());
  }

  private PluginStartUpService service() {
    return service(true, true, "25.2");
  }

  private PluginStartUpService pinnedOnly(String url, String checksum) {
    return new PluginStartUpService(pluginBox, repository, fetcher, client, installer("25.2"),
        new ProductVersion("25.2"), true, false, "junit", "junit", "6.0.0", url, checksum,
        temp.toString());
  }

  private void installed(String version) {
    var type = new IntegrationType();
    type.setName("junit");
    var details = new IntegrationTypeDetails();
    var map = new HashMap<String, Object>();
    map.put("version", version);
    details.setDetails(map);
    type.setDetails(details);
    when(repository.findByName("junit")).thenReturn(Optional.of(type));
  }

  private void registryLatest(String version, String sha256) {
    when(client.getPlugin("junit")).thenReturn(
        new MarketplacePluginDetail("junit", "JUnit import", version, null, null, null, null, null,
            null, "public", null, "official", version));
    when(client.getVersion("junit", version)).thenReturn(
        new MarketplaceVersionDetail("junit", "JUnit import", version, null, null, null, null,
            new MarketplaceCompatibility(">=25.1"), null, "public", null, "official", false, null,
            null, null, sha256, null, List.of()));
    when(client.resolveArtifact(eq("junit"), eq(version), any()))
        .thenReturn(new MarketplaceArtifact(CDN_URL, null));
  }

  private void registryUnreachable() {
    when(client.getPlugin("junit")).thenThrow(
        new RegistryUnreachableException("marketplace.example.test",
            new UnknownHostException("marketplace.example.test")));
  }

  private static String sha256(byte[] bytes) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private void assertNoLeftovers() throws IOException {
    try (Stream<Path> files = Files.list(temp)) {
      assertEquals(0, files.count(), "staged downloads must be deleted");
    }
  }

  private void assertNothingInstalled() {
    verify(pluginBox, never()).uploadPlugin(anyString(), any());
  }

  // ---- registry first -----------------------------------------------------------------------

  @Test
  void registryNewerThanInstalledIsInstalledThroughTheMarketplacePath() throws IOException {
    installed("6.0.0");
    registryLatest("6.1.0", sha256(REGISTRY_JAR));

    service().loadPlugins();

    verify(client).getVersion("junit", "6.1.0");
    verify(fetcher).fetch(eq(CDN_URL), any(Path.class));
    verify(fetcher, never()).fetch(eq(PINNED_URL), any(Path.class));
    assertEquals("junit-6.1.0.jar", uploadedName);
    assertArrayEquals(REGISTRY_JAR, uploadedBytes);
    // the marketplace path records where it came from, and the event names no user
    var saved = ArgumentCaptor.forClass(IntegrationType.class);
    verify(repository).save(saved.capture());
    assertEquals("junit", saved.getValue().getDetails().getDetails().get("marketplacePluginId"));
    var event = ArgumentCaptor.forClass(PluginUploadedEvent.class);
    verify(events).publishEvent(event.capture());
    assertTrue(event.getValue().isSystemEvent());
    assertNoLeftovers();
  }

  @Test
  void freshInstanceGetsTheRegistryVersionNotThePin() {
    registryLatest("6.1.0", sha256(REGISTRY_JAR));

    service().loadPlugins();

    assertArrayEquals(REGISTRY_JAR, uploadedBytes);
    verify(fetcher, never()).fetch(eq(PINNED_URL), any(Path.class));
  }

  @Test
  void registrySameVersionChangesNothing() {
    installed("6.1.0");
    registryLatest("6.1.0", sha256(REGISTRY_JAR));

    service().loadPlugins();

    verify(client, never()).getVersion(anyString(), anyString());
    verifyNoInteractions(fetcher);
    assertNothingInstalled();
  }

  @Test
  void registryOlderVersionNeverDowngrades() {
    installed("7.0.0");
    registryLatest("6.1.0", sha256(REGISTRY_JAR));

    service().loadPlugins();

    verify(client, never()).getVersion(anyString(), anyString());
    verifyNoInteractions(fetcher);
    assertNothingInstalled();
  }

  @Test
  void registryUnreachableAndPluginAbsentFallsBackToThePin() throws IOException {
    registryUnreachable();

    service().loadPlugins();

    verify(fetcher).fetch(eq(PINNED_URL), any(Path.class));
    assertEquals("junit-6.0.0.jar", uploadedName);
    assertArrayEquals(PINNED_JAR, uploadedBytes);
    assertNoLeftovers();
  }

  @Test
  void registryUnreachableAndPluginInstalledDoesNothing() {
    installed("6.0.0");
    registryUnreachable();

    assertDoesNotThrow(() -> service().loadPlugins());

    verifyNoInteractions(fetcher);
    assertNothingInstalled();
  }

  @Test
  void registryWithoutThePluginFallsBackToThePin() {
    when(client.getPlugin("junit"))
        .thenThrow(new RegistryNotFoundException("junit", null, "not_found", "no such plugin"));

    service().loadPlugins();

    assertArrayEquals(PINNED_JAR, uploadedBytes);
  }

  @Test
  void registryVersionWithoutChecksumIsNotInstalledAndThePinIsUsed() throws IOException {
    registryLatest("6.1.0", null);

    service().loadPlugins();

    // the registry jar was refused before it reached the loader; the pin went in instead
    verify(pluginBox, never()).uploadPlugin(eq("junit-6.1.0.jar"), any());
    assertEquals("junit-6.0.0.jar", uploadedName);
    assertArrayEquals(PINNED_JAR, uploadedBytes);
    assertNoLeftovers();
  }

  @Test
  void registryVersionWithoutChecksumLeavesAnInstalledPluginAlone() {
    installed("6.0.0");
    registryLatest("6.1.0", null);

    service().loadPlugins();

    assertNothingInstalled();
  }

  @Test
  void registryChecksumMismatchIsNotInstalled() {
    installed("6.0.0");
    registryLatest("6.1.0", sha256("something else".getBytes(StandardCharsets.UTF_8)));

    service().loadPlugins();

    assertNothingInstalled();
  }

  @Test
  void incompatibleRegistryVersionIsNotInstalled() {
    installed("6.0.0");
    registryLatest("6.1.0", sha256(REGISTRY_JAR));
    when(client.getVersion("junit", "6.1.0")).thenReturn(
        new MarketplaceVersionDetail("junit", "JUnit import", "6.1.0", null, null, null, null,
            new MarketplaceCompatibility(">=99.0"), null, "public", null, "official", false, null,
            null, null, sha256(REGISTRY_JAR), null, List.of()));

    service().loadPlugins();

    verify(fetcher, never()).fetch(eq(CDN_URL), any(Path.class));
    assertNothingInstalled();
  }

  @Test
  void unknownProductVersionDoesNotAskTheRegistry() {
    service(true, true, "").loadPlugins();

    verifyNoInteractions(client);
    assertArrayEquals(PINNED_JAR, uploadedBytes);
  }

  // ---- switches -----------------------------------------------------------------------------

  @Test
  void autoUpdateOffUsesOnlyThePin() {
    registryLatest("6.1.0", sha256(REGISTRY_JAR));

    service(true, false, "25.2").loadPlugins();

    verifyNoInteractions(client);
    assertArrayEquals(PINNED_JAR, uploadedBytes);
  }

  @Test
  void loadOffDoesNothingAtAll() {
    registryLatest("6.1.0", sha256(REGISTRY_JAR));

    service(false, true, "25.2").loadPlugins();

    verify(pluginBox).startUp();
    verifyNoInteractions(client, fetcher, repository, events);
    assertNothingInstalled();
  }

  // ---- nothing escapes ----------------------------------------------------------------------

  @Test
  void exceptionsNeverLeaveTheListener() {
    doThrow(new IllegalStateException("loader exploded")).when(pluginBox).startUp();
    when(repository.findByName("junit")).thenThrow(new IllegalStateException("db down"));
    when(client.getPlugin("junit")).thenThrow(new IllegalStateException("boom"));

    assertDoesNotThrow(() -> service().loadPlugins());
  }

  @Test
  void loaderFailureOnTheRegistryJarDoesNotEscapeAndFallsBack() {
    registryLatest("6.1.0", sha256(REGISTRY_JAR));
    doThrow(new NoClassDefFoundError("com/epam/ta/reportportal/Old"))
        .when(pluginBox).uploadPlugin(eq("junit-6.1.0.jar"), any(InputStream.class));

    assertDoesNotThrow(() -> service().loadPlugins());

    verify(pluginBox).uploadPlugin(eq("junit-6.0.0.jar"), any(InputStream.class));
  }

  // ---- the pinned path on its own (auto-update off) -----------------------------------------

  @Test
  void pinnedMatchingChecksumInstalls() throws IOException {
    pinnedOnly(PINNED_URL, pinnedSha).loadPlugins();

    verify(fetcher).fetch(eq(PINNED_URL), any(Path.class));
    assertEquals("junit-6.0.0.jar", uploadedName);
    assertNoLeftovers();
  }

  @Test
  void pinnedUpperCaseChecksumIsAccepted() {
    pinnedOnly(PINNED_URL, pinnedSha.toUpperCase()).loadPlugins();

    assertEquals("junit-6.0.0.jar", uploadedName);
  }

  @Test
  void pinnedMismatchDoesNotInstall() throws IOException {
    serves(PINNED_URL, "tampered".getBytes(StandardCharsets.UTF_8));

    assertDoesNotThrow(() -> pinnedOnly(PINNED_URL, pinnedSha).loadPlugins());

    assertNothingInstalled();
    assertNoLeftovers();
  }

  @Test
  void pinnedNonHttpsUrlIsRefusedBeforeAnyDownload() {
    for (var url : new String[] {"http://example.test/p.jar", "file:///etc/passwd",
        "ftp://example.test/p.jar", "", "not a url"}) {
      assertDoesNotThrow(() -> pinnedOnly(url, pinnedSha).loadPlugins());
    }

    verifyNoInteractions(fetcher);
    assertNothingInstalled();
  }

  @Test
  void pinnedMissingChecksumIsRefusedBeforeAnyDownload() {
    pinnedOnly(PINNED_URL, "").loadPlugins();

    verifyNoInteractions(fetcher);
    assertNothingInstalled();
  }

  @Test
  void pinnedSameOrNewerInstalledIsLeftAlone() {
    installed("6.0.0");
    pinnedOnly(PINNED_URL, pinnedSha).loadPlugins();
    installed("6.1.0");
    pinnedOnly(PINNED_URL, pinnedSha).loadPlugins();

    verifyNoInteractions(fetcher);
    assertNothingInstalled();
  }

  @Test
  void pinnedUpgradesAnOlderInstall() {
    installed("1.1.2");

    pinnedOnly(PINNED_URL, pinnedSha).loadPlugins();

    assertEquals("junit-6.0.0.jar", uploadedName);
  }

  @Test
  void pinnedDownloadFailureDoesNotFailStartup() throws IOException {
    doThrow(new RegistryUnreachableException("example.test", new IOException("offline")))
        .when(fetcher).fetch(eq(PINNED_URL), any(Path.class));

    assertDoesNotThrow(() -> pinnedOnly(PINNED_URL, pinnedSha).loadPlugins());

    assertNothingInstalled();
    assertNoLeftovers();
  }
}
