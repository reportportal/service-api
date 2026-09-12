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
 * The registry refused a premium artifact on licence grounds — 401 (no JWT) or a 403 whose body is
 * not a block notice. {@link #getFailure()} is as specific as the registry allows, no more.
 */
public class LicenceRejectedException extends RegistryResponseException {

  private final String pluginId;
  private final String version;
  private final LicenceFailure failure;

  /**
   * Creates the exception from a licence rejection.
   *
   * @param pluginId        registry plugin id
   * @param version         version whose artifact was requested
   * @param status          HTTP status the registry answered with
   * @param failure         what the registry reported, {@link LicenceFailure#UNSPECIFIED} when it
   *                        did not say
   * @param registryCode    {@code code} field of the error body, may be null
   * @param registryMessage {@code message} field of the error body, may be null
   */
  public LicenceRejectedException(String pluginId, String version, int status,
      LicenceFailure failure, String registryCode, String registryMessage) {
    super("Marketplace registry rejected the licence for '" + pluginId + ":" + version + "' ("
        + failure + ")" + (registryMessage == null ? "" : ": " + registryMessage),
        status, registryCode, registryMessage);
    this.pluginId = pluginId;
    this.version = version;
    this.failure = failure;
  }

  public String getPluginId() {
    return pluginId;
  }

  public String getVersion() {
    return version;
  }

  public LicenceFailure getFailure() {
    return failure;
  }
}
