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
import static com.epam.ta.reportportal.entity.activity.ActivityAction.UNASSIGN_USER;

import com.epam.ta.reportportal.builder.ActivityBuilder;
import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.EventAction;
import com.epam.ta.reportportal.entity.activity.EventObject;
import com.epam.ta.reportportal.entity.activity.EventPriority;
import com.epam.ta.reportportal.entity.activity.EventSubject;
import com.epam.ta.reportportal.model.activity.UserActivityResource;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UnassignUserEvent extends AbstractEvent implements ActivityEvent {

  private UserActivityResource userActivityResource;
  private boolean isSystemEvent;
  private Long orgId;

  public UnassignUserEvent() {
  }

  public UnassignUserEvent(UserActivityResource userActivityResource, Long userId,
      String userLogin, Long orgId) {
    this(userActivityResource, userId, userLogin, false);
    this.orgId = orgId;
  }

  public UnassignUserEvent(UserActivityResource userActivityResource) {
    this(userActivityResource, null, null, true);
  }

  public UnassignUserEvent(UserActivityResource userActivityResource, Long userId, String userLogin,
      boolean isSystemEvent) {
    super(userId, userLogin);
    this.userActivityResource = userActivityResource;
    this.isSystemEvent = isSystemEvent;
  }

  @Override
  public Activity toActivity() {
    return new ActivityBuilder().addCreatedNow()
        .addAction(EventAction.UNASSIGN)
        .addEventName(UNASSIGN_USER.getValue())
        .addPriority(EventPriority.MEDIUM)
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
