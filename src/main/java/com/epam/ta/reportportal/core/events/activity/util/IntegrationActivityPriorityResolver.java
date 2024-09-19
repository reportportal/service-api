/*
 * Copyright 2023 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.events.activity.util;

import com.epam.ta.reportportal.entity.activity.EventPriority;
import com.epam.ta.reportportal.model.activity.IntegrationActivityResource;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class IntegrationActivityPriorityResolver {

  private IntegrationActivityPriorityResolver() {

  }

  /**
   * Resolves priority depending on integration type. Global integrations detected by the project id
   * absence.
   *
   * @param activity current event
   * @return {@link EventPriority#HIGH} for global integration if project id is absent. {@link
   * EventPriority#MEDIUM} for project integration
   */
  public static EventPriority resolvePriority(IntegrationActivityResource activity) {
    return activity.getProjectId() == null ? EventPriority.HIGH : EventPriority.MEDIUM;

  }
}
