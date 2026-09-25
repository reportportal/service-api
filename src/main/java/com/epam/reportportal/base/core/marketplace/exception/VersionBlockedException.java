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

import java.time.Instant;

/**
 * HTTP 403 whose body says {@code blocked:true} — an operator blocked this version. Distinct from
 * {@link LicenceRejectedException}, which shares the status but not the body.
 */
public class VersionBlockedException extends RegistryResponseException {

  private final String pluginId;
  private final String version;
  private final String reason;
  private final Instant blockedAt;

  /**
   * Creates the exception from a blocked-artifact body.
   *
   * @param pluginId  registry plugin id
   * @param version   blocked version
   * @param reason    operator-supplied reason, null when the body carried none
   * @param blockedAt when the version was blocked, null when unknown
   */
  public VersionBlockedException(String pluginId, String version, String reason,
      Instant blockedAt) {
    super("Version '" + version + "' of plugin '" + pluginId + "' is blocked in the marketplace"
        + " registry" + (reason == null ? "" : ": " + reason), 403, null, reason);
    this.pluginId = pluginId;
    this.version = version;
    this.reason = reason;
    this.blockedAt = blockedAt;
  }

  public String getPluginId() {
    return pluginId;
  }

  public String getVersion() {
    return version;
  }

  public String getReason() {
    return reason;
  }

  public Instant getBlockedAt() {
    return blockedAt;
  }
}
