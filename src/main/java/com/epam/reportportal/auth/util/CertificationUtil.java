/*
 * Copyright 2019 EPAM Systems
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Optional;

/**
 * Utility class for loading certificates and keys from JKS key stores.
 *
 * <p>The key store location is resolved as follows:
 * <ul>
 *   <li>{@code file:} URL (e.g. {@code file:///etc/secret/keystore.jks}) - always a filesystem file;</li>
 *   <li>absolute path - a filesystem file if it exists, otherwise a classpath resource;</li>
 *   <li>relative path - a classpath resource (e.g. {@code saml/keystore.jks}) if it exists, otherwise a file
 *   relative to the working directory. The bundled resource always wins over a file in the working
 *   directory.</li>
 * </ul>
 *
 * <p>Any failure is reported as {@link Error} so that it is not swallowed by {@code catch (Exception)} blocks
 * of callers and fails the initialization fast.
 *
 * @author Yevgeniy Svalukhin
 */
public class CertificationUtil {

  private static final String FILE_URL_PREFIX = "file:";
  private static final String LOCALHOST = "localhost";

  private CertificationUtil() {
  }

  /**
   * Loads an X.509 certificate from the key store.
   *
   * @param certificateAlias alias of the certificate
   * @param trustStoreName   key store location
   * @param password         key store password
   * @return certificate
   * @throws Error if the key store cannot be loaded or the certificate is missing
   */
  public static X509Certificate getCertificateByName(String certificateAlias, String trustStoreName,
      String password) {
    KeyStore keyStore = loadKeyStore(trustStoreName, password);
    try {
      Certificate cert = keyStore.getCertificate(certificateAlias);
      if (cert == null) {
        throw new Error(
            "Certificate with alias '" + certificateAlias + "' not found in keystore: " + trustStoreName);
      }
      if (cert instanceof X509Certificate x509Certificate) {
        return x509Certificate;
      }
      throw new Error(
          "Could not find a suitable x509 certificate for alias '" + certificateAlias + "' in keystore: "
              + trustStoreName);
    } catch (GeneralSecurityException e) {
      throw new Error("Error reading certificate from keystore " + trustStoreName + ": "
          + e.getMessage(), e);
    }
  }

  /**
   * Loads a private key from the key store.
   *
   * @param keyAlias   alias of the key
   * @param keyPass    key password
   * @param trustStore key store location
   * @param password   key store password
   * @return private key
   * @throws Error if the key store cannot be loaded or the private key is missing
   */
  public static PrivateKey getPrivateKey(String keyAlias, String keyPass, String trustStore,
      String password) {
    KeyStore keyStore = loadKeyStore(trustStore, password);
    try {
      Key key = keyStore.getKey(keyAlias, toCharArray(keyPass));
      if (key instanceof PrivateKey privateKey) {
        return privateKey;
      }
      throw new Error(
          "Private key with alias '" + keyAlias + "' not found in keystore: " + trustStore);
    } catch (GeneralSecurityException e) {
      throw new Error("Error reading private key from keystore " + trustStore + ": "
          + e.getMessage(), e);
    }
  }

  private static KeyStore loadKeyStore(String jksPath, String jksPassword) {
    if (jksPath == null || jksPath.isBlank()) {
      throw new Error("Keystore location is not specified");
    }
    try (InputStream is = openKeyStore(jksPath.trim())) {
      KeyStore keyStore = KeyStore.getInstance("JKS");
      keyStore.load(is, toCharArray(jksPassword));
      return keyStore;
    } catch (IOException | GeneralSecurityException e) {
      throw new Error("Error opening keystore " + jksPath + ": " + e.getMessage(), e);
    }
  }

  private static InputStream openKeyStore(String location) throws IOException {
    if (location.startsWith(FILE_URL_PREFIX)) {
      return Files.newInputStream(fileUrlToPath(location));
    }
    boolean absolute = isAbsolutePath(location);
    if (absolute) {
      Optional<Path> file = toExistingFile(location);
      if (file.isPresent()) {
        return Files.newInputStream(file.get());
      }
    }
    InputStream resource = getClassLoader().getResourceAsStream(stripLeadingSlash(location));
    if (resource != null) {
      return resource;
    }
    if (!absolute) {
      Optional<Path> file = toExistingFile(location);
      if (file.isPresent()) {
        return Files.newInputStream(file.get());
      }
    }
    throw new FileNotFoundException(
        "keystore not found on the filesystem or in the classpath. "
            + "Use an absolute path or a 'file://' URL to point to a mounted file");
  }

  /**
   * Converts a {@code file:} URL to a path. Supports {@code file:///abs/path}, {@code file:/abs/path},
   * {@code file://localhost/abs/path} (percent-encoded characters are decoded) and the legacy non-URI forms
   * {@code file://relative/path} and URLs with unencoded characters such as spaces.
   */
  private static Path fileUrlToPath(String location) {
    try {
      URI uri = new URI(location);
      String authority = uri.getRawAuthority();
      if (!uri.isOpaque() && (authority == null || LOCALHOST.equalsIgnoreCase(authority))) {
        return Paths.get(new URI("file", null, uri.getPath(), null));
      }
    } catch (URISyntaxException | IllegalArgumentException e) {
      // not a valid URI, fall back to the legacy handling below
    }
    String path = location.substring(FILE_URL_PREFIX.length());
    if (path.startsWith("//")) {
      path = path.substring(2);
      if (path.regionMatches(true, 0, LOCALHOST + "/", 0, LOCALHOST.length() + 1)) {
        path = path.substring(LOCALHOST.length());
      }
    }
    return Paths.get(path);
  }

  private static boolean isAbsolutePath(String location) {
    if (location.startsWith("/")) {
      return true;
    }
    try {
      return Paths.get(location).isAbsolute();
    } catch (InvalidPathException e) {
      return false;
    }
  }

  private static Optional<Path> toExistingFile(String location) {
    try {
      Path path = Paths.get(location);
      return Files.isRegularFile(path) ? Optional.of(path) : Optional.empty();
    } catch (InvalidPathException e) {
      return Optional.empty();
    }
  }

  private static String stripLeadingSlash(String location) {
    return location.startsWith("/") ? location.substring(1) : location;
  }

  private static ClassLoader getClassLoader() {
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    return contextClassLoader != null ? contextClassLoader : CertificationUtil.class.getClassLoader();
  }

  private static char[] toCharArray(String value) {
    return value == null ? null : value.toCharArray();
  }
}
