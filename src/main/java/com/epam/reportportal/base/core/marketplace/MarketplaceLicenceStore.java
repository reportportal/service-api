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

import java.util.Optional;

/**
 * Where this instance keeps its marketplace licence credentials.
 *
 * <p>Reading the customer id is deliberately separate from reading the whole credential: the
 * catalogue asks "is a licence configured" on every request and the admin page asks who we sign
 * as, and neither has any business decrypting a private key to find out.
 */
public interface MarketplaceLicenceStore {

  /**
   * The configured customer id, or empty when this instance holds no credentials.
   *
   * @return customer id
   */
  Optional<String> customerId();

  /**
   * The full credentials, decrypted.
   *
   * @return credentials, empty when this instance holds none
   */
  Optional<MarketplaceLicenceCredentials> credentials();

  /**
   * Replaces the stored credentials.
   *
   * @param customerId customer id the registry issued the entitlement to
   * @param privateKey base64 Ed25519 private key
   */
  void save(String customerId, String privateKey);
}
