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

package com.epam.reportportal.core.project.impl;

import com.epam.reportportal.core.events.activity.AssignUserEvent;
import com.epam.reportportal.core.project.ProjectUserHandler;
import com.epam.reportportal.infrastructure.persistence.dao.ProjectUserRepository;
import com.epam.reportportal.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.infrastructure.persistence.entity.user.ProjectUser;
import com.epam.reportportal.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.model.activity.UserActivityResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class ProjectUserHandlerImpl implements ProjectUserHandler {

  private final ApplicationEventPublisher eventPublisher;
  private final ProjectUserRepository projectUserRepository;

  @Autowired
  public ProjectUserHandlerImpl(ApplicationEventPublisher eventPublisher,
      ProjectUserRepository projectUserRepository) {
    this.projectUserRepository = projectUserRepository;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public ProjectUser assign(User user, Project project, ProjectRole projectRole, User creator,
      boolean isSystemEvent) {
    final ProjectUser projectUser =
        new ProjectUser().withProjectRole(projectRole).withUser(user).withProject(project);
    projectUserRepository.save(projectUser);

    AssignUserEvent assignUserEvent =
        new AssignUserEvent(getUserActivityResource(user, project), creator.getId(),
            creator.getLogin(), isSystemEvent, project.getOrganizationId()
        );
    eventPublisher.publishEvent(assignUserEvent);

    return projectUser;
  }

  private UserActivityResource getUserActivityResource(User user, Project project) {
    UserActivityResource userActivityResource = new UserActivityResource();
    userActivityResource.setId(user.getId());
    userActivityResource.setDefaultProjectId(project.getId());
    userActivityResource.setFullName(user.getLogin());
    return userActivityResource;
  }
}
