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

package com.epam.reportportal.core.events.activity.item;

import com.epam.reportportal.core.events.ProjectIdAwareEvent;
import com.epam.reportportal.infrastructure.persistence.entity.item.TestItem;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class TestItemFinishedEvent implements ProjectIdAwareEvent {

  private final TestItem testItem;

  private final Long projectId;

  public TestItemFinishedEvent(TestItem testItem, Long projectId) {
    this.testItem = testItem;
    this.projectId = projectId;
  }

  public TestItem getTestItem() {
    return testItem;
  }

  @Override
  public Long projectId() {
    return projectId;
  }
}
