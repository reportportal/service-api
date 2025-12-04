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

package com.epam.reportportal.core.events.domain;

import com.epam.reportportal.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.infrastructure.persistence.entity.enums.LaunchModeEnum;
import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Event triggered when a launch is finished.
 *
 * @author Andrei Varabyeu
 */
@Getter
@Setter
@NoArgsConstructor
public class LaunchFinishedEvent extends AbstractEvent<Void> {

  private Long id;
  private String name;
  private LaunchModeEnum mode;
  private ReportPortalUser user;
  private String baseUrl;

  public LaunchFinishedEvent(Launch launch, Long orgId) {
    super();
    this.id = launch.getId();
    this.name = launch.getName();
    this.mode = launch.getMode();
    this.projectId = launch.getProjectId();
    this.organizationId = orgId;
  }

  public LaunchFinishedEvent(Launch launch, Long userId, String userLogin, Long orgId) {
    super(userId, userLogin);
    this.id = launch.getId();
    this.name = launch.getName();
    this.mode = launch.getMode();
    this.projectId = launch.getProjectId();
    this.organizationId = orgId;
  }

  public LaunchFinishedEvent(Launch launch, Long userId, String userLogin, String baseUrl,
      Long orgId) {
    this(launch, userId, userLogin, orgId);
    this.baseUrl = baseUrl;
  }

  public LaunchFinishedEvent(Launch launch, ReportPortalUser user, String baseUrl, Long orgId) {
    this(launch, user.getUserId(), user.getUsername(), baseUrl, orgId);
    this.user = user;
  }

}
