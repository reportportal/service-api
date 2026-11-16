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

package com.epam.reportportal.base.core.events.domain.item;

import com.epam.reportportal.base.core.events.domain.AbstractEvent;
import com.epam.reportportal.base.model.activity.TestItemActivityResource;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Event triggered when a test item status is changed.
 *
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Getter
@NoArgsConstructor
public class TestItemStatusChangedEvent extends AbstractEvent<TestItemActivityResource> {

  /**
   * Constructs a TestItemStatusChangedEvent.
   *
   * @param before         The test item state before the status change
   * @param after          The test item state after the status change
   * @param userId         The ID of the user who changed the status
   * @param userLogin      The login of the user who changed the status
   * @param organizationId The organization ID
   */
  public TestItemStatusChangedEvent(TestItemActivityResource before, TestItemActivityResource after,
      Long userId, String userLogin, Long organizationId) {
    super(userId, userLogin, before, after);
    this.projectId = after.getProjectId();
    this.organizationId = organizationId;
  }
}
