/*
 * Copyright 2024 EPAM Systems
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
import static com.epam.ta.reportportal.commons.Predicates.isPresent;
import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;
import static com.epam.ta.reportportal.util.OffsetUtils.responseWithPageParameters;
import static com.epam.ta.reportportal.ws.converter.converters.OrganizationProjectInfoConverter.TO_ORG_PROJECT_INFO;

import com.epam.reportportal.api.model.OrganizationProjectInfo;
import com.epam.reportportal.api.model.OrganizationProjectsPage;
import com.epam.reportportal.api.model.ProjectDetails;
import com.epam.reportportal.api.model.ProjectProfile;
import com.epam.reportportal.extension.event.ProjectEvent;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.core.events.activity.ProjectCreatedEvent;
import com.epam.ta.reportportal.core.project.OrganizationProjectHandler;
import com.epam.ta.reportportal.dao.AttributeRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.ProjectUserRepository;
import com.epam.ta.reportportal.dao.organization.OrganizationRepositoryCustom;
import com.epam.ta.reportportal.dao.project.OrganizationProjectRepository;
import com.epam.ta.reportportal.entity.organization.Organization;
import com.epam.ta.reportportal.entity.organization.OrganizationRole;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectAttribute;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.util.SlugifyUtils;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class OrganizationProjectHandlerImpl implements OrganizationProjectHandler {

  private static final String CREATE_KEY = "create";

  private final OrganizationProjectRepository organizationProjectRepository;
  private final ProjectUserRepository projectUserRepository;
  private final ProjectRepository projectRepository;
  private final OrganizationRepositoryCustom organizationRepositoryCustom;
  private final AttributeRepository attributeRepository;
  private final ApplicationEventPublisher applicationEventPublisher;


  public OrganizationProjectHandlerImpl(OrganizationProjectRepository organizationProjectRepository,
      ProjectUserRepository projectUserRepository, ProjectRepository projectRepository,
      OrganizationRepositoryCustom organizationRepositoryCustom,
      AttributeRepository attributeRepository,
      ApplicationEventPublisher applicationEventPublisher) {
    this.organizationProjectRepository = organizationProjectRepository;
    this.projectUserRepository = projectUserRepository;
    this.projectRepository = projectRepository;
    this.organizationRepositoryCustom = organizationRepositoryCustom;
    this.attributeRepository = attributeRepository;
    this.applicationEventPublisher = applicationEventPublisher;
  }

  @Override
  public OrganizationProjectsPage getOrganizationProjectsPage(ReportPortalUser user, Long orgId,
      Filter filter, Pageable pageable) {
    OrganizationProjectsPage organizationProjectsPage = new OrganizationProjectsPage();

    if (!user.getUserRole().equals(UserRole.ADMINISTRATOR)
        && user.getOrganizationDetails().get(orgId.toString()).getOrgRole()
        .equals(OrganizationRole.MEMBER)) {

      var projectIds = projectUserRepository.findProjectIdsByUserId(user.getUserId())
          .stream()
          .map(Object::toString)
          .collect(Collectors.joining(","));

      if (projectIds.isEmpty()) {
        // return empty response
        return responseWithPageParameters(organizationProjectsPage, pageable, 0);
      } else {
        filter.withCondition(
            new FilterCondition(Condition.IN, false, projectIds, CRITERIA_PROJECT_ID));
      }
    }

    Page<ProjectProfile> projectProfilePagedList =
        organizationProjectRepository.getProjectProfileListByFilter(filter, pageable);
    organizationProjectsPage.items(projectProfilePagedList.getContent());

    return responseWithPageParameters(organizationProjectsPage, pageable,
        projectProfilePagedList.getTotalElements());
  }

  @Override
  public OrganizationProjectInfo createProject(Long orgId, ProjectDetails projectDetails,
      ReportPortalUser user) {
    Organization organization = organizationRepositoryCustom.findById(orgId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.ORGANIZATION_NOT_FOUND, orgId));

    if (StringUtils.isEmpty(projectDetails.getSlug())) {
      projectDetails.setSlug(SlugifyUtils.slugify(projectDetails.getName()));
    }
    var projectKey = organization.getSlug() + "." + projectDetails.getSlug();

    Optional<Project> existProject = projectRepository.findByKey(projectKey);
    expect(existProject, not(isPresent())).verify(ErrorType.PROJECT_ALREADY_EXISTS, projectKey);

    var projectToSave = generateProjectBody(orgId, projectDetails, projectKey);
    Set<ProjectAttribute> projectAttributes = ProjectUtils.defaultProjectAttributes(projectToSave,
        attributeRepository.getDefaultProjectAttributes()
    );
    projectToSave.setProjectAttributes(projectAttributes);
    Project createdProject = projectRepository.save(projectToSave);

    applicationEventPublisher.publishEvent(new ProjectEvent(createdProject.getId(), CREATE_KEY));
    publishProjectCreatedEvent(user.getUserId(), user.getUsername(), createdProject);

    return TO_ORG_PROJECT_INFO.apply(createdProject);
  }

  private Project generateProjectBody(Long orgId, ProjectDetails projectDetails,
      String projectKey) {
    var project = new Project();
    var now = Instant.now();
    project.setName(projectDetails.getName());
    project.setSlug(projectDetails.getSlug());
    project.setKey(projectKey);
    project.setOrganizationId(orgId);
    project.setCreationDate(now);
    project.setUpdatedAt(now);
    return project;
  }

  private void publishProjectCreatedEvent(Long userId, String userLogin, Project project) {
    Long projectId = project.getId();
    String projectName = project.getName();
    ProjectCreatedEvent event = new ProjectCreatedEvent(userId, userLogin, projectId, projectName);
    applicationEventPublisher.publishEvent(event);
  }


}
