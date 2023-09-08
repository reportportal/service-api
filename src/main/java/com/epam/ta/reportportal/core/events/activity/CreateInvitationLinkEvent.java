/*
 * Copyright 2023 EPAM Systems
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

package com.epam.ta.reportportal.core.events.activity;

import com.epam.ta.reportportal.builder.ActivityBuilder;
import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.EventAction;
import com.epam.ta.reportportal.entity.activity.EventObject;
import com.epam.ta.reportportal.entity.activity.EventPriority;
import com.epam.ta.reportportal.entity.activity.EventSubject;

/**
 * Event on creation invite link.
 *
 * @author Andrei Piankouski
 */
public class CreateInvitationLinkEvent extends AbstractEvent implements ActivityEvent {

  private static final String EVENT_NAME = "createInvitationLink";

  private final Long projectId;

  public CreateInvitationLinkEvent(Long userId, String userLogin, Long projectId) {
    super(userId, userLogin);
    this.projectId = projectId;
  }

  @Override
  public Activity toActivity() {
    return new ActivityBuilder()
        .addCreatedNow()
        .addAction(EventAction.CREATE)
        .addEventName(EVENT_NAME)
        .addPriority(EventPriority.HIGH)
        .addObjectId(userId)
        .addObjectName(EventObject.INVITATION_LINK.getValue())
        .addObjectType(EventObject.INVITATION_LINK)
        .addSubjectId(getUserId())
        .addSubjectName(getUserLogin())
        .addSubjectType(EventSubject.USER)
        .addProjectId(projectId)
        .get();
  }
}
