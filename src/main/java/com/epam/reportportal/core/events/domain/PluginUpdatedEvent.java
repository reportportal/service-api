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

/**
 * Event published when a plugin is updated by a user or reloaded by the system. Use
 * {@link #isSystemEvent()} to distinguish between user-initiated updates (activity audit required)
 * and system lifecycle reloads (no audit).
 */
@Getter
@NoArgsConstructor
public class PluginUpdatedEvent extends AbstractEvent<PluginActivityResource> {

  /**
   * Constructor for user-initiated plugin update (activity audit required).
   *
   * @param userId    User ID who updated the plugin
   * @param userLogin User login who updated the plugin
   * @param before    Plugin state before update
   * @param after     Plugin state after update
   */
  public PluginUpdatedEvent(Long userId, String userLogin, PluginActivityResource before,
      PluginActivityResource after) {
    super(userId, userLogin, before, after);
  }

  /**
   * Constructor for system-initiated plugin reload (no activity audit).
   *
   * @param before Plugin state before reload
   * @param after  Plugin state after reload
   */
  public PluginUpdatedEvent(PluginActivityResource before, PluginActivityResource after) {
    super();
    this.before = before;
    this.after = after;
  }
}
