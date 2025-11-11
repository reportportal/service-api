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

import static com.epam.reportportal.core.events.activity.util.ActivityDetailsUtil.STATUS;

import com.epam.reportportal.core.events.ActivityEvent;
import com.epam.reportportal.core.events.activity.AroundEvent;
import com.epam.reportportal.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventSubject;
import com.epam.reportportal.model.activity.TestItemActivityResource;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class TestItemStatusChangedEvent extends AroundEvent<TestItemActivityResource> implements
    ActivityEvent {

  private Long organizationId;

  public TestItemStatusChangedEvent() {
  }

  public TestItemStatusChangedEvent(TestItemActivityResource before, TestItemActivityResource after,
      Long userId, String userLogin, Long organizationId) {
    super(userId, userLogin, before, after);
    this.organizationId = organizationId;
  }

  @Override
  public Activity toActivity() {
    return new ActivityBuilder()
        .addCreatedNow()
        .addAction(EventAction.UPDATE)
        .addEventName(ActivityAction.UPDATE_ITEM.getValue())
        .addPriority(EventPriority.LOW)
        .addObjectId(getAfter().getId())
        .addObjectName(getAfter().getName())
        .addObjectType(EventObject.ITEM_ISSUE)
        .addProjectId(getAfter().getProjectId())
        .addOrganizationId(organizationId)
        .addSubjectId(getUserId())
        .addSubjectName(getUserLogin())
        .addSubjectType(EventSubject.USER)
        .addHistoryField(STATUS, getBefore().getStatus(), getAfter().getStatus())
        .get();
  }
}
