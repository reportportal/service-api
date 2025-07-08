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

package com.epam.ta.reportportal.core.organization.impl;

import static com.epam.reportportal.api.model.OrgRole.MANAGER;
import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.rules.commons.validation.Suppliers.formattedSupplier;
import static com.epam.reportportal.rules.exception.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.reportportal.rules.exception.ErrorType.NOT_FOUND;
import static com.epam.reportportal.rules.exception.ErrorType.ORGANIZATION_NOT_FOUND;
import static com.epam.reportportal.rules.exception.ErrorType.PROJECT_NOT_FOUND;
import static com.epam.reportportal.rules.exception.ErrorType.USER_ALREADY_ASSIGNED;
import static com.epam.reportportal.rules.exception.ErrorType.USER_NOT_FOUND;
import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.util.OffsetUtils.responseWithPageParameters;
import static com.epam.ta.reportportal.util.OrganizationUserValidator.validateUserRoles;
import static com.epam.ta.reportportal.util.OrganizationUserValidator.validateUserType;
import static com.epam.ta.reportportal.util.SecurityContextUtils.getPrincipal;
import static com.epam.ta.reportportal.ws.converter.converters.OrganizationConverter.ORG_USER_ACCOUNT_TO_ORG_USER;
import static com.epam.ta.reportportal.ws.converter.converters.OrganizationUserConverter.MEMBERSHIP_TO_ORG_USER_PROJECT;
import static java.util.function.Predicate.isEqual;

import com.epam.reportportal.api.model.OrgRole;
import com.epam.reportportal.api.model.OrgUserAssignment;
import com.epam.reportportal.api.model.OrgUserProjectPage;
import com.epam.reportportal.api.model.OrgUserUpdateRequest;
import com.epam.reportportal.api.model.OrganizationUsersPage;
import com.epam.reportportal.api.model.ProjectRole;
import com.epam.reportportal.api.model.UserAssignmentResponse;
import com.epam.reportportal.api.model.UserProjectInfo;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.organization.OrganizationUsersHandler;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.ProjectUserRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.dao.organization.OrganizationRepositoryCustom;
import com.epam.ta.reportportal.dao.organization.OrganizationUserRepository;
import com.epam.ta.reportportal.dao.organization.OrganizationUsersRepositoryCustom;
import com.epam.ta.reportportal.entity.enums.OrganizationType;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.entity.organization.Organization;
import com.epam.ta.reportportal.entity.organization.OrganizationRole;
import com.epam.ta.reportportal.entity.organization.OrganizationUserAccount;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.user.OrganizationUser;
import com.epam.ta.reportportal.entity.user.ProjectUser;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.util.SlugifyUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Slf4j
public class OrganizationUsersHandlerImpl implements OrganizationUsersHandler {

  private final OrganizationUsersRepositoryCustom organizationUsersRepositoryCustom;

  private final ProjectRepository projectRepository;

  private final UserRepository userRepository;

  private final ProjectUserRepository projectUserRepository;
  private final OrganizationUserRepository organizationUserRepository;
  private final OrganizationRepositoryCustom organizationRepositoryCustom;


  public OrganizationUsersHandlerImpl(
      OrganizationUsersRepositoryCustom organizationUsersRepositoryCustom,
      ProjectRepository projectRepository, UserRepository userRepository,
      ProjectUserRepository projectUserRepository,
      OrganizationUserRepository organizationUserRepository,
      OrganizationRepositoryCustom organizationRepositoryCustom) {
    this.organizationUsersRepositoryCustom = organizationUsersRepositoryCustom;
    this.projectRepository = projectRepository;
    this.userRepository = userRepository;
    this.projectUserRepository = projectUserRepository;
    this.organizationUserRepository = organizationUserRepository;
    this.organizationRepositoryCustom = organizationRepositoryCustom;

  }

  @Override
  @Transactional(readOnly = true)
  public OrganizationUsersPage getOrganizationUsers(Queryable filter, Pageable pageable) {
    Page<OrganizationUserAccount> organizationUserAccounts =
        organizationUsersRepositoryCustom.findByFilter(filter, pageable);

    List<com.epam.reportportal.api.model.OrganizationUser> items =
        organizationUserAccounts.getContent()
            .stream()
            .map(orgUserAccount -> ORG_USER_ACCOUNT_TO_ORG_USER.apply(orgUserAccount))
            .toList();

    OrganizationUsersPage organizationUsersPage =
        new OrganizationUsersPage()
            .items(items);

    return responseWithPageParameters(organizationUsersPage, pageable,
        organizationUserAccounts.getTotalElements());
  }

  @Override
  @Transactional
  public UserAssignmentResponse assignUser(Long orgId, OrgUserAssignment request) {
    User assignedUser = userRepository.findById(request.getId())
        .orElseThrow(() -> new ReportPortalException(USER_NOT_FOUND, request.getId()));
    var organization = organizationRepositoryCustom.findById(orgId)
        .orElseThrow(() -> new ReportPortalException(ORGANIZATION_NOT_FOUND, orgId));
    validateUserType(organization, assignedUser);

    var orgUserExists = organizationUserRepository.findByUserIdAndOrganization_Id(
        assignedUser.getId(), orgId).isPresent();
    expect(orgUserExists, equalTo(false)).verify(USER_ALREADY_ASSIGNED,
        request.getId(),
        formattedSupplier("organization '{}'", orgId)
    );

    saveOrganizationUser(organization, assignedUser, request.getOrgRole().getValue());

    var projects = getDeduplicatedProjectList(request);

    // validate projects
    projects.forEach(project -> {
      var projectEntity = projectRepository.findById(project.getId())
          .orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, project.getId()));
      expect(projectEntity.getOrganizationId(), equalTo(orgId)).verify(BAD_REQUEST_ERROR,
          formattedSupplier("Project '{}' does not belong to organization {}", project.getId(), orgId)
      );

      var projectUser = projectUserRepository
          .findProjectUserByUserIdAndProjectId(request.getId(), project.getId());
      expect(projectUser.isPresent(), isEqual(false))
          .verify(ErrorType.PROJECT_ALREADY_EXISTS, projectEntity.getKey());

      projectUserRepository.save(new ProjectUser()
          .withProject(projectEntity)
          .withProjectRole(com.epam.ta.reportportal.entity.project.ProjectRole
              .valueOf(project.getProjectRole().toString()))
          .withUser(assignedUser));
    });

    return new UserAssignmentResponse()
        .message("User %s has been successfully assigned".formatted(assignedUser.getLogin()));
  }

  @Override
  @Transactional
  public void unassignUser(Long orgId, Long unassignUserId) {
    OrganizationUser organizationUser = organizationUserRepository.findByUserIdAndOrganization_Id(unassignUserId, orgId)
        .orElseThrow(() -> new ReportPortalException(NOT_FOUND, "User %s assignment".formatted(unassignUserId)));

    validateUserRoles(getPrincipal(), organizationUser);
    validateUserType(organizationUser.getOrganization(), organizationUser.getUser());
    validatePersonalOrganization(organizationUser.getOrganization(), organizationUser.getUser());

    projectUserRepository.deleteProjectUserByProjectOrganizationId(orgId, unassignUserId);
    organizationUserRepository.delete(organizationUser);
  }

  @Override
  @Transactional
  public OrgUserProjectPage findUserProjectsInOrganization(Long userId, Long organizationId,
      Pageable pageable) {
    OrgUserProjectPage orgUserProjectPage = new OrgUserProjectPage();

    Page<MembershipDetails> userProjectsInOrganization = projectUserRepository
        .findUserProjectsInOrganization(userId, organizationId, pageable);
    orgUserProjectPage.items(
        userProjectsInOrganization.getContent().stream().map(MEMBERSHIP_TO_ORG_USER_PROJECT)
            .collect(
                Collectors.toList()));

    return responseWithPageParameters(orgUserProjectPage, pageable,
        userProjectsInOrganization.getTotalElements());
  }

  private void validatePersonalOrganization(Organization organization, User userToUnassign) {
    if (organization.getOrganizationType().equals(OrganizationType.PERSONAL)
        && isPersonalOrganization(organization.getName(), userToUnassign.getEmail())) {
      throw new ReportPortalException(ErrorType.ACCESS_DENIED,
          "User %s cannot be unassigned from personal organization".formatted(userToUnassign.getId()));
    }
  }

  // TODO: introduce a proper way to check if the organization is personal.
  //  e.g. extend organization_users table with is_owner field. Waiting for requirements
  private boolean isPersonalOrganization(String orgName, String email) {
    var username = SlugifyUtils.slugify(StringUtils.substringBefore(email, "@"));
    var baseOrgName = StringUtils.substringBefore(orgName, "_personal").toLowerCase();
    return username.equals(baseOrgName);
  }

  public void saveOrganizationUser(Organization organization, User assignedUser, String role) {
    var organizationUser = new OrganizationUser();
    organizationUser.setOrganization(organization);
    organizationUser.setUser(assignedUser);
    organizationUser.setOrganizationRole(OrganizationRole.valueOf(role));
    organizationUserRepository.save(organizationUser);
  }

  private List<UserProjectInfo> getDeduplicatedProjectList(OrgUserAssignment request) {
    Map<Long, com.epam.reportportal.api.model.ProjectRole> projectRoleMap = new ConcurrentHashMap<>();
    request.getProjects().stream()
        .map(project -> {
          if (request.getOrgRole() != null && request.getOrgRole().equals(MANAGER)) {
            return project.projectRole(ProjectRole.EDITOR);
          }
          return project;
        })
        .forEach(project -> {
          if (projectRoleMap.get(project.getId()) == null) {
            projectRoleMap.put(project.getId(), project.getProjectRole());
          } else {
            if (project.getProjectRole().equals(ProjectRole.EDITOR)) {
              projectRoleMap.replace(project.getId(), ProjectRole.EDITOR);
            }
          }
        });
    return projectRoleMap.entrySet().stream()
        .map(entry -> new UserProjectInfo()
            .id(entry.getKey())
            .projectRole(entry.getValue()))
        .toList();
  }

  @Override
  @Transactional
  public void updateOrganizationUserDetails(Long orgId, Long userId,
      OrgUserUpdateRequest orgUserUpdateRequest) {

    Organization organization = organizationRepositoryCustom.findById(orgId)
        .orElseThrow(() -> new ReportPortalException(ORGANIZATION_NOT_FOUND, orgId));
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ReportPortalException(USER_NOT_FOUND, userId));
    Optional<OrganizationUser> userOrganization = organizationUserRepository.findByUserIdAndOrganization_Id(
        userId, orgId);

    assignToOrganization(orgUserUpdateRequest, userOrganization, organization, user);

    List<UserProjectInfo> projects = orgUserUpdateRequest.getProjects();
    List<Long> projectsId = projects.stream().map(UserProjectInfo::getId).toList();

    assignToProjects(projects, user, orgUserUpdateRequest.getOrgRole().equals(MANAGER));

    unassignUserProject(orgId, userId, projectsId);
  }

  private void unassignUserProject(Long orgId, Long userId, List<Long> projectsId) {
    Set<Long> currentUserProjectIds = projectUserRepository.findUserProjectIdsInOrganization(
        userId, orgId);

    List<Long> projectIdsToUnassign = currentUserProjectIds.stream()
        .filter(projectId -> !projectsId.contains(projectId))
        .toList();

    projectUserRepository.deleteByUserIdAndProjectIds(userId, projectIdsToUnassign);
  }

  private void assignToProjects(List<UserProjectInfo> projects, User user, boolean isManager) {
    for (UserProjectInfo userProjectInfo : projects) {
      Optional<ProjectUser> projectUserOptional = projectUserRepository.findProjectUserByUserIdAndProjectId(
          user.getId(), userProjectInfo.getId());
      Project project = projectRepository.findById(userProjectInfo.getId())
          .orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, user.getId()));

      ProjectUser projectUser = projectUserOptional.orElse(new ProjectUser());

      projectUser.setUser(user);
      projectUser.setProject(project);
      if (userProjectInfo.getProjectRole() == null) {
        userProjectInfo.setProjectRole(ProjectRole.VIEWER);
      }
      if (isManager) {
        userProjectInfo.setProjectRole(ProjectRole.EDITOR);
      }
      projectUser.setProjectRole(com.epam.ta.reportportal.entity.project.ProjectRole.valueOf(
          userProjectInfo.getProjectRole().getValue()));
      projectUserRepository.save(projectUser);
    }
  }

  private void assignToOrganization(OrgUserUpdateRequest orgUserUpdateRequest,
      Optional<OrganizationUser> userOrganization, Organization organization, User assignedUser) {
    OrganizationUser organizationUser = userOrganization.orElse(new OrganizationUser());
    if (organizationUser.getOrganization() == null) {
      organizationUser.setOrganization(organization);
    }
    organizationUser.setUser(assignedUser);
    organizationUser.setOrganizationRole(OrganizationRole.valueOf(
        orgUserUpdateRequest.getOrgRole().getValue()));
    organizationUserRepository.save(organizationUser);
  }
}
