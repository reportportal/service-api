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

package com.epam.ta.reportportal.core.project.impl;

import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.isPresent;
import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.RP_SUBJECT_NAME;

import com.epam.reportportal.extension.event.ProjectEvent;
import com.epam.reportportal.rules.commons.validation.Suppliers;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.activity.ProjectCreatedEvent;
import com.epam.ta.reportportal.core.project.CreateProjectHandler;
import com.epam.ta.reportportal.dao.AttributeRepository;
import com.epam.ta.reportportal.dao.IssueTypeRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.ProjectUserRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.dao.organization.OrganizationRepositoryCustom;
import com.epam.ta.reportportal.entity.organization.Organization;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectAttribute;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.entity.user.ProjectUser;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.model.EntryCreatedRS;
import com.epam.ta.reportportal.model.project.CreateProjectRQ;
import com.epam.ta.reportportal.util.PersonalProjectService;
import com.epam.ta.reportportal.util.SlugifyUtils;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * @author Pavel Bortnik
 */
@Service
public class CreateProjectHandlerImpl implements CreateProjectHandler {

  private static final String CREATE_KEY = "create";
  private static final String RESERVED_PROJECT_NAME = "project";

  private final PersonalProjectService personalProjectService;

  private final ProjectRepository projectRepository;

  private final UserRepository userRepository;

  private final AttributeRepository attributeRepository;

  private final IssueTypeRepository issueTypeRepository;

  private final ApplicationEventPublisher applicationEventPublisher;

  private final ProjectUserRepository projectUserRepository;
  private final OrganizationRepositoryCustom organizationRepositoryCustom;

  @Autowired
  public CreateProjectHandlerImpl(PersonalProjectService personalProjectService,
      ProjectRepository projectRepository, UserRepository userRepository,
      AttributeRepository attributeRepository, IssueTypeRepository issueTypeRepository,
      ApplicationEventPublisher applicationEventPublisher,
      ProjectUserRepository projectUserRepository,
      OrganizationRepositoryCustom organizationRepositoryCustom) {
    this.personalProjectService = personalProjectService;
    this.projectRepository = projectRepository;
    this.userRepository = userRepository;
    this.attributeRepository = attributeRepository;
    this.issueTypeRepository = issueTypeRepository;
    this.applicationEventPublisher = applicationEventPublisher;
    this.projectUserRepository = projectUserRepository;
    this.organizationRepositoryCustom = organizationRepositoryCustom;
  }

  @Override
  public EntryCreatedRS createProject(CreateProjectRQ createProjectRQ, ReportPortalUser user) {
    String projectName = createProjectRQ.getProjectName().toLowerCase().trim();
    Long orgId = createProjectRQ.getOrganizationId();

    expect(projectName, not(equalTo(RESERVED_PROJECT_NAME))).verify(ErrorType.INCORRECT_REQUEST,
        Suppliers.formattedSupplier("Project with name '{}' is reserved by system", projectName)
    );

    expect(projectName,
        com.epam.ta.reportportal.util.Predicates.SPECIAL_CHARS_ONLY.negate()
    ).verify(ErrorType.INCORRECT_REQUEST,
        Suppliers.formattedSupplier("Project name '{}' consists only of special characters",
            projectName
        )
    );

    Organization organization = organizationRepositoryCustom.findById(orgId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.ORGANIZATION_NOT_FOUND, orgId));
    var projectSlug = SlugifyUtils.slugify(projectName);
    var projectKey = generateProjectKey(organization, projectSlug);

    Optional<Project> existProject = projectRepository.findByKey(projectKey);
    expect(existProject, not(isPresent())).verify(ErrorType.PROJECT_ALREADY_EXISTS, projectName);

    User dbUser = userRepository.findRawById(user.getUserId())
        .orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, user.getUsername()));

    Project project = new Project();

    project.setOrganizationId(organization.getId());
    project.setKey(projectKey);
    project.setSlug(projectSlug);
    project.setName(projectName);
    project.setCreationDate(Instant.now());

    project.setProjectIssueTypes(
        ProjectUtils.defaultIssueTypes(project, issueTypeRepository.getDefaultIssueTypes()));
    Set<ProjectAttribute> projectAttributes = ProjectUtils.defaultProjectAttributes(project,
        attributeRepository.getDefaultProjectAttributes()
    );

    project.setProjectAttributes(projectAttributes);

    ProjectUser projectUser = new ProjectUser().withProject(project).withUser(dbUser)
        .withProjectRole(ProjectRole.EDITOR);

    projectRepository.save(project);
    projectUserRepository.save(projectUser);

    applicationEventPublisher.publishEvent(new ProjectEvent(project.getId(), CREATE_KEY));
    publishProjectCreatedEvent(user.getUserId(), user.getUsername(), project);

    return new EntryCreatedRS(project.getId());
  }

  private void publishProjectCreatedEvent(Long userId, String userLogin, Project project) {
    Long projectId = project.getId();
    String projectName = project.getName();
    ProjectCreatedEvent event = new ProjectCreatedEvent(userId, userLogin, projectId, projectName,
        project.getOrganizationId());
    applicationEventPublisher.publishEvent(event);
  }

  @Override
  public Project createPersonal(User user) {
    //TODO refactor personal project generation to not add user inside method (cannot be done now, because DAO dependency may affect other services)
    final Project personalProject = personalProjectService.generatePersonalProject(user);
    personalProject.getUsers().clear();
    projectRepository.save(personalProject);
    publishProjectCreatedEvent(null, RP_SUBJECT_NAME, personalProject);
    return personalProject;
  }

  private String generateProjectKey(Organization organization, String projectSlug) {
    return organization.getSlug() + "." + projectSlug;
  }
}
