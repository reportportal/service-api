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

package com.epam.reportportal.base.core.user.impl;

import static com.epam.reportportal.api.model.InvitationStatus.ACTIVATED;
import static com.epam.reportportal.api.model.InvitationStatus.PENDING;
import static com.epam.reportportal.base.core.user.impl.CreateUserHandlerImpl.BID_TYPE;
import static com.epam.reportportal.base.core.user.impl.CreateUserHandlerImpl.INTERNAL_BID_TYPE;
import static com.epam.reportportal.base.infrastructure.persistence.commons.Predicates.equalTo;
import static com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers.formattedSupplier;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.ACCESS_DENIED;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.NOT_FOUND;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.USER_ALREADY_ASSIGNED;
import static com.epam.reportportal.base.model.settings.SettingsKeyConstants.SERVER_USERS_SSO;
import static com.epam.reportportal.base.util.DateTimeProvider.instantNow;
import static com.epam.reportportal.base.util.OrganizationUserValidator.validateUserType;
import static com.epam.reportportal.base.util.SecurityContextUtils.getPrincipal;
import static com.epam.reportportal.base.util.email.EmailRulesValidator.NORMALIZE_EMAIL;

import com.epam.reportportal.api.model.Invitation;
import com.epam.reportportal.api.model.InvitationRequest;
import com.epam.reportportal.api.model.InvitationRequestOrganizationsInner;
import com.epam.reportportal.api.model.UserProjectInfo;
import com.epam.reportportal.base.core.events.domain.AssignUserEvent;
import com.epam.reportportal.base.core.events.domain.CreateInvitationLinkEvent;
import com.epam.reportportal.base.core.launch.util.LinkGenerator;
import com.epam.reportportal.base.core.organization.OrganizationUserService;
import com.epam.reportportal.base.core.user.UserInvitationService;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ServerSettingsRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserCreationBidRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepositoryCustom;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.Metadata;
import com.epam.reportportal.base.infrastructure.persistence.entity.ServerSettings;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.OrganizationUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.ProjectUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserCreationBid;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.util.email.MailServiceFactory;
import com.google.common.collect.Maps;
import jakarta.servlet.http.HttpServletRequest;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserInvitationServiceImpl implements UserInvitationService {

  private final ApplicationEventPublisher eventPublisher;
  private final ThreadPoolTaskExecutor emailExecutorService;
  private final MailServiceFactory emailServiceFactory;
  private final HttpServletRequest httpServletRequest;
  private final UserCreationBidRepository userCreationBidRepository;
  private final UserRepository userRepository;
  private final ProjectUserRepository projectUserRepository;
  private final OrganizationUserRepository organizationUserRepository;
  private final OrganizationUserService organizationUserService;
  private final OrganizationRepositoryCustom organizationRepositoryCustom;
  private final ProjectRepository projectRepository;
  private final ServerSettingsRepository settingsRepository;
  private final LinkGenerator linkGenerator;

  @Override
  public Invitation sendInvitation(InvitationRequest invitationRq) {
    if (isSsoEnabled()) {
      throw new ReportPortalException(ACCESS_DENIED, "Cannot invite user if SSO enabled.");
    }

    var rpUser = getPrincipal();
    var invitation = new Invitation();

    var inviter = userRepository.findById(rpUser.getUserId()).orElseThrow(
        () -> new ReportPortalException(ErrorType.USER_NOT_FOUND, rpUser.getUserId()));

    UserCreationBid userBid = new UserCreationBid();
    userBid.setUuid(UUID.randomUUID().toString());
    userBid.setEmail(NORMALIZE_EMAIL.apply(invitationRq.getEmail()));
    userBid.setInvitingUser(inviter);
    userBid.setMetadata(getUserCreationBidMetadata(invitationRq.getOrganizations()));

    UserCreationBid storedUserBid;
    try {
      storedUserBid = userCreationBidRepository.save(userBid);
    } catch (Exception e) {
      throw new ReportPortalException("Error while user creation bid registering.", e);
    }

    invitation.setCreatedAt(storedUserBid.getLastModified());
    invitation.setExpiresAt(storedUserBid.getLastModified().plus(1, ChronoUnit.DAYS));
    invitation.setId(UUID.fromString(userBid.getUuid()));
    invitation.setLink(linkGenerator.generateInvitationUrl(httpServletRequest, userBid.getUuid()));
    invitation.setEmail(userBid.getEmail());
    invitation.setStatus(PENDING);
    invitation.setUserId(inviter.getId());
    invitation.setFullName(inviter.getFullName());

    //  TODO: Add search organization integrations
    emailExecutorService.execute(() -> emailServiceFactory.getDefaultEmailService(false)
        .sendCreateUserConfirmationEmail(
            "User registration confirmation",
            new String[]{userBid.getEmail()}, invitation.getLink().toString()
        ));

    invitationRq.getOrganizations().forEach(org -> {
      if (org.getProjects() == null || org.getProjects().isEmpty()) {
        eventPublisher.publishEvent(
            new CreateInvitationLinkEvent(rpUser.getUserId(), rpUser.getUsername(), null, org.getId()));
      } else {
        org.getProjects().stream()
            .map(UserProjectInfo::getId)
            .forEach(projectId -> eventPublisher.publishEvent(
                new CreateInvitationLinkEvent(rpUser.getUserId(), rpUser.getUsername(), projectId, org.getId())));
      }
    });
    return invitation;
  }

  @Override
  public Invitation assignUser(InvitationRequest invitationRq, User userToAssign) {
    invitationRq.getOrganizations().forEach(orgInfo -> {
      var organization = organizationRepositoryCustom.findById(orgInfo.getId())
          .orElseThrow(
              () -> new ReportPortalException(ErrorType.ORGANIZATION_NOT_FOUND, orgInfo.getId()));
      var orgUserOptional = organizationUserRepository.findByUserIdAndOrganization_Id(
          userToAssign.getId(),
          organization.getId());

      orgUserOptional.ifPresent(ou -> {
        if (CollectionUtils.isEmpty(orgInfo.getProjects())) {
          throw new ReportPortalException(USER_ALREADY_ASSIGNED, userToAssign.getId(),
              formattedSupplier("the organization '{}'", organization.getId()));
        }
      });
      var orgUser = orgUserOptional.orElseGet(() -> {
        validateUserType(organization, userToAssign);
        return organizationUserService.saveOrganizationUser(organization, userToAssign,
            OrganizationRole.valueOf(orgInfo.getOrgRole().getValue()).toString());
      });

      assignProjects(orgInfo.getProjects(), orgUser, userToAssign);
    });

    var invitation = new Invitation();
    invitation.setCreatedAt(instantNow());
    invitation.setEmail(userToAssign.getEmail());
    invitation.setStatus(ACTIVATED);
    invitation.setUserId(userToAssign.getId());
    invitation.setFullName(userToAssign.getFullName());
    return invitation;
  }

  private void assignProjects(List<UserProjectInfo> projects, OrganizationUser orgUser, User user) {
    var orgId = orgUser.getOrganization().getId();
    projects.forEach(project -> {
      var projectEntity = projectRepository.findById(project.getId())
          .orElseThrow(() -> new ReportPortalException(NOT_FOUND, "Project " + project.getId()));
      expect(projectEntity.getOrganizationId(), equalTo(orgId))
          .verify(BAD_REQUEST_ERROR,
              formattedSupplier("Project '{}' does not belong to organization {}", project.getId(),
                  orgId));

      projectUserRepository.findProjectUserByUserIdAndProjectId(user.getId(), project.getId())
          .ifPresent(pu -> {
            throw new ReportPortalException(USER_ALREADY_ASSIGNED, user.getId(),
                formattedSupplier("the project '{}'", project.getId()));
          });

      projectUserRepository.save(new ProjectUser()
          .withProject(projectEntity)
          .withProjectRole(resolveProjectRole(orgUser.getOrganizationRole(),
              project.getProjectRole().getValue()))
          .withUser(user));

      AssignUserEvent assignUserEvent = new AssignUserEvent(user, projectEntity);
      eventPublisher.publishEvent(assignUserEvent);
    });

  }

  private Metadata getUserCreationBidMetadata(
      List<InvitationRequestOrganizationsInner> organizations) {
    final Map<String, Object> meta = Maps.newHashMapWithExpectedSize(1);
    meta.put(BID_TYPE, INTERNAL_BID_TYPE);
    meta.put("organizations", getOrganizationsMetadata(organizations));

    return new Metadata(meta);
  }

  private List<Map<String, Object>> getProjectsMetadata(
      InvitationRequestOrganizationsInner organization) {
    return organization.getProjects().stream()
        .map(project -> {
          Map<String, Object> obj = new HashMap<>();
          obj.put("id", project.getId());
          obj.put("role", project.getProjectRole().name());
          return obj;
        }).toList();
  }


  private List<Map<String, Object>> getOrganizationsMetadata(
      List<InvitationRequestOrganizationsInner> organizations) {
    return organizations.stream()
        .map(org -> {
          Map<String, Object> obj = new HashMap<>();
          obj.put("id", org.getId());
          obj.put("role", org.getOrgRole().name());
          obj.put("projects", getProjectsMetadata(org));
          return obj;
        }).toList();
  }

  private boolean isSsoEnabled() {
    return settingsRepository.findByKey(SERVER_USERS_SSO).map(ServerSettings::getValue)
        .map(Boolean::parseBoolean).orElse(false);
  }

  private ProjectRole resolveProjectRole(OrganizationRole orgRole, String projectRole) {
    return orgRole.equals(OrganizationRole.MANAGER)
        ? ProjectRole.EDITOR
        : ProjectRole.valueOf(
            projectRole);
  }

}
