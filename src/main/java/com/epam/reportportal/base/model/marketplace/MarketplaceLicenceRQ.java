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

package com.epam.reportportal.base.model.marketplace;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * The two things the registry operator hands over when they create an entitlement.
 *
 * @param customerId customer id the entitlement was issued to
 * @param privateKey base64 Ed25519 private key; write-only, no endpoint ever gives it back
 */
public record MarketplaceLicenceRQ(
    @NotBlank @Size(max = 255) String customerId,
    @NotBlank @Size(max = 512) String privateKey) {

  /** The request object itself must not be the thing that logs the key. */
  @Override
  public String toString() {
    return "MarketplaceLicenceRQ[customerId=" + customerId + ", privateKey=***]";
  }
}
