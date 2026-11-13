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

package com.epam.reportportal.base.core.events.domain;

import com.epam.reportportal.base.model.settings.ServerSettingsResource;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Event published when server settings are updated.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Getter
@NoArgsConstructor
public class SettingsUpdatedEvent extends AbstractEvent<ServerSettingsResource> {

  /**
   * Constructs a SettingsUpdatedEvent.
   *
   * @param before    The server settings state before the update
   * @param after     The server settings state after the update
   * @param userId    The ID of the user who updated the settings
   * @param userLogin The login of the user who updated the settings
   */
  public SettingsUpdatedEvent(ServerSettingsResource before, ServerSettingsResource after,
      Long userId,
      String userLogin) {
    super(userId, userLogin, before, after);
  }
}
