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
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;

/**
 * Lifecycle events.
 *
 * @author Andrei Varabyeu
 */
public class LaunchFinishedEvent extends AbstractEvent implements ActivityEvent,
    ProjectIdAwareEvent {

  private Long id;
  private String name;
  private LaunchModeEnum mode;

  private Long projectId;

  private ReportPortalUser user;

  private String baseUrl;

  public LaunchFinishedEvent(Launch launch) {
    this.id = launch.getId();
    this.name = launch.getName();
    this.mode = launch.getMode();
    this.projectId = launch.getProjectId();
  }

  public LaunchFinishedEvent(Launch launch, Long userId, String userLogin) {
    super(userId, userLogin);
    this.id = launch.getId();
    this.name = launch.getName();
    this.mode = launch.getMode();
    this.projectId = launch.getProjectId();
  }

  public LaunchFinishedEvent(Launch launch, Long userId, String userLogin, String baseUrl) {
    this(launch, userId, userLogin);
    this.baseUrl = baseUrl;
  }

  public LaunchFinishedEvent(Launch launch, ReportPortalUser user, String baseUrl) {
    this(launch, user.getUserId(), user.getUsername());
    this.user = user;
    this.baseUrl = baseUrl;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public LaunchModeEnum getMode() {
    return mode;
  }

  public void setMode(LaunchModeEnum mode) {
    this.mode = mode;
  }

  @Override
  public Long getProjectId() {
    return projectId;
  }

  public void setProjectId(Long projectId) {
    this.projectId = projectId;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public ReportPortalUser getUser() {
    return user;
  }

  public void setUser(ReportPortalUser user) {
    this.user = user;
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
        .addSubjectId(getUserId())
        .addSubjectName(getUserLogin())
        .addSubjectType(EventSubject.USER)
        .get();
  }
}
