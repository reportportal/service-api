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

import com.epam.reportportal.model.activity.LaunchActivityResource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Event triggered when a launch is started. Uses {@code after} field to hold the started launch
 * resource (before=null for CREATE events).
 *
 * @author Andrei Varabyeu
 */
@Setter
@Getter
@NoArgsConstructor
public class LaunchStartedEvent extends AbstractEvent<LaunchActivityResource> {

  public LaunchStartedEvent(LaunchActivityResource launchActivityResource, Long userId,
      String userLogin, Long orgId) {
    super(userId, userLogin);
    this.after = launchActivityResource;
    this.organizationId = orgId;
  }

  /**
   * Convenience method to get the started launch resource. Equivalent to {@code getAfter()}.
   */
  public LaunchActivityResource getLaunchActivityResource() {
    return getAfter();
  }
}
