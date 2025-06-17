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
import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.rules.commons.validation.Suppliers.formattedSupplier;
import static com.epam.reportportal.rules.exception.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.reportportal.rules.exception.ErrorType.INCORRECT_REQUEST;
import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.core.launch.util.LinkGenerator.generateInvitationUrl;
import static com.epam.ta.reportportal.core.user.impl.CreateUserHandlerImpl.INTERNAL_BID_TYPE;
import static com.epam.ta.reportportal.util.ControllerUtils.safeParseLong;
import static com.epam.ta.reportportal.util.SecurityContextUtils.getPrincipal;
import static com.epam.ta.reportportal.util.email.EmailRulesValidator.NORMALIZE_EMAIL;
import static com.epam.ta.reportportal.ws.converter.builders.UserBuilder.USER_LAST_LOGIN;
import static com.epam.ta.reportportal.ws.converter.converters.UserConverter.TO_ACTIVITY_RESOURCE;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.isEqual;

import com.epam.reportportal.api.model.Invitation;
import com.epam.reportportal.api.model.InvitationActivation;
import com.epam.reportportal.api.model.InvitationRequest;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.auth.authenticator.UserAuthenticator;
import com.epam.ta.reportportal.core.events.activity.UserCreatedEvent;
import com.epam.ta.reportportal.core.organization.OrganizationUserService;
import com.epam.ta.reportportal.core.user.UserInvitationService;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.ProjectUserRepository;
import com.epam.ta.reportportal.dao.UserCreationBidRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.dao.organization.OrganizationRepositoryCustom;
import com.epam.ta.reportportal.entity.Metadata;
import com.epam.ta.reportportal.entity.organization.OrganizationRole;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.OrganizationUser;
import com.epam.ta.reportportal.entity.user.ProjectUser;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserCreationBid;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.entity.user.UserType;
import com.epam.ta.reportportal.ws.converter.converters.InvitationConverter;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link UserInvitationHandler}.
 *
 * @author <a href="mailto:Siarhei_Hrabko@epam.com">Siarhei Hrabko</a>
 */
@Slf4j
@Service
public class UserInvitationHandler {

  private final HttpServletRequest httpServletRequest;
  private final UserCreationBidRepository userCreationBidRepository;
  private final UserRepository userRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final UserAuthenticator userAuthenticator;
  private final ProjectUserRepository projectUserRepository;
  private final OrganizationUserService organizationUserService;
  private final OrganizationRepositoryCustom organizationRepositoryCustom;
  private final ProjectRepository projectRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserInvitationService userInvitationService;

  /**
   * Constructor of UserInvitationHandlerImpl.
   */
  public UserInvitationHandler(HttpServletRequest httpServletRequest,
      UserCreationBidRepository userCreationBidRepository,
      UserRepository userRepository, ApplicationEventPublisher eventPublisher,
      UserAuthenticator userAuthenticator,
      ProjectUserRepository projectUserRepository,
      OrganizationUserService organizationUserService,
      OrganizationRepositoryCustom organizationRepositoryCustom,
      ProjectRepository projectRepository, PasswordEncoder passwordEncoder,
      UserInvitationService userInvitationService) {
    this.httpServletRequest = httpServletRequest;
    this.userCreationBidRepository = userCreationBidRepository;
    this.userRepository = userRepository;
    this.eventPublisher = eventPublisher;
    this.userAuthenticator = userAuthenticator;
    this.projectUserRepository = projectUserRepository;
    this.organizationUserService = organizationUserService;
    this.organizationRepositoryCustom = organizationRepositoryCustom;
    this.projectRepository = projectRepository;
    this.passwordEncoder = passwordEncoder;
    this.userInvitationService = userInvitationService;
  }

  /**
   * Sends invitation for external user or assigns existing user on organizations and projects.
   *
   * @param invitationRq Invitation request
   * @return Operation result
   */
  public Invitation createUserInvitation(InvitationRequest invitationRq) {
    log.debug("User '{}' is trying to create invitation for user '{}'",
        getPrincipal().getUsername(),
        invitationRq.getEmail());
    var userOptional = userRepository.findByEmail(NORMALIZE_EMAIL.apply(invitationRq.getEmail()));
    if (userOptional.isPresent()) {
      return userOptional
          .map(user -> userInvitationService.assignUser(invitationRq, user))
          .get();
    } else {
      return userInvitationService.sendInvitation(invitationRq);
    }
  }

  /**
   * Retrieve an invitation by its ID.
   *
   * @param invitationId the ID of the invitation to retrieve
   * @return the invitation corresponding to the given ID
   */
  public Invitation getInvitation(String invitationId) {
    var bid = userCreationBidRepository.findById(invitationId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND, "User invitation id"));

    var invitation = InvitationConverter.TO_INVITATION.apply(bid);
    invitation.setLink(generateInvitationUrl(httpServletRequest, bid.getUuid()));
    return invitation;
  }

  /**
   * Activate user invitation.
   *
   * @param invitationActivation Invitation activation request
   * @param invitationId         Invitation ID
   * @return Operation result
   */
  public Invitation activate(InvitationActivation invitationActivation, String invitationId) {
    UserCreationBid bid = userCreationBidRepository
        .findByUuidAndType(invitationId, INTERNAL_BID_TYPE)
        .orElseThrow(() -> new ReportPortalException(INCORRECT_REQUEST,
            "Impossible to register user. UUID expired or already registered."));

    var createdUser = saveUser(invitationActivation, bid);
    assignOrganizationsAndProjects(createdUser, bid.getMetadata());
    userCreationBidRepository.deleteByUuid(bid.getUuid());

    var userCreatedEvent = new UserCreatedEvent(
        TO_ACTIVITY_RESOURCE.apply(createdUser, null),
        bid.getInvitingUser().getId(),
        bid.getInvitingUser().getLogin(),
        true
    );
    eventPublisher.publishEvent(userCreatedEvent);

    userAuthenticator.authenticate(createdUser);

    return new Invitation()
        .id(UUID.fromString(invitationId))
        .userId(createdUser.getId())
        .fullName(createdUser.getFullName())
        .email(bid.getEmail())
        .status(ACTIVATED);
  }


  private void assignOrganizationsAndProjects(User createdUser, Metadata metadata) {
    var orgs = metadata.getMetadata().entrySet()
        .stream()
        .filter(entry -> "organizations".equals(entry.getKey()))
        .map(Map.Entry::getValue)
        .flatMap(a -> ((List<Map<String, Object>>) a).stream())
        .toList();

    orgs.forEach(org -> {
      try {
        Long orgId = safeParseLong(org.get("id").toString());
        var organization = organizationRepositoryCustom.findById(orgId)
            .orElseThrow(
                () -> new ReportPortalException(ErrorType.ORGANIZATION_NOT_FOUND, org.get("id")));
        var orgUser =
            organizationUserService.saveOrganizationUser(organization, createdUser, org.get("role").toString());

        assignProjects(createdUser, org, orgUser);
      } catch (Exception e) {
        log.warn("Failed to assign organization {}. {}", org.get("id").toString(), e.getMessage());
      }
    });
  }

  private void assignProjects(User createdUser, Map<String, Object> orgFields, OrganizationUser orgUser) {
    if (orgFields.get("projects") != null) {
      ((List<Map<String, Object>>) orgFields.get("projects"))
          .forEach(project -> {
            Long projectId = safeParseLong(project.get("id").toString());

            var projectEntity = projectRepository.findById(projectId)
                .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectId));
            expect(projectEntity.getOrganizationId(), equalTo(orgUser.getOrganization().getId()))
                .verify(BAD_REQUEST_ERROR,
                    formattedSupplier("Project '{}' does not belong to organization {}",
                        projectId,
                        orgUser.getOrganization().getName())
                );

            var projectUser = projectUserRepository
                .findProjectUserByUserIdAndProjectId(createdUser.getId(), projectId);
            expect(projectUser.isPresent(), isEqual(false))
                .verify(ErrorType.PROJECT_ALREADY_EXISTS, projectEntity.getKey());

            projectUserRepository.save(new ProjectUser()
                .withProject(projectEntity)
                .withProjectRole(calculateProjectRole(orgUser.getOrganizationRole(), project.get("role").toString()))
                .withUser(createdUser));
          });
    }
  }

  private ProjectRole calculateProjectRole(OrganizationRole orgRole, String projectRole) {
    return orgRole.equals(OrganizationRole.MANAGER)
        ? ProjectRole.EDITOR
        : com.epam.ta.reportportal.entity.project.ProjectRole.valueOf(projectRole);
  }

  private User saveUser(InvitationActivation activationRq, UserCreationBid bid) {
    var email = NORMALIZE_EMAIL.apply(bid.getEmail());

    return userRepository.findByEmail(email)
        .orElseGet(() -> {
          var user = new User();
          user.setLogin(email);
          user.setEmail(email);
          user.setUuid(UUID.randomUUID());
          user.setFullName(activationRq.getFullName());
          user.setActive(Boolean.TRUE);
          user.setRole(UserRole.USER);
          user.setUserType(UserType.valueOf("INTERNAL"));
          user.setExpired(false);
          Map<String, Object> meta = new HashMap<>();
          meta.put(USER_LAST_LOGIN, Instant.now().toEpochMilli());
          user.setMetadata(new Metadata(meta));
          ofNullable(activationRq.getPassword())
              .ifPresent(password -> user.setPassword(passwordEncoder.encode(password)));
          return userRepository.save(user);
        });
  }
}
