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

package com.epam.ta.reportportal.core.events.activity;

import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.EventAction;
import com.epam.ta.reportportal.entity.activity.EventObject;
import com.epam.ta.reportportal.entity.activity.EventPriority;
import com.epam.ta.reportportal.entity.activity.EventSubject;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class ChangeUserTypeEvent extends AbstractEvent implements ActivityEvent {

  private Long userId;
  private String userName;

  private UserRole oldType;
  private UserRole newType;

  public ChangeUserTypeEvent() {
  }

  public ChangeUserTypeEvent(Long userId, String userName, UserRole oldRole, UserRole newRole,
      Long editorId, String editorUsername) {
    super(editorId, editorUsername);
    this.userId = userId;
    this.userName = userName;
    this.oldType = oldRole;
    this.newType = newRole;
  }


  @Override
  public Activity toActivity() {
    return new ActivityBuilder()
        .addCreatedNow()
        .addAction(EventAction.UPDATE_USER_ROLE)
        .addEventName(EventAction.UPDATE_USER_ROLE.getValue())
        .addPriority(UserRole.ADMINISTRATOR.equals(newType) ? EventPriority.CRITICAL : EventPriority.HIGH)
        .addObjectId(userId)
        .addObjectName(userName)
        .addObjectType(EventObject.USER)
        .addSubjectId(getUserId())
        .addSubjectName(getUserLogin())
        .addSubjectType(EventSubject.USER)
        .addHistoryField("userRole", oldType.getAuthority(), newType.getAuthority())
        .get();
  }
}
