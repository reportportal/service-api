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

package com.epam.reportportal.base.core.marketplace;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Where a credential may go. The licence JWT and a signed artifact URL are both bearer
 * credentials, and both were being sent wherever the registry URL happened to point.
 */
class CredentialTransportTest {

  private static final CredentialTransport STRICT = new CredentialTransport(false);
  private static final CredentialTransport PERMISSIVE = new CredentialTransport(true);

  @Test
  void httpsCarriesACredential() {
    assertTrue(STRICT.permits("https://marketplace.reportportal.io/api/v1/plugins"));
    assertTrue(STRICT.permits("HTTPS://MARKETPLACE.REPORTPORTAL.IO"));
  }

  @Test
  void plainHttpToARemoteHostDoesNot() {
    assertFalse(STRICT.permits("http://marketplace.reportportal.io/api/v1/plugins"));
    assertFalse(STRICT.permits("http://marketplace.local/cdn/plugin.jar"));
  }

  /**
   * Loopback is the shape every developer and every integration test in this repository uses.
   * Refusing it would mean the strict default could never be the one under test, and there is no
   * network on loopback for anything to read the credential off.
   */
  @Test
  void plainHttpToLoopbackDoes() {
    assertTrue(STRICT.permits("http://localhost:8080/api/v1/plugins"));
    assertTrue(STRICT.permits("http://127.0.0.1:9000/artifact"));
    assertTrue(STRICT.permits("http://[::1]:8080/artifact"));
  }

  @Test
  void anOperatorCanAllowPlainHttpDeliberately() {
    // an in-cluster registry on a trusted network, which is why the escape hatch exists at all
    assertTrue(PERMISSIVE.permits("http://service-marketplace:8080/api/v1/plugins"));
  }

  /** Even then, only for HTTP: nothing turns on a scheme nobody vetted. */
  @Test
  void theEscapeHatchDoesNotOpenEveryScheme() {
    assertFalse(PERMISSIVE.permits("ftp://marketplace.reportportal.io/plugin.jar"));
    assertFalse(PERMISSIVE.permits("file:///etc/passwd"));
  }

  @Test
  void aUrlThatCannotBeReadIsNotGuessedAt() {
    assertFalse(STRICT.permits(null));
    assertFalse(STRICT.permits(""));
    assertFalse(STRICT.permits("   "));
    assertFalse(STRICT.permits("not a url at all"));
  }

  @Test
  void theRefusalSaysWhatToDoAboutIt() {
    var message = CredentialTransport.refusal("the marketplace licence", "http://registry.internal");

    assertTrue(message.contains("http://registry.internal"));
    assertTrue(message.contains("marketplace.registry.allow-insecure-transport"));
  }
}
