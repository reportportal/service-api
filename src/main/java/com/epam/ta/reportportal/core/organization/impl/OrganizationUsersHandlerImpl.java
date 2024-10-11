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

import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.rules.commons.validation.Suppliers.formattedSupplier;
import static com.epam.reportportal.rules.exception.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.reportportal.rules.exception.ErrorType.RESOURCE_ALREADY_EXISTS;
import static com.epam.reportportal.rules.exception.ErrorType.USER_NOT_FOUND;
import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.entity.organization.OrganizationRole.MEMBER;
import static com.epam.ta.reportportal.util.OffsetUtils.responseWithPageParameters;
import static java.util.function.Predicate.isEqual;

import com.epam.reportportal.api.model.AccountType;
import com.epam.reportportal.api.model.InstanceRole;
import com.epam.reportportal.api.model.OrgRole;
import com.epam.reportportal.api.model.OrgUserAssignment;
import com.epam.reportportal.api.model.OrganizationUsersPage;
import com.epam.reportportal.api.model.UserAssignmentResponse;
import com.epam.reportportal.api.model.UserProjectInfo;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.organization.OrganizationUsersHandler;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.ProjectUserRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.dao.organization.OrganizationRepositoryCustom;
import com.epam.ta.reportportal.dao.organization.OrganizationUserRepository;
import com.epam.ta.reportportal.dao.organization.OrganizationUsersRepositoryCustom;
import com.epam.ta.reportportal.entity.enums.OrganizationType;
import com.epam.ta.reportportal.entity.organization.Organization;
import com.epam.ta.reportportal.entity.organization.OrganizationUserAccount;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.OrganizationUser;
import com.epam.ta.reportportal.entity.user.ProjectUser;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserType;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
@Log4j2
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
  public OrganizationUsersPage getOrganizationUsers(Queryable filter, Pageable pageable) {
    Page<OrganizationUserAccount> organizationUserAccounts = organizationUsersRepositoryCustom.findByFilter(filter, pageable);
// TODO: Implement the converter
    List<com.epam.reportportal.api.model.OrganizationUser> items = organizationUserAccounts.getContent()
        .stream()
        .map(orgUserAccount -> {
          return new com.epam.reportportal.api.model.OrganizationUser()
              .id(orgUserAccount.getId())
              .fullName(orgUserAccount.getFullName())
              .createdAt(orgUserAccount.getCreatedAt())
              .updatedAt(orgUserAccount.getUpdatedAt())
              .instanceRole(InstanceRole.fromValue(orgUserAccount.getInstanceRole().toString()))
              .orgRole(OrgRole.fromValue(orgUserAccount.getOrgRole().toString()))
              .accountType(AccountType.fromValue(orgUserAccount.getAuthProvider().toString()))
              .email(orgUserAccount.getEmail())
              .lastLoginAt(orgUserAccount.getLastLoginAt())
              .externalId(orgUserAccount.getExternalId())
              .uuid(orgUserAccount.getUuid());
        })
        .toList();

    OrganizationUsersPage organizationUsersPage =
        new OrganizationUsersPage()
            .items(organizationUserAccounts.getContent());

    return responseWithPageParameters(organizationUsersPage, pageable,
        organizationUserProfiles.getTotalElements());
  }

  @Override
  public UserAssignmentResponse assignUser(Long orgId, OrgUserAssignment request,
      ReportPortalUser rpUser) {
    User assignedUser = userRepository.findById(request.getId())
        .orElseThrow(() -> new ReportPortalException(USER_NOT_FOUND, orgId));
    var organization = organizationRepositoryCustom.findById(orgId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.ORGANIZATION_NOT_FOUND, orgId));
    validateUserType(organization, assignedUser);

    var orgUserExists = organizationUserRepository.findByUserIdAndOrganization_Id(
        assignedUser.getId(), orgId).isPresent();
    expect(orgUserExists, equalTo(false)).verify(RESOURCE_ALREADY_EXISTS,
        formattedSupplier("User '{}' cannot be assigned to organization twice.", request.getId())
    );

    saveOrganizationUser(organization, assignedUser);

    var projects = getDeduplicatedProjectList(request);

    // validate projects
    projects.forEach(project -> {
      var projectEntity = projectRepository.findById(project.getId())
          .orElseThrow(
              () -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, project.getId()));
      expect(projectEntity.getOrganizationId(), equalTo(orgId)).verify(BAD_REQUEST_ERROR,
          formattedSupplier("Project '{}' does not belong to organization {}", request.getId(),
              orgId)
      );

      var projectUser = projectUserRepository
          .findProjectUserByUserIdAndProjectId(request.getId(), project.getId());
      expect(projectUser.isPresent(), isEqual(false))
          .verify(ErrorType.PROJECT_ALREADY_EXISTS, projectEntity.getKey());

      projectUserRepository.save(new ProjectUser()
          .withProject(projectEntity)
          .withProjectRole(ProjectRole.valueOf(project.getProjectRole().name()))
          .withUser(assignedUser));
    });

    return new UserAssignmentResponse()
        .message("User %s has been successfully assigned".formatted(assignedUser.getLogin()));
  }

  private void saveOrganizationUser(Organization organization, User assignedUser) {
    var organizationUser = new OrganizationUser();
    organizationUser.setOrganization(organization);
    organizationUser.setUser(assignedUser);
    organizationUser.setOrganizationRole(MEMBER);
    organizationUserRepository.save(organizationUser);
  }

  private List<UserProjectInfo> getDeduplicatedProjectList(OrgUserAssignment request) {
    Map<Long, com.epam.reportportal.api.model.ProjectRole> projectRoleMap = new ConcurrentHashMap<>();
    request.getProjects().stream()
        .map(project -> {
          if (request.getOrgRole().equals(OrgRole.MANAGER)) {
            return project.projectRole(ProjectRole.EDITOR);
          }
          return project;
        })
        .forEach(project -> {
          if (projectRoleMap.get(project.getId()) == null) {
            projectRoleMap.put(project.getId(), project.getProjectRole());
          } else {
            if (project.getProjectRole().equals(ProjectRoleEnum.EDITOR)) {
              projectRoleMap.replace(project.getId(), ProjectRoleEnum.EDITOR);
            }
          }
        });
    return projectRoleMap.entrySet().stream()
        .map(entry -> new UserProjectInfo()
            .id(entry.getKey())
            .projectRole(entry.getValue()))
        .toList();
  }


  private void validateUserType(Organization organization, User assignedUser) {
    if (organization.getOrganizationType().equals(OrganizationType.EXTERNAL)
        && assignedUser.getUserType().equals(UserType.UPSA)) {
      throw new ReportPortalException(ErrorType.ACCESS_DENIED, assignedUser.getLogin());
    }
  }

}
