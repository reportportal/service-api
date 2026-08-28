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
 * HTTP 410 — the plugin was removed from the registry. The operator's reason is what the user is
 * shown, so it travels with the exception.
 */
public class PluginRemovedException extends RegistryResponseException {

  private final String pluginId;
  private final String removalReason;
  private final Instant removedAt;
  private final String removedBy;

  /**
   * Creates the exception from a registry tombstone.
   *
   * @param pluginId      registry plugin id that was asked for
   * @param removalReason operator-supplied reason, null when the body carried none
   * @param removedAt     when the plugin was removed, null when unknown
   * @param removedBy     operator who removed it, null when unknown
   */
  public PluginRemovedException(String pluginId, String removalReason, Instant removedAt,
      String removedBy) {
    super("Plugin '" + pluginId + "' was removed from the marketplace registry"
        + (removalReason == null ? "" : ": " + removalReason), 410, null, removalReason);
    this.pluginId = pluginId;
    this.removalReason = removalReason;
    this.removedAt = removedAt;
    this.removedBy = removedBy;
  }

  public String getPluginId() {
    return pluginId;
  }

  public String getRemovalReason() {
    return removalReason;
  }

  public Instant getRemovedAt() {
    return removedAt;
  }

  public String getRemovedBy() {
    return removedBy;
  }
}
