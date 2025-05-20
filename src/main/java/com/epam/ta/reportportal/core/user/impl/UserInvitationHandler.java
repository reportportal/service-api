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
import static com.epam.reportportal.rules.exception.ErrorType.INCORRECT_REQUEST;
import static com.epam.reportportal.rules.exception.ErrorType.USER_ALREADY_EXISTS;
import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.model.settings.SettingsKeyConstants.SERVER_USERS_SSO;
import static com.epam.ta.reportportal.util.ControllerUtils.safeParseLong;
import static com.epam.ta.reportportal.util.email.EmailRulesValidator.NORMALIZE_EMAIL;
import static com.epam.ta.reportportal.ws.converter.builders.UserBuilder.USER_LAST_LOGIN;
import static com.epam.ta.reportportal.ws.converter.converters.UserConverter.TO_ACTIVITY_RESOURCE;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.isEqual;

import com.epam.reportportal.api.model.Invitation;
import com.epam.reportportal.api.model.InvitationActivation;
import com.epam.reportportal.api.model.InvitationRequest;
import com.epam.reportportal.api.model.InvitationRequestOrganizationsInner;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.auth.authenticator.UserAuthenticator;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.activity.CreateInvitationLinkEvent;
import com.epam.ta.reportportal.core.events.activity.UserCreatedEvent;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.ProjectUserRepository;
import com.epam.ta.reportportal.dao.ServerSettingsRepository;
import com.epam.ta.reportportal.dao.UserCreationBidRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.dao.organization.OrganizationRepositoryCustom;
import com.epam.ta.reportportal.dao.organization.OrganizationUserRepository;
import com.epam.ta.reportportal.entity.Metadata;
import com.epam.ta.reportportal.entity.ServerSettings;
import com.epam.ta.reportportal.entity.organization.Organization;
import com.epam.ta.reportportal.entity.organization.OrganizationRole;
import com.epam.ta.reportportal.entity.user.OrganizationUser;
import com.epam.ta.reportportal.entity.user.ProjectUser;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserCreationBid;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.entity.user.UserType;
import com.epam.ta.reportportal.util.email.MailServiceFactory;
import com.epam.ta.reportportal.ws.converter.converters.InvitationConverter;
import com.google.common.collect.Maps;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link UserInvitationHandler}.
 *
 * @author <a href="mailto:Siarhei_Hrabko@epam.com">Siarhei Hrabko</a>
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class UserInvitationHandler {

  public static final String BID_TYPE = "type";
  public static final String INTERNAL_BID_TYPE = "internal";

  private final UserCreationBidRepository userCreationBidRepository;
  private final ThreadPoolTaskExecutor emailExecutorService;
  private final MailServiceFactory emailServiceFactory;
  private final UserRepository userRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final ServerSettingsRepository settingsRepository;
  private final UserAuthenticator userAuthenticator;
  private final ProjectUserRepository projectUserRepository;
  private final OrganizationUserRepository organizationUserRepository;
  private final OrganizationRepositoryCustom organizationRepositoryCustom;
  private final ProjectRepository projectRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * Create user bid (send invitation).
   *
   * @param request Create Request
   * @param rpUser  User that creates the request
   * @param baseUrl User registration url
   * @return Operation result
   */
  public Invitation createUserInvitation(
      InvitationRequest request,
      ReportPortalUser rpUser,
      String baseUrl
  ) {
    if (isSsoEnabled()) {
      throw new ReportPortalException(ACCESS_DENIED, "Cannot invite user if SSO enabled.");
    }

    log.debug("User '{}' is trying to create invitation for user '{}'",
        rpUser.getUsername(),
        request.getEmail());

    validateInvitationRequest(request);

    var user = userRepository.findById(rpUser.getUserId()).orElseThrow(
        () -> new ReportPortalException(ErrorType.USER_NOT_FOUND, rpUser.getUserId()));

    UserCreationBid userBid = new UserCreationBid();
    userBid.setUuid(UUID.randomUUID().toString());
    userBid.setEmail(request.getEmail().trim());
    userBid.setInvitingUser(user);
    userBid.setMetadata(getUserCreationBidMetadata(request.getOrganizations()));

    UserCreationBid storedUserBid;
    try {
      storedUserBid = userCreationBidRepository.save(userBid);
    } catch (Exception e) {
      throw new ReportPortalException("Error while user creation bid registering.", e);
    }

    var link = getEmailLink(baseUrl, userBid.getUuid());
    var response = new Invitation();
    response.setCreatedAt(storedUserBid.getLastModified());
    response.setExpiresAt(storedUserBid.getLastModified().plus(1, ChronoUnit.DAYS));
    response.setId(UUID.fromString(userBid.getUuid()));
    response.setLink(link);
    response.setEmail(request.getEmail());
    response.setStatus(PENDING);
    response.setUserId(user.getId());
    response.setFullName(user.getFullName());

    //  TODO: Add search organization integrations
    emailExecutorService.execute(() -> emailServiceFactory.getDefaultEmailService(false)
        .sendCreateUserConfirmationEmail(
            "User registration confirmation",
            new String[]{userBid.getEmail()}, link.toString()
        ));

    // TODO: Add org IDs to event publisher. Needs to refactor ActivityEvent.
    eventPublisher.publishEvent(
        new CreateInvitationLinkEvent(rpUser.getUserId(), rpUser.getUsername()));

    return response;
  }

  /**
   * Retrieve an invitation by its ID.
   *
   * @param invitationId the ID of the invitation to retrieve
   * @param baseUrl      the base url of the webservice
   * @return the invitation corresponding to the given ID
   */
  public Invitation getInvitation(String invitationId, String baseUrl) {
    var bid = userCreationBidRepository.findById(invitationId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND, "User invitation id"));

    var invitation = InvitationConverter.TO_INVITATION.apply(bid);
    invitation.setLink(getEmailLink(baseUrl, bid.getUuid()));
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
    userCreationBidRepository.delete(bid);

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

  private void saveOrganizationUser(Organization organization, User assignedUser, String role) {
    var organizationUser = new OrganizationUser();
    organizationUser.setOrganization(organization);
    organizationUser.setUser(assignedUser);
    organizationUser.setOrganizationRole(OrganizationRole.valueOf(role));
    organizationUserRepository.save(organizationUser);
  }

  private void validateInvitationRequest(InvitationRequest request) {
    var email = NORMALIZE_EMAIL.apply(request.getEmail());

    expect(userRepository.findByEmail(email).isEmpty(), equalTo(true)).verify(
        USER_ALREADY_EXISTS, formattedSupplier("email='{}'", request.getEmail()));
  }

  private Metadata getUserCreationBidMetadata(
      @Valid List<InvitationRequestOrganizationsInner> organizations) {
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

  private URI getEmailLink(String baseUrl, String invitationId) {
    return URI.create(baseUrl + "/ui/#registration?uuid=" + invitationId);
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
        saveOrganizationUser(organization, createdUser, org.get("role").toString());

        // assign user to projects
        assignProjects(createdUser, org, orgId);
      } catch (Exception e) {
        log.warn("Failed to assign organization {}. {}", org.get("id").toString(), e.getMessage());
      }
    });
  }

  private void assignProjects(User createdUser, Map<String, Object> org, Long orgId) {
    if (org.get("projects") != null) {
      ((List<Map<String, Object>>) org.get("projects"))
          .forEach(project -> {
            Long projectId = safeParseLong(project.get("id").toString());

            var projectEntity = projectRepository.findById(projectId)
                .orElseThrow(
                    () -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectId));
            expect(projectEntity.getOrganizationId(), equalTo(orgId)).verify(BAD_REQUEST_ERROR,
                formattedSupplier("Project '{}' does not belong to organization {}", projectId,
                    orgId)
            );

            var projectUser = projectUserRepository
                .findProjectUserByUserIdAndProjectId(createdUser.getId(), projectId);
            expect(projectUser.isPresent(), isEqual(false))
                .verify(ErrorType.PROJECT_ALREADY_EXISTS, projectEntity.getKey());

            projectUserRepository.save(new ProjectUser()
                .withProject(projectEntity)
                .withProjectRole(com.epam.ta.reportportal.entity.project.ProjectRole.valueOf(
                    project.get("role").toString()))
                .withUser(createdUser));
          });
    }
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
