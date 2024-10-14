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
import static com.epam.reportportal.rules.exception.ErrorType.PROJECT_NOT_FOUND;
import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.isPresent;
import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.RP_SUBJECT_NAME;
import static com.epam.ta.reportportal.util.OffsetUtils.responseWithPageParameters;
import static com.epam.ta.reportportal.ws.converter.converters.OrganizationConverter.PROJECT_PROFILE_TO_ORG_PROJECT_INFO;
import static com.epam.ta.reportportal.ws.converter.converters.OrganizationConverter.PROJECT_TO_ORG_PROJECT_INFO;

import com.epam.reportportal.api.model.OrganizationProjectsPage;
import com.epam.reportportal.api.model.ProjectBase;
import com.epam.reportportal.api.model.ProjectInfo;
import com.epam.reportportal.extension.event.ProjectEvent;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.binary.AttachmentBinaryDataService;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.auto.client.AnalyzerServiceClient;
import com.epam.ta.reportportal.core.events.activity.ProjectCreatedEvent;
import com.epam.ta.reportportal.core.events.activity.ProjectDeletedEvent;
import com.epam.ta.reportportal.core.project.OrganizationProjectHandler;
import com.epam.ta.reportportal.core.remover.ContentRemover;
import com.epam.ta.reportportal.dao.AttributeRepository;
import com.epam.ta.reportportal.dao.IssueTypeRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.ProjectUserRepository;
import com.epam.ta.reportportal.dao.organization.OrganizationRepositoryCustom;
import com.epam.ta.reportportal.dao.project.OrganizationProjectRepository;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.organization.Organization;
import com.epam.ta.reportportal.entity.organization.OrganizationRole;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectAttribute;
import com.epam.ta.reportportal.entity.project.ProjectIssueType;
import com.epam.ta.reportportal.entity.project.ProjectProfile;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.util.SlugifyUtils;
import java.time.Instant;
import java.util.Objects;
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
  private final IssueTypeRepository issueTypeRepository;
  private final ContentRemover<Project> projectContentRemover;
  private final LogRepository logRepository;
  private final AttachmentBinaryDataService attachmentBinaryDataService;
  private final LogIndexer logIndexer;
  private final AnalyzerServiceClient analyzerServiceClient;


  public OrganizationProjectHandlerImpl(OrganizationProjectRepository organizationProjectRepository,
      ProjectUserRepository projectUserRepository, ProjectRepository projectRepository,
      OrganizationRepositoryCustom organizationRepositoryCustom,
      AttributeRepository attributeRepository,
      ApplicationEventPublisher applicationEventPublisher, IssueTypeRepository issueTypeRepository,
      ContentRemover<Project> projectContentRemover, LogRepository logRepository,
      AttachmentBinaryDataService attachmentBinaryDataService, LogIndexer logIndexer,
      AnalyzerServiceClient analyzerServiceClient) {
    this.organizationProjectRepository = organizationProjectRepository;
    this.projectUserRepository = projectUserRepository;
    this.projectRepository = projectRepository;
    this.organizationRepositoryCustom = organizationRepositoryCustom;
    this.attributeRepository = attributeRepository;
    this.applicationEventPublisher = applicationEventPublisher;
    this.issueTypeRepository = issueTypeRepository;
    this.projectContentRemover = projectContentRemover;
    this.logRepository = logRepository;
    this.attachmentBinaryDataService = attachmentBinaryDataService;

    this.logIndexer = logIndexer;
    this.analyzerServiceClient = analyzerServiceClient;
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

    organizationProjectsPage.items(projectProfilePagedList.getContent()
        .stream()
        .map(PROJECT_PROFILE_TO_ORG_PROJECT_INFO)
        .collect(Collectors.toList()));

    return responseWithPageParameters(organizationProjectsPage, pageable,
        projectProfilePagedList.getTotalElements());
  }

  @Override
  public ProjectInfo createProject(Long orgId, ProjectBase projectBase,
      ReportPortalUser user) {
    Organization organization = organizationRepositoryCustom.findById(orgId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.ORGANIZATION_NOT_FOUND, orgId));

    if (StringUtils.isEmpty(projectBase.getSlug())) {
      projectBase.setSlug(SlugifyUtils.slugify(projectBase.getName()));
    }
    var projectKey = organization.getSlug() + "." + projectBase.getSlug();

    Optional<Project> existProject =
        projectRepository.findByNameAndOrganizationId(projectBase.getName(), orgId);
    expect(existProject, not(isPresent())).verify(ErrorType.PROJECT_ALREADY_EXISTS, projectKey);

    var projectToSave = generateProjectBody(orgId, projectBase, projectKey);
    Set<ProjectAttribute> projectAttributes = ProjectUtils.defaultProjectAttributes(projectToSave,
        attributeRepository.getDefaultProjectAttributes()
    );
    projectToSave.setProjectAttributes(projectAttributes);

    projectToSave.setProjectIssueTypes(
        ProjectUtils.defaultIssueTypes(projectToSave, issueTypeRepository.getDefaultIssueTypes()));

    Project createdProject = projectRepository.save(projectToSave);

    applicationEventPublisher.publishEvent(new ProjectEvent(createdProject.getId(), CREATE_KEY));
    publishProjectCreatedEvent(user, createdProject);

    return PROJECT_TO_ORG_PROJECT_INFO.apply(createdProject); // backward convert to ProjectInfo
  }

  @Override
  public void deleteProject(ReportPortalUser rpUser, Long orgId, Long projectId) {
    Project project = getProjectById(projectId);
    expect(project.getOrganizationId(), equalTo(orgId))
        .verify(PROJECT_NOT_FOUND, "Project " + projectId + " not found in organization " + orgId);

    deleteProjectWithDependants(project);

    publishSpecialProjectDeletedEvent(rpUser, project);
  }


  private Project generateProjectBody(Long orgId, ProjectBase projectDetails,
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

  private void deleteProjectWithDependants(Project project) {
    Set<Long> defaultIssueTypeIds = issueTypeRepository.getDefaultIssueTypes()
        .stream()
        .map(IssueType::getId)
        .collect(Collectors.toSet());

    Set<IssueType> issueTypesToRemove = project.getProjectIssueTypes()
        .stream()
        .map(ProjectIssueType::getIssueType)
        .filter(issueType -> !defaultIssueTypeIds.contains(issueType.getId()))
        .collect(Collectors.toSet());

    projectContentRemover.remove(project);
    projectRepository.delete(project);
    issueTypeRepository.deleteAll(issueTypesToRemove);
    logIndexer.deleteIndex(project.getId());
    analyzerServiceClient.removeSuggest(project.getId());
    logRepository.deleteByProjectId(project.getId());
    attachmentBinaryDataService.deleteAllByProjectId(project.getId());

  }

  private void publishProjectCreatedEvent(ReportPortalUser user, Project project) {
    ProjectCreatedEvent event = new ProjectCreatedEvent(
        user.getUserId(),
        user.getUsername(),
        project.getId(),
        project.getName());
    applicationEventPublisher.publishEvent(event);
  }

  private void publishSpecialProjectDeletedEvent(ReportPortalUser user, Project project) {
    if (Objects.nonNull(user)) {
      Long userId = user.getUserId();
      String username = user.getUsername();
      publishProjectDeletedEvent(userId, username, project.getId(), project.getName());
    } else {
      publishProjectDeletedEvent(null, RP_SUBJECT_NAME, project.getId(), "personal_project");
    }
  }

  private void publishProjectDeletedEvent(Long userId, String userLogin, Long projectId,
      String projectName) {
    ProjectDeletedEvent event = new ProjectDeletedEvent(userId, userLogin, projectId, projectName);
    applicationEventPublisher.publishEvent(event);
  }

  private Project getProjectById(Long projectId) {
    return projectRepository.findById(projectId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectId));
  }


}
