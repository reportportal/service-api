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

package com.epam.reportportal.core.events.activity;

import com.epam.reportportal.core.events.ActivityEvent;
import com.epam.reportportal.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventSubject;
import java.util.Objects;
import lombok.Getter;

/**
 * Event publish when project is deleted.
 *
 * @author Ryhor_Kukharenka
 */
@Getter
public class ProjectDeletedEvent extends AbstractEvent implements ActivityEvent {

  private final Long projectId;
  private final String projectName;

  private final Long organizationId;

  public ProjectDeletedEvent(Long userId, String userLogin, Long projectId, String projectName,
      Long organizationId) {
    super(userId, userLogin);
    this.projectId = projectId;
    this.projectName = projectName;
    this.organizationId = organizationId;
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
        .addOrganizationId(organizationId)
        .addSubjectName(getUserLogin())
        .get();
  }

}
