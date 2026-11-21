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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

import com.epam.reportportal.base.core.marketplace.MarketplaceArtifactFetcher;
import com.epam.reportportal.base.core.marketplace.exception.RegistryUnreachableException;
import com.epam.reportportal.base.core.plugin.Pf4jPluginBox;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationTypeRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationTypeDetails;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PluginStartUpServiceTest {

  private static final byte[] JAR = "pretend jar".getBytes(StandardCharsets.UTF_8);
  private static final String URL = "https://example.test/plugin-junit-6.0.0.jar";

  @TempDir
  Path temp;

  private Pf4jPluginBox pluginBox;
  private IntegrationTypeRepository repository;
  private MarketplaceArtifactFetcher fetcher;
  private String sha;

  @BeforeEach
  void setUp() throws Exception {
    pluginBox = mock(Pf4jPluginBox.class);
    repository = mock(IntegrationTypeRepository.class);
    fetcher = mock(MarketplaceArtifactFetcher.class);
    sha = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(JAR));
    when(repository.findByName("junit")).thenReturn(Optional.empty());
    servesBytes(JAR);
  }

  private void servesBytes(byte[] bytes) {
    doAnswer(invocation -> {
      Files.write(invocation.<Path>getArgument(1), bytes);
      return null;
    }).when(fetcher).fetch(anyString(), any(Path.class));
  }

  private PluginStartUpService service(String url, String checksum) {
    return new PluginStartUpService(pluginBox, repository, fetcher, true, "junit", "6.0.0", url,
        checksum, temp.toString());
  }

  private void installed(String version) {
    var type = new IntegrationType();
    var details = new IntegrationTypeDetails();
    var map = new HashMap<String, Object>();
    map.put("version", version);
    details.setDetails(map);
    type.setDetails(details);
    when(repository.findByName("junit")).thenReturn(Optional.of(type));
  }

  private void assertNoLeftovers() throws IOException {
    try (Stream<Path> files = Files.list(temp)) {
      assertEquals(0, files.count(), "staged download must be deleted");
    }
  }

  @Test
  void matchingChecksumInstalls() throws IOException {
    service(URL, sha).loadPlugins();

    verify(fetcher).fetch(eq(URL), any(Path.class));
    verify(pluginBox).uploadPlugin(eq("junit-6.0.0.jar"), any());
    assertNoLeftovers();
  }

  @Test
  void upperCaseChecksumIsAccepted() {
    service(URL, sha.toUpperCase()).loadPlugins();

    verify(pluginBox).uploadPlugin(eq("junit-6.0.0.jar"), any());
  }

  @Test
  void mismatchDoesNotInstall() throws IOException {
    servesBytes("tampered".getBytes(StandardCharsets.UTF_8));

    assertDoesNotThrow(() -> service(URL, sha).loadPlugins());

    verify(pluginBox, never()).uploadPlugin(anyString(), any());
    assertNoLeftovers();
  }

  @Test
  void nonHttpsUrlIsRefusedBeforeAnyDownload() {
    for (var url : new String[] {"http://example.test/p.jar", "file:///etc/passwd",
        "ftp://example.test/p.jar", "", "not a url"}) {
      assertDoesNotThrow(() -> service(url, sha).loadPlugins());
    }

    verifyNoInteractions(fetcher);
    verify(pluginBox, never()).uploadPlugin(anyString(), any());
  }

  @Test
  void missingChecksumIsRefusedBeforeAnyDownload() {
    service(URL, "").loadPlugins();

    verifyNoInteractions(fetcher);
    verify(pluginBox, never()).uploadPlugin(anyString(), any());
  }

  @Test
  void sameVersionAlreadyInstalledIsSkipped() {
    installed("6.0.0");

    service(URL, sha).loadPlugins();

    verifyNoInteractions(fetcher);
    verify(pluginBox, never()).uploadPlugin(anyString(), any());
  }

  @Test
  void newerVersionAlreadyInstalledIsNotDowngraded() {
    installed("6.1.0");

    service(URL, sha).loadPlugins();

    verifyNoInteractions(fetcher);
    verify(pluginBox, never()).uploadPlugin(anyString(), any());
  }

  @Test
  void olderVersionIsUpgradedToThePin() {
    installed("1.1.2");

    service(URL, sha).loadPlugins();

    verify(pluginBox).uploadPlugin(eq("junit-6.0.0.jar"), any());
  }

  @Test
  void downloadFailureDoesNotFailStartup() throws IOException {
    doThrow(new RegistryUnreachableException("example.test", new IOException("offline")))
        .when(fetcher).fetch(anyString(), any(Path.class));

    assertDoesNotThrow(() -> service(URL, sha).loadPlugins());

    verify(pluginBox, never()).uploadPlugin(anyString(), any());
    assertNoLeftovers();
  }

  @Test
  void disabledDefaultLoadDownloadsNothing() {
    new PluginStartUpService(pluginBox, repository, fetcher, false, "junit", "6.0.0", URL, sha,
        temp.toString()).loadPlugins();

    verify(pluginBox).startUp();
    verifyNoInteractions(fetcher);
  }
}
