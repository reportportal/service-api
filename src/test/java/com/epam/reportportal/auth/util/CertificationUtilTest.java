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

package com.epam.reportportal.auth.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class CertificationUtilTest {

  private static final String RESOURCE = "saml/keystore.jks";
  private static final String ALIAS = "report-portal-sp";
  private static final String PASSWORD = "password";

  @TempDir
  static Path tempDir;

  private static Path keystoreFile;

  @BeforeAll
  static void copyKeystoreToFilesystem() throws IOException {
    keystoreFile = tempDir.resolve("custom-pki.jks");
    try (InputStream is = CertificationUtilTest.class.getClassLoader().getResourceAsStream(RESOURCE)) {
      assertNotNull(is, "Test keystore resource is missing");
      Files.copy(is, keystoreFile);
    }
  }

  static String[] locations() {
    String absolute = keystoreFile.toAbsolutePath().toString();
    return new String[]{
        RESOURCE,
        "/" + RESOURCE,
        absolute,
        "file://" + absolute,
        "file:" + absolute,
        "file://localhost" + absolute,
        keystoreFile.toUri().toString()
    };
  }

  @ParameterizedTest
  @MethodSource("locations")
  void loadsCertificateAndPrivateKey(String location) {
    assertNotNull(CertificationUtil.getCertificateByName(ALIAS, location, PASSWORD));
    assertNotNull(CertificationUtil.getPrivateKey(ALIAS, PASSWORD, location, PASSWORD));
  }

  @Test
  void loadsFromFileUrlWithPercentEncodedPath() throws IOException {
    Path dir = Files.createDirectories(tempDir.resolve("my certs"));
    Path file = Files.copy(keystoreFile, dir.resolve("ks.jks"));
    String location = file.toUri().toString();
    assertTrue(location.contains("%20"));
    assertNotNull(CertificationUtil.getCertificateByName(ALIAS, location, PASSWORD));
  }

  @Test
  void loadsFromLocalhostFileUrlWithUnencodedSpaces() throws IOException {
    Path dir = Files.createDirectories(tempDir.resolve("localhost certs"));
    Path file = Files.copy(keystoreFile, dir.resolve("ks.jks"), StandardCopyOption.REPLACE_EXISTING);
    String location = "file://localhost" + file.toAbsolutePath();
    assertTrue(location.contains(" "));
    assertNotNull(CertificationUtil.getCertificateByName(ALIAS, location, PASSWORD));
  }

  @Test
  void missingKeystoreFailsWithClearMessage() {
    String location = tempDir.resolve("missing.jks").toString();
    Error ex = assertThrows(Error.class,
        () -> CertificationUtil.getCertificateByName(ALIAS, location, PASSWORD));
    assertTrue(ex.getMessage().contains(location));
    assertTrue(ex.getMessage().contains("not found"));
  }

  @Test
  void missingAliasFailsWithClearMessage() {
    Error certEx = assertThrows(Error.class,
        () -> CertificationUtil.getCertificateByName("unknown", RESOURCE, PASSWORD));
    assertTrue(certEx.getMessage().contains("'unknown' not found"));

    Error keyEx = assertThrows(Error.class,
        () -> CertificationUtil.getPrivateKey("unknown", PASSWORD, RESOURCE, PASSWORD));
    assertTrue(keyEx.getMessage().contains("'unknown' not found"));
  }

  @Test
  void blankLocationFails() {
    assertThrows(Error.class, () -> CertificationUtil.getCertificateByName(ALIAS, " ", PASSWORD));
  }
}
