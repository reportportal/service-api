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

package com.epam.reportportal.base.core.marketplace.exception;

/**
 * Why the registry rejected a licence.
 *
 * <p>Today the registry cannot tell unknown customer, bad signature, expired entitlement and
 * wrong-plugin apart — all four are one 403 "Invalid license", which maps to {@link #UNSPECIFIED}.
 * The specific constants exist for the codes the registry may start sending; do not invent a
 * distinction it cannot make.
 */
public enum LicenceFailure {

  /** No licence JWT was presented (HTTP 401). */
  MISSING,
  /** Registry said the token itself is not valid. */
  INVALID,
  /** Registry said the entitlement has expired. */
  EXPIRED,
  /** Registry said the entitlement does not cover this plugin. */
  ENTITLEMENT_DENIED,
  /** Registry rejected the licence without saying why. */
  UNSPECIFIED;

  /**
   * Maps a registry error code to a failure, falling back to status-only mapping when the code is
   * absent or generic. Merging the registry's licence-error-code amendment needs no change here
   * beyond new cases.
   *
   * @param registryCode {@code code} field of the error body, may be null
   * @param status       HTTP status of the response
   * @return the failure the registry actually reported
   */
  public static LicenceFailure from(String registryCode, int status) {
    return switch (registryCode == null ? "" : registryCode) {
      case "LICENSE_JWT_MISSING" -> MISSING;
      case "LICENSE_JWT_INVALID" -> INVALID;
      case "LICENSE_EXPIRED" -> EXPIRED;
      case "LICENSE_ENTITLEMENT_DENIED" -> ENTITLEMENT_DENIED;
      default -> status == 401 ? MISSING : UNSPECIFIED;
    };
  }
}
