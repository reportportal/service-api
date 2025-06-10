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

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.RP_SUBJECT_NAME;

import com.epam.ta.reportportal.builder.ActivityBuilder;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.core.events.ProjectIdAwareEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityAction;
import com.epam.ta.reportportal.entity.activity.EventAction;
import com.epam.ta.reportportal.entity.activity.EventObject;
import com.epam.ta.reportportal.entity.activity.EventPriority;
import com.epam.ta.reportportal.entity.activity.EventSubject;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import lombok.Getter;
import lombok.Setter;

/**
 * Lifecycle events.
 *
 * @author Andrei Varabyeu
 */

@Getter
@Setter
public class LaunchFinishedEvent extends AbstractEvent implements ActivityEvent,
    ProjectIdAwareEvent {

  private Long id;
  private String name;
  private LaunchModeEnum mode;

  private Long projectId;

  private ReportPortalUser user;

  private String baseUrl;
  private Long orgId;

  private final boolean isSystemEvent;

  public LaunchFinishedEvent(Launch launch, Long orgId) {
    this(launch, null, null, true, orgId);
    this.id = launch.getId();
    this.name = launch.getName();
    this.mode = launch.getMode();
    this.projectId = launch.getProjectId();
  }

  public LaunchFinishedEvent(Launch launch, Long userId, String userLogin, boolean isSystemEvent, Long orgId) {
    super(userId, userLogin);
    this.id = launch.getId();
    this.name = launch.getName();
    this.mode = launch.getMode();
    this.projectId = launch.getProjectId();
    this.isSystemEvent = isSystemEvent;
    this.orgId = orgId;
  }

  public LaunchFinishedEvent(Launch launch, Long userId, String userLogin, String baseUrl, Long orgId) {
    this(launch, userId, userLogin, false, orgId);
    this.baseUrl = baseUrl;
  }

  public LaunchFinishedEvent(Launch launch, ReportPortalUser user, String baseUrl, Long orgId) {
    this(launch, user.getUserId(), user.getUsername(), false, orgId);
    this.user = user;
    this.baseUrl = baseUrl;
  }

  @Override
  public Activity toActivity() {
    return new ActivityBuilder()
        .addCreatedNow()
        .addAction(EventAction.FINISH)
        .addEventName(ActivityAction.FINISH_LAUNCH.getValue())
        .addPriority(EventPriority.LOW)
        .addObjectId(id)
        .addObjectName(name)
        .addObjectType(EventObject.LAUNCH)
        .addProjectId(projectId)
        .addOrganizationId(orgId)
        .addSubjectId(isSystemEvent ? null : getUserId())
        .addSubjectName(isSystemEvent ? RP_SUBJECT_NAME : getUserLogin())
        .addSubjectType(isSystemEvent ? EventSubject.APPLICATION : EventSubject.USER)
        .get();
  }

  @Override
  public Long projectId() {
    return projectId;
  }
}
