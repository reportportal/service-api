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

package com.epam.reportportal.core.events.domain;

import com.epam.reportportal.model.activity.PluginActivityResource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Event published when a plugin is uploaded by a user or loaded by the system. Use
 * {@link #isSystemEvent()} to distinguish between user-initiated uploads (activity audit required)
 * and system lifecycle loads (startup, reload - no audit).
 */
@Setter
@Getter
@NoArgsConstructor
public class PluginUploadedEvent extends AbstractEvent<Void> {

  private PluginActivityResource pluginActivityResource;

  /**
   * Constructor for user-initiated plugin upload (activity audit required).
   *
   * @param pluginActivityResource Plugin information
   * @param userId                 User ID who uploaded the plugin
   * @param userLogin              User login who uploaded the plugin
   */
  public PluginUploadedEvent(PluginActivityResource pluginActivityResource, Long userId,
      String userLogin) {
    super(userId, userLogin);
    this.pluginActivityResource = pluginActivityResource;
  }

  /**
   * Constructor for system-initiated plugin load (startup, reload - no activity audit).
   *
   * @param pluginActivityResource Plugin information
   */
  public PluginUploadedEvent(PluginActivityResource pluginActivityResource) {
    super();
    this.pluginActivityResource = pluginActivityResource;
  }
}
