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

import com.epam.ta.reportportal.builder.ActivityBuilder;
import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityAction;
import com.epam.ta.reportportal.entity.activity.EventAction;
import com.epam.ta.reportportal.entity.activity.EventObject;
import com.epam.ta.reportportal.entity.activity.EventPriority;
import com.epam.ta.reportportal.entity.activity.EventSubject;
import java.util.Objects;

/**
 * Event publish when project is deleted.
 *
 * @author Ryhor_Kukharenka
 */
public class ProjectDeletedEvent extends AbstractEvent implements ActivityEvent {

  private final Long projectId;
  private final String projectName;

  public ProjectDeletedEvent(Long userId, String userLogin, Long projectId, String projectName) {
    super(userId, userLogin);
    this.projectId = projectId;
    this.projectName = projectName;
  }

  @Override
  public Activity toActivity() {
    return new ActivityBuilder()
        .addCreatedNow()
        .addAction(EventAction.DELETE)
        .addEventName(ActivityAction.DELETE_PROJECT.getValue())
        .addPriority(EventPriority.CRITICAL)
        .addObjectId(projectId)
        .addObjectName(projectName)
        .addObjectType(EventObject.PROJECT)
        .addSubjectId(getUserId())
        .addSubjectType(Objects.isNull(getUserId()) ? EventSubject.APPLICATION : EventSubject.USER)
        .addSubjectName(getUserLogin())
        .get();
  }

}
