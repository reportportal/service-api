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

/**
 * What the registry operator hands over when they create an entitlement: the customer this
 * instance signs as, and the Ed25519 private key it signs with.
 *
 * <p>The key is base64 exactly as the registry printed it — Go's {@code ed25519.PrivateKey} is 64
 * bytes (seed followed by the public key), and the shorter 32-byte seed is accepted too. It is
 * never logged and never leaves this service.
 *
 * @param customerId the entitlement's customer id, the {@code customerId} claim
 * @param privateKey base64 Ed25519 private key
 */
public record MarketplaceLicenceCredentials(String customerId, String privateKey) {

  /** Keeps the key out of logs and of anything that string-formats a credential by accident. */
  @Override
  public String toString() {
    return "MarketplaceLicenceCredentials[customerId=" + customerId + ", privateKey=***]";
  }
}
