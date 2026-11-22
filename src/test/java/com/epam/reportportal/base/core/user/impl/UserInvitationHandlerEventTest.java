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

import static com.epam.reportportal.base.core.user.impl.CreateUserHandlerImpl.INTERNAL_BID_TYPE;
import static com.epam.reportportal.base.model.settings.SettingsKeyConstants.SERVER_USERS_SSO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.api.model.InvitationActivation;
import com.epam.reportportal.auth.event.UiAuthenticationFailureEventHandler;
import com.epam.reportportal.base.auth.authenticator.UserAuthenticator;
import com.epam.reportportal.base.core.events.domain.AssignUserEvent;
import com.epam.reportportal.base.core.events.domain.UserCreatedEvent;
import com.epam.reportportal.base.core.launch.util.LinkGenerator;
import com.epam.reportportal.base.core.organization.OrganizationUserService;
import com.epam.reportportal.base.core.user.InvitationActivationRefusedException;
import com.epam.reportportal.base.core.user.PasswordPolicyService;
import com.epam.reportportal.base.core.user.UserInvitationService;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ServerSettingsRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserCreationBidRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepositoryCustom;
import com.epam.reportportal.base.infrastructure.persistence.entity.Metadata;
import com.epam.reportportal.base.infrastructure.persistence.entity.ServerSettings;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.OrganizationType;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.Organization;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.OrganizationUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserCreationBid;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserType;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Test for UserInvitationHandler event publishing when user activates invitation link.
 *
 */
@ExtendWith(MockitoExtension.class)
class UserInvitationHandlerEventTest {

  @Mock
  private UserCreationBidRepository userCreationBidRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @Mock
  private UserAuthenticator userAuthenticator;

  @Mock
  private ProjectUserRepository projectUserRepository;

  @Mock
  private OrganizationUserService organizationUserService;

  @Mock
  private OrganizationRepositoryCustom organizationRepositoryCustom;

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private UserInvitationService userInvitationService;

  @Mock
  private LinkGenerator linkGenerator;

  @Mock
  private PasswordPolicyService passwordPolicyService;

  @Mock
  private ServerSettingsRepository settingsRepository;

  @Mock
  private UiAuthenticationFailureEventHandler failureEventHandler;

  @InjectMocks
  private UserInvitationHandler handler;

  private static final String BID_UUID = "baf08d9f-72ea-46f6-81ef-3950a34deae9";

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(handler, "invitationBidTtl", Duration.ofHours(24));
  }

  @Test
  void getInvitationShouldNotExposePendingEmailChange() {
    when(userCreationBidRepository.findByUuidAndType(BID_UUID, INTERNAL_BID_TYPE)).thenReturn(Optional.empty());

    var exception = assertThrows(ReportPortalException.class, () -> handler.getInvitation(BID_UUID));

    assertEquals(ErrorType.NOT_FOUND, exception.getErrorType());
    verify(userCreationBidRepository, never()).findById(anyString());
  }

  @Test
  void activateShouldNotAcceptPendingEmailChangeToken() {
    when(userCreationBidRepository.findByUuidAndType(BID_UUID, INTERNAL_BID_TYPE)).thenReturn(Optional.empty());

    var exception = assertThrows(ReportPortalException.class,
        () -> handler.activate(new InvitationActivation(), BID_UUID));

    assertEquals(ErrorType.INCORRECT_REQUEST, exception.getErrorType());
    verify(userCreationBidRepository, never()).deleteByUuid(anyString());
  }

  @Test
  void activateInvitationWithProjectShouldPublishAssignUserToOrgAndProjectEvents() {
    // given
    InvitationActivation activation = new InvitationActivation();
    activation.setFullName("Test User");
    activation.setPassword("Password123!");

    User invitingUser = new User();
    invitingUser.setId(1L);
    invitingUser.setLogin("inviter@example.com");

    Map<String, Object> projectMetadata = new HashMap<>();
    projectMetadata.put("id", "200");
    projectMetadata.put("role", "VIEWER");

    Map<String, Object> orgMetadata = new HashMap<>();
    orgMetadata.put("id", "100");
    orgMetadata.put("role", "MEMBER");
    orgMetadata.put("projects", List.of(projectMetadata));

    UserCreationBid bid = new UserCreationBid();
    bid.setUuid("baf08d9f-72ea-46f6-81ef-3950a34deae9");
    bid.setEmail("newuser@example.com");
    bid.setInvitingUser(invitingUser);
    bid.setMetadata(new Metadata(Map.of("organizations", List.of(orgMetadata))));
    bid.setLastModified(Instant.now());

    User createdUser = new User();
    createdUser.setId(2L);
    createdUser.setLogin("newuser@example.com");
    createdUser.setEmail("newuser@example.com");
    createdUser.setFullName("Test User");

    Organization organization = new Organization();
    organization.setId(100L);
    organization.setName("Test Organization");
    organization.setOrganizationType(OrganizationType.INTERNAL);

    OrganizationUser orgUser = new OrganizationUser();
    orgUser.setOrganization(organization);
    orgUser.setUser(createdUser);
    orgUser.setOrganizationRole(OrganizationRole.MANAGER);

    Project project = new Project();
    project.setId(200L);
    project.setName("Test Project");
    project.setOrganizationId(100L);

    when(userCreationBidRepository.findByUuidAndType("baf08d9f-72ea-46f6-81ef-3950a34deae9",
        INTERNAL_BID_TYPE))
        .thenReturn(Optional.of(bid));
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenReturn(createdUser);
    when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
    when(organizationRepositoryCustom.findById(100L)).thenReturn(Optional.of(organization));
    when(organizationUserService.saveOrganizationUser(any(), any(), anyString())).thenReturn(
        orgUser);
    when(projectRepository.findById(200L)).thenReturn(Optional.of(project));
    when(projectUserRepository.findProjectUserByUserIdAndProjectId(anyLong(), anyLong()))
        .thenReturn(Optional.empty());

    // when
    handler.activate(activation, "baf08d9f-72ea-46f6-81ef-3950a34deae9");

    // then
    verify(eventPublisher).publishEvent(any(UserCreatedEvent.class));
    verify(eventPublisher, times(2)).publishEvent(any(AssignUserEvent.class));
  }

  @Test
  void activateInvitationWhenPasswordViolatesPolicyShouldThrowAndNotCreateUser() {
    // given
    InvitationActivation activation = new InvitationActivation();
    activation.setFullName("Test User");
    activation.setPassword("Pass123+");

    UserCreationBid bid = new UserCreationBid();
    bid.setUuid("baf08d9f-72ea-46f6-81ef-3950a34deae9");
    bid.setEmail("newuser@example.com");
    bid.setLastModified(Instant.now());

    when(userCreationBidRepository.findByUuidAndType("baf08d9f-72ea-46f6-81ef-3950a34deae9",
        INTERNAL_BID_TYPE))
        .thenReturn(Optional.of(bid));
    doThrow(new ReportPortalException(ErrorType.INCORRECT_REQUEST,
        "[Field 'password' should be at least 12 characters long.]"))
        .when(passwordPolicyService).validate("Pass123+");

    // when + then
    assertThrows(ReportPortalException.class,
        () -> handler.activate(activation, "baf08d9f-72ea-46f6-81ef-3950a34deae9"));

    verify(passwordPolicyService).validate("Pass123+");
    verify(userRepository, never()).save(any(User.class));
    verify(eventPublisher, never()).publishEvent(any());
  }
  @Test
  void activateExpiredInvitationShouldThrowAndNotCreateUser() {
    var bid = newBid(Instant.now().minus(25, ChronoUnit.HOURS));
    when(userCreationBidRepository.findByUuidAndType(BID_UUID, INTERNAL_BID_TYPE)).thenReturn(Optional.of(bid));

    var ex = assertThrows(ReportPortalException.class, () -> handler.activate(activation(), BID_UUID));

    assertEquals(ErrorType.INCORRECT_REQUEST, ex.getErrorType());
    verify(userRepository, never()).save(any(User.class));
    verify(organizationUserService, never()).saveOrganizationUser(any(), any(), anyString());
  }

  @Test
  void activateInvitationWithoutIssueTimeShouldBeTreatedAsExpired() {
    var bid = newBid(null);
    when(userCreationBidRepository.findByUuidAndType(BID_UUID, INTERNAL_BID_TYPE)).thenReturn(Optional.of(bid));

    assertThrows(ReportPortalException.class, () -> handler.activate(activation(), BID_UUID));
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void activateInvitationWhenSsoEnabledSinceIssueShouldThrowAndNotCreateUser() {
    var bid = newBid(Instant.now());
    when(userCreationBidRepository.findByUuidAndType(BID_UUID, INTERNAL_BID_TYPE)).thenReturn(Optional.of(bid));
    var sso = new ServerSettings();
    sso.setKey(SERVER_USERS_SSO);
    sso.setValue("true");
    when(settingsRepository.findByKey(SERVER_USERS_SSO)).thenReturn(Optional.of(sso));

    var ex = assertThrows(ReportPortalException.class, () -> handler.activate(activation(), BID_UUID));

    assertEquals(ErrorType.ACCESS_DENIED, ex.getErrorType());
    verify(userRepository, never()).save(any(User.class));
    verify(organizationUserService, never()).saveOrganizationUser(any(), any(), anyString());
  }

  @Test
  void activateInvitationForExistingAccountWithWrongPasswordShouldNotAttachAssignments() {
    var bid = newBid(Instant.now());
    when(userCreationBidRepository.findByUuidAndType(BID_UUID, INTERNAL_BID_TYPE)).thenReturn(Optional.of(bid));
    var existing = existingUser(UserType.INTERNAL);
    when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.of(existing));
    when(passwordEncoder.matches("Password123!", "existing-hash")).thenReturn(false);

    var ex = assertThrows(ReportPortalException.class, () -> handler.activate(activation(), BID_UUID));

    assertInstanceOf(InvitationActivationRefusedException.class, ex);
    assertEquals(ErrorType.ACCESS_DENIED, ex.getErrorType());
    verify(organizationUserService, never()).saveOrganizationUser(any(), any(), anyString());
    verify(userRepository, never()).save(any(User.class));
    verify(userCreationBidRepository, never()).deleteByUuid(anyString());
    verify(eventPublisher, never()).publishEvent(any());
    verify(failureEventHandler).recordFailure(any(), eq("newuser@example.com"));
    verify(userCreationBidRepository).incrementActivationFailures(BID_UUID, INTERNAL_BID_TYPE);
    verify(userCreationBidRepository).deleteIfActivationFailuresReached(BID_UUID, INTERNAL_BID_TYPE,
        UserInvitationHandler.MAX_ACTIVATION_FAILURES);
  }

  @Test
  void activateInvitationForExistingAccountWhenLoginIsBlockedShouldAnswerLikeLoginAndNotCheckPassword() {
    var bid = newBid(Instant.now());
    when(userCreationBidRepository.findByUuidAndType(BID_UUID, INTERNAL_BID_TYPE)).thenReturn(Optional.of(bid));
    when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.of(existingUser(UserType.INTERNAL)));
    when(failureEventHandler.isBlocked(any(), eq("newuser@example.com"))).thenReturn(true);

    var ex = assertThrows(ReportPortalException.class, () -> handler.activate(activation(), BID_UUID));

    assertEquals(ErrorType.ADDRESS_LOCKED, ex.getErrorType());
    verify(passwordEncoder, never()).matches(any(), any());
    verify(failureEventHandler, never()).recordFailure(any(), any());
    verify(userCreationBidRepository, never()).incrementActivationFailures(anyString(), anyString());
    verify(organizationUserService, never()).saveOrganizationUser(any(), any(), anyString());
  }

  @Test
  void activateInvitationForInactiveAccountWithItsPasswordShouldBeRefusedLikeWrongPassword() {
    var bid = newBid(Instant.now());
    when(userCreationBidRepository.findByUuidAndType(BID_UUID, INTERNAL_BID_TYPE)).thenReturn(Optional.of(bid));
    var existing = existingUser(UserType.INTERNAL);
    existing.setActive(false);
    when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.of(existing));
    when(passwordEncoder.matches("Password123!", "existing-hash")).thenReturn(true);

    var ex = assertThrows(ReportPortalException.class, () -> handler.activate(activation(), BID_UUID));

    assertInstanceOf(InvitationActivationRefusedException.class, ex);
    assertEquals(new InvitationActivationRefusedException().getMessage(), ex.getMessage());
    verify(organizationUserService, never()).saveOrganizationUser(any(), any(), anyString());
    verify(failureEventHandler).recordFailure(any(), eq("newuser@example.com"));
    verify(userCreationBidRepository).incrementActivationFailures(BID_UUID, INTERNAL_BID_TYPE);
  }

  @Test
  void activateInvitationForExistingExternalAccountShouldNotAttachAssignments() {
    var bid = newBid(Instant.now());
    when(userCreationBidRepository.findByUuidAndType(BID_UUID, INTERNAL_BID_TYPE)).thenReturn(Optional.of(bid));
    var existing = existingUser(UserType.SAML);
    when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.of(existing));
    when(passwordEncoder.encode(anyString())).thenReturn("dummy-hash");

    var ex = assertThrows(ReportPortalException.class, () -> handler.activate(activation(), BID_UUID));

    assertInstanceOf(InvitationActivationRefusedException.class, ex);
    verify(organizationUserService, never()).saveOrganizationUser(any(), any(), anyString());
    // same hash comparison as for an internal account, so the refusal takes as long
    verify(passwordEncoder).matches("Password123!", "dummy-hash");
    verify(failureEventHandler).recordFailure(any(), eq("newuser@example.com"));
    verify(userCreationBidRepository).incrementActivationFailures(BID_UUID, INTERNAL_BID_TYPE);
  }

  @Test
  void getInvitationShouldReportExpiryFromConfiguredTtl() {
    ReflectionTestUtils.setField(handler, "invitationBidTtl", Duration.ofHours(72));
    var issuedAt = Instant.parse("2026-11-01T10:00:00Z");
    when(userCreationBidRepository.findByUuidAndType(BID_UUID, INTERNAL_BID_TYPE))
        .thenReturn(Optional.of(newBid(issuedAt)));

    var invitation = handler.getInvitation(BID_UUID);

    assertEquals(issuedAt.plus(Duration.ofHours(72)), invitation.getExpiresAt());
  }

  @Test
  void activateInvitationForExistingAccountWithItsPasswordShouldAttachAssignmentsWithoutCreatingUser() {
    var bid = newBid(Instant.now());
    when(userCreationBidRepository.findByUuidAndType(BID_UUID, INTERNAL_BID_TYPE)).thenReturn(Optional.of(bid));
    var existing = existingUser(UserType.INTERNAL);
    when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.of(existing));
    when(passwordEncoder.matches("Password123!", "existing-hash")).thenReturn(true);
    var organization = new Organization();
    organization.setId(100L);
    organization.setOrganizationType(OrganizationType.INTERNAL);
    var orgUser = new OrganizationUser();
    orgUser.setOrganization(organization);
    orgUser.setUser(existing);
    orgUser.setOrganizationRole(OrganizationRole.MEMBER);
    when(organizationRepositoryCustom.findById(100L)).thenReturn(Optional.of(organization));
    when(organizationUserService.saveOrganizationUser(organization, existing, "MEMBER")).thenReturn(orgUser);

    var invitation = handler.activate(activation(), BID_UUID);

    assertEquals(5L, invitation.getUserId());
    verify(organizationUserService).saveOrganizationUser(organization, existing, "MEMBER");
    verify(passwordPolicyService, never()).validate(anyString());
    verify(userRepository, never()).save(any(User.class));
    verify(eventPublisher, never()).publishEvent(any(UserCreatedEvent.class));
    verify(userCreationBidRepository).deleteByUuid(BID_UUID);
  }

  private static InvitationActivation activation() {
    var activation = new InvitationActivation();
    activation.setFullName("Test User");
    activation.setPassword("Password123!");
    return activation;
  }

  private static UserCreationBid newBid(Instant issuedAt) {
    var invitingUser = new User();
    invitingUser.setId(1L);
    invitingUser.setLogin("inviter@example.com");

    Map<String, Object> orgMetadata = new HashMap<>();
    orgMetadata.put("id", "100");
    orgMetadata.put("role", "MEMBER");

    var bid = new UserCreationBid();
    bid.setUuid(BID_UUID);
    bid.setEmail("newuser@example.com");
    bid.setInvitingUser(invitingUser);
    bid.setMetadata(new Metadata(Map.of("organizations", List.of(orgMetadata))));
    bid.setLastModified(issuedAt);
    return bid;
  }

  private static User existingUser(UserType type) {
    var user = new User();
    user.setId(5L);
    user.setLogin("newuser@example.com");
    user.setEmail("newuser@example.com");
    user.setUserType(type);
    user.setPassword(type == UserType.INTERNAL ? "existing-hash" : null);
    user.setActive(true);
    return user;
  }
}
