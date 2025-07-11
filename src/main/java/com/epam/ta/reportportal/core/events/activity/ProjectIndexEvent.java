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

package com.epam.ta.reportportal.core.events.activity;

import com.epam.ta.reportportal.builder.ActivityBuilder;
import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityAction;
import com.epam.ta.reportportal.entity.activity.EventAction;
import com.epam.ta.reportportal.entity.activity.EventObject;
import com.epam.ta.reportportal.entity.activity.EventPriority;
import com.epam.ta.reportportal.entity.activity.EventSubject;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Pavel Bortnik
 */
@Setter
@Getter
public class ProjectIndexEvent extends AbstractEvent implements ActivityEvent {

  private Long projectId;
  private String projectName;
  private boolean indexing;
  private Long orgId;


  public ProjectIndexEvent() {
  }

  public ProjectIndexEvent(Long userId, String userLogin, Long projectId, String projectName,
      boolean indexing, Long orgId) {
    super(userId, userLogin);
    this.projectId = projectId;
    this.projectName = projectName;
    this.indexing = indexing;
    this.orgId = orgId;
  }

  @Override
  public Activity toActivity() {
    return new ActivityBuilder().addCreatedNow()
        .addAction(indexing ? EventAction.GENERATE : EventAction.DELETE)
        .addEventName(indexing
            ? ActivityAction.GENERATE_INDEX.getValue()
            : ActivityAction.DELETE_INDEX.getValue())
        .addPriority(indexing ? EventPriority.LOW : EventPriority.MEDIUM)
        .addObjectId(projectId)
        .addObjectName(StringUtils.EMPTY)
        .addObjectType(EventObject.INDEX)
        .addProjectId(projectId)
        .addOrganizationId(orgId)
        .addSubjectId(getUserId())
        .addSubjectName(getUserLogin())
        .addSubjectType(EventSubject.USER)
        .get();
  }
}
