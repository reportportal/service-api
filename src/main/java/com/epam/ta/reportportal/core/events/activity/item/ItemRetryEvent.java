/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.core.events.activity.item;

import com.epam.ta.reportportal.core.events.Event;
import java.util.List;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class ItemRetryEvent implements Event {

  private final Long projectId;

  private final Long launchId;

  private final List<Long> previousTryIds;

  private ItemRetryEvent(Long projectId, Long launchId, List<Long> previousTryIds) {
    this.projectId = projectId;
    this.launchId = launchId;
    this.previousTryIds = previousTryIds;
  }

  public Long getProjectId() {
    return projectId;
  }

  public Long getLaunchId() {
    return launchId;
  }

  public List<Long> getPreviousTryIds() {
    return previousTryIds;
  }

  public static ItemRetryEvent of(Long projectId, Long launchId, List<Long> previousTryIds) {
    return new ItemRetryEvent(projectId, launchId, previousTryIds);
  }

}
