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
 * The registry answered with a status the client cannot act on specifically (404, 5xx, ...). Also
 * the parent of the specific failures, so a caller can catch one type and still read the status.
 */
public class RegistryResponseException extends MarketplaceException {

  private final int status;
  private final String registryCode;
  private final String registryMessage;

  public RegistryResponseException(int status, String registryCode, String registryMessage) {
    this(defaultMessage(status, registryCode, registryMessage), status, registryCode,
        registryMessage);
  }

  protected RegistryResponseException(String message, int status, String registryCode,
      String registryMessage) {
    super(message);
    this.status = status;
    this.registryCode = registryCode;
    this.registryMessage = registryMessage;
  }

  private static String defaultMessage(int status, String code, String message) {
    return "Marketplace registry responded HTTP " + status
        + (code == null ? "" : " [" + code + "]")
        + (message == null ? "" : ": " + message);
  }

  public int getStatus() {
    return status;
  }

  /**
   * Registry {@code code} field, null when the body carried none.
   */
  public String getRegistryCode() {
    return registryCode;
  }

  /**
   * Registry {@code message} field, null when the body carried none.
   */
  public String getRegistryMessage() {
    return registryMessage;
  }
}
