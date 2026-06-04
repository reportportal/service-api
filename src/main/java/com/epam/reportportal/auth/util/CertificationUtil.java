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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for loading certificates from trusted stores.
 *
 * @author Yevgeniy Svalukhin
 */
public class CertificationUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(CertificationUtil.class);

  public static X509Certificate getCertificateByName(String certificateAlias, String trustStoreName,
      String password) {
    try {
      KeyStore keyStore = KeyStore.getInstance("JKS");
      loadKeyStore(keyStore, trustStoreName, password);
      Certificate cert = keyStore.getCertificate(certificateAlias);
      if ("X.509".equals(cert.getType())) {
        return (X509Certificate) cert;
      }
      throw new Error(
          "Could not find a suitable x509 certificate for alias " + certificateAlias + " in "
              + trustStoreName);
    } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
      throw new Error("Error opening keystore: " + e.getCause(), e);
    }
  }

  public static PrivateKey getPrivateKey(String keyAlias, String keyPass, String trustStore,
      String password) {
    try {
      KeyStore keyStore = KeyStore.getInstance("JKS");
      loadKeyStore(keyStore, trustStore, password);
      Key key = keyStore.getKey(keyAlias, keyPass.toCharArray());
      if (key instanceof PrivateKey privateKey) {
        return privateKey;
      }
      throw new Error("Unable to find private key in store: " + trustStore);
    } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException
             | UnrecoverableKeyException e) {
      throw new Error("Error opening keystore: " + e.getCause(), e);
    }
  }

  private static void loadKeyStore(KeyStore keyStore, String jksPath, String jksPassword)
      throws IOException, NoSuchAlgorithmException, CertificateException {
    char[] password = null;
    if (jksPassword != null) {
      password = jksPassword.toCharArray();
    }
    if (jksPath.startsWith("file://")) {
      keyStore.load(Files.newInputStream(Paths.get(jksPath.replaceFirst("file://", ""))), password);
    } else {
      try (var is = ClassLoader.getSystemResourceAsStream(jksPath)) {
        keyStore.load(is, password);
      } catch (Exception e) {
        LOGGER.error("Failed to load key store", e);
        throw e;
      }

    }
  }
}
