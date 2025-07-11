/*
 * Copyright 2023 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.epam.ta.reportportal.core.events.activity;

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.RP_SUBJECT_NAME;
import static com.epam.ta.reportportal.entity.activity.ActivityAction.ASSIGN_USER;

import com.epam.ta.reportportal.builder.ActivityBuilder;
import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.EventAction;
import com.epam.ta.reportportal.entity.activity.EventObject;
import com.epam.ta.reportportal.entity.activity.EventPriority;
import com.epam.ta.reportportal.entity.activity.EventSubject;
import com.epam.ta.reportportal.model.activity.UserActivityResource;

public class AssignUserEvent extends AbstractEvent implements ActivityEvent {


  private UserActivityResource userActivityResource;

  private final boolean isSystemEvent;
  private Long orgId;


  public AssignUserEvent(UserActivityResource userActivityResource, Long userId, String userLogin,
      boolean isSystemEvent, Long orgId) {
    super(userId, userLogin);
    this.userActivityResource = userActivityResource;
    this.isSystemEvent = isSystemEvent;
    this.orgId = orgId;
  }

  @Override
  public Activity toActivity() {
    return new ActivityBuilder()
        .addCreatedNow()
        .addAction(EventAction.ASSIGN)
        .addEventName(ASSIGN_USER.getValue())
        .addPriority(EventPriority.HIGH)
        .addObjectId(userActivityResource.getId())
        .addObjectName(userActivityResource.getFullName())
        .addObjectType(EventObject.USER)
        .addProjectId(userActivityResource.getDefaultProjectId())
        .addOrganizationId(orgId)
        .addSubjectId(isSystemEvent ? null : getUserId())
        .addSubjectName(isSystemEvent ? RP_SUBJECT_NAME : getUserLogin())
        .addSubjectType(isSystemEvent ? EventSubject.APPLICATION : EventSubject.USER).get();
  }

}
