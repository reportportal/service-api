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

package com.epam.ta.reportportal.core.user.impl;

import static com.epam.reportportal.api.model.InvitationStatus.ACTIVATED;
import static com.epam.reportportal.api.model.InvitationStatus.PENDING;
import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.rules.commons.validation.Suppliers.formattedSupplier;
import static com.epam.reportportal.rules.exception.ErrorType.ACCESS_DENIED;
import static com.epam.reportportal.rules.exception.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.core.launch.util.LinkGenerator.generateInvitationUrl;
import static com.epam.ta.reportportal.core.user.impl.CreateUserHandlerImpl.BID_TYPE;
import static com.epam.ta.reportportal.core.user.impl.CreateUserHandlerImpl.INTERNAL_BID_TYPE;
import static com.epam.ta.reportportal.model.settings.SettingsKeyConstants.SERVER_USERS_SSO;
import static com.epam.ta.reportportal.util.DateTimeProvider.instantNow;
import static com.epam.ta.reportportal.util.OrganizationUserValidator.validateUserType;
import static com.epam.ta.reportportal.util.SecurityContextUtils.getPrincipal;
import static com.epam.ta.reportportal.util.email.EmailRulesValidator.NORMALIZE_EMAIL;

import com.epam.reportportal.api.model.Invitation;
import com.epam.reportportal.api.model.InvitationRequest;
import com.epam.reportportal.api.model.InvitationRequestOrganizationsInner;
import com.epam.reportportal.api.model.UserProjectInfo;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.events.activity.CreateInvitationLinkEvent;
import com.epam.ta.reportportal.core.organization.OrganizationUserService;
import com.epam.ta.reportportal.core.user.UserInvitationService;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.ProjectUserRepository;
import com.epam.ta.reportportal.dao.ServerSettingsRepository;
import com.epam.ta.reportportal.dao.UserCreationBidRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.dao.organization.OrganizationRepositoryCustom;
import com.epam.ta.reportportal.dao.organization.OrganizationUserRepository;
import com.epam.ta.reportportal.entity.Metadata;
import com.epam.ta.reportportal.entity.ServerSettings;
import com.epam.ta.reportportal.entity.organization.OrganizationRole;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.ProjectUser;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserCreationBid;
import com.epam.ta.reportportal.util.email.MailServiceFactory;
import com.google.common.collect.Maps;
import jakarta.servlet.http.HttpServletRequest;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Slf4j
@Service
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


  public UserInvitationServiceImpl(HttpServletRequest httpServletRequest, ThreadPoolTaskExecutor emailExecutorService,
      MailServiceFactory emailServiceFactory, UserRepository userRepository,
      UserCreationBidRepository userCreationBidRepository,
      ApplicationEventPublisher eventPublisher, ProjectUserRepository projectUserRepository,
      OrganizationUserRepository organizationUserRepository,
      OrganizationUserService organizationUserService, OrganizationRepositoryCustom organizationRepositoryCustom,
      ProjectRepository projectRepository, ServerSettingsRepository settingsRepository) {
    this.httpServletRequest = httpServletRequest;
    this.emailExecutorService = emailExecutorService;
    this.emailServiceFactory = emailServiceFactory;
    this.userRepository = userRepository;
    this.userCreationBidRepository = userCreationBidRepository;
    this.eventPublisher = eventPublisher;
    this.projectUserRepository = projectUserRepository;
    this.organizationUserRepository = organizationUserRepository;
    this.organizationUserService = organizationUserService;
    this.organizationRepositoryCustom = organizationRepositoryCustom;
    this.projectRepository = projectRepository;
    this.settingsRepository = settingsRepository;
  }

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
    invitation.setLink(generateInvitationUrl(httpServletRequest, userBid.getUuid()));
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

    // TODO: Add org IDs to event publisher. Needs to refactor ActivityEvent.
    eventPublisher.publishEvent(new CreateInvitationLinkEvent(rpUser.getUserId(), rpUser.getUsername()));
    return invitation;
  }

  @Override
  public Invitation assignUser(InvitationRequest invitationRq, User userToAssign) {
    invitationRq.getOrganizations().forEach(orgInfo -> {
      var organization = organizationRepositoryCustom.findById(orgInfo.getId())
          .orElseThrow(() -> new ReportPortalException(ErrorType.ORGANIZATION_NOT_FOUND, orgInfo.getId()));
      organizationUserRepository.findByUserIdAndOrganization_Id(userToAssign.getId(), organization.getId())
          .orElseGet(() -> {
            validateUserType(organization, userToAssign);
            return organizationUserService.saveOrganizationUser(organization, userToAssign, OrganizationRole.MEMBER.toString());
          });

      assignProjects(orgInfo.getProjects(), organization.getId(), userToAssign);
    });

    var invitation = new Invitation();
    invitation.setCreatedAt(instantNow());
    invitation.setEmail(userToAssign.getEmail());
    invitation.setStatus(ACTIVATED);
    invitation.setUserId(userToAssign.getId());
    invitation.setFullName(userToAssign.getFullName());
    return invitation;
  }

  private void assignProjects(List<UserProjectInfo> projects, Long orgId, User user) {
    projects.forEach(project -> {
      var projectEntity = projectRepository.findById(project.getId())
          .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, project.getId()));
      expect(projectEntity.getOrganizationId(), equalTo(orgId))
          .verify(BAD_REQUEST_ERROR, formattedSupplier("Project '{}' does not belong to organization {}", project.getId(), orgId));

      projectUserRepository.findProjectUserByUserIdAndProjectId(user.getId(), project.getId())
          .orElseGet(() -> projectUserRepository.save(new ProjectUser()
              .withProject(projectEntity)
              .withProjectRole(ProjectRole.valueOf(project.getProjectRole().toString()))
              .withUser(user)));
    });

  }

  private Metadata getUserCreationBidMetadata(List<InvitationRequestOrganizationsInner> organizations) {
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

}
