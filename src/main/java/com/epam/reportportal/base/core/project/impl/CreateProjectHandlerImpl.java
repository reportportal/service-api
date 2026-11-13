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

package com.epam.reportportal.base.core.project.impl;

import com.epam.reportportal.base.core.events.domain.ProjectCreatedEvent;
import com.epam.reportportal.base.core.project.CreateProjectHandler;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.base.infrastructure.persistence.util.PersonalProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Creates projects and default settings in an organization.
 *
 * @author Pavel Bortnik
 */
@Service
public class CreateProjectHandlerImpl implements CreateProjectHandler {

  private final PersonalProjectService personalProjectService;

  private final ProjectRepository projectRepository;

  private final ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  public CreateProjectHandlerImpl(PersonalProjectService personalProjectService,
      ProjectRepository projectRepository,
      ApplicationEventPublisher applicationEventPublisher) {
    this.personalProjectService = personalProjectService;
    this.projectRepository = projectRepository;
    this.applicationEventPublisher = applicationEventPublisher;
  }

  private void publishSystemProjectCreatedEvent(Project project) {
    Long projectId = project.getId();
    String projectName = project.getName();
    ProjectCreatedEvent event = new ProjectCreatedEvent(projectId, projectName,
        project.getOrganizationId());
    applicationEventPublisher.publishEvent(event);
  }

  @Override
  public Project createPersonal(User user) {
    //TODO refactor personal project generation to not add user inside method (cannot be done now, because DAO dependency may affect other services)
    final Project personalProject = personalProjectService.generatePersonalProject(user);
    personalProject.getUsers().clear();
    projectRepository.save(personalProject);
    publishSystemProjectCreatedEvent(personalProject);
    return personalProject;
  }
}
