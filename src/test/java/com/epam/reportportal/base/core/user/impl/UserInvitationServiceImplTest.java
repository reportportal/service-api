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

import static com.epam.reportportal.base.ReportPortalUserUtil.getRpUser;
import static com.epam.reportportal.base.infrastructure.persistence.entity.enums.ReservedIntegrationTypeEnum.EMAIL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.api.model.InvitationRequest;
import com.epam.reportportal.api.model.InvitationRequestOrganizationsInner;
import com.epam.reportportal.api.model.OrgRole;
import com.epam.reportportal.api.model.ProjectRole;
import com.epam.reportportal.api.model.UserProjectInfo;
import com.epam.reportportal.base.core.integration.GetIntegrationHandler;
import com.epam.reportportal.base.core.launch.util.LinkGenerator;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.ServerSettingsRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserCreationBidRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserCreationBid;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.util.SecurityContextUtils;
import com.epam.reportportal.base.util.email.EmailService;
import com.epam.reportportal.base.util.email.MailServiceFactory;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@ExtendWith(MockitoExtension.class)
class UserInvitationServiceImplTest {

  private static final Long INVITER_ID = 1L;
  private static final Long ORG_ID = 100L;
  private static final Long SECOND_ORG_ID = 101L;
  private static final Long PROJECT_ID = 200L;
  private static final Long SECOND_PROJECT_ID = 201L;
  private static final String EMAIL_TYPE = EMAIL.getName();
  private static final String INVITEE_EMAIL = "invite@example.com";
  private static final String INVITATION_SUBJECT = "User registration confirmation";

  @Mock
  private ApplicationEventPublisher eventPublisher;
  @Mock
  private ThreadPoolTaskExecutor emailExecutorService;
  @Mock
  private MailServiceFactory emailServiceFactory;
  @Mock
  private GetIntegrationHandler getIntegrationHandler;
  @Mock
  private UserCreationBidRepository userCreationBidRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private ServerSettingsRepository settingsRepository;
  @Mock
  private LinkGenerator linkGenerator;

  @InjectMocks
  private UserInvitationServiceImpl userInvitationService;

  private MockedStatic<SecurityContextUtils> securityContextMock;

  @BeforeEach
  void setUp() {
    ReportPortalUser rpUser = getRpUser("inviter", UserRole.USER, OrganizationRole.MEMBER,
        com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole.VIEWER, PROJECT_ID);

    User inviter = new User();
    inviter.setId(INVITER_ID);
    inviter.setLogin("inviter");
    inviter.setFullName("Test Inviter");

    UserCreationBid savedBid = new UserCreationBid();
    savedBid.setUuid("test-uuid-123");
    savedBid.setEmail(INVITEE_EMAIL);
    savedBid.setLastModified(Instant.now());
    savedBid.setInvitingUser(inviter);

    when(settingsRepository.findByKey(anyString())).thenReturn(Optional.empty());
    when(userRepository.findById(INVITER_ID)).thenReturn(Optional.of(inviter));
    when(userCreationBidRepository.save(any())).thenReturn(savedBid);
    when(linkGenerator.generateInvitationUrl(any(), anyString()))
        .thenReturn(URI.create("http://example.com/invite/test-uuid-123"));

    securityContextMock = mockStatic(SecurityContextUtils.class);
    securityContextMock.when(SecurityContextUtils::getPrincipal).thenReturn(rpUser);
  }

  @AfterEach
  void tearDown() {
    securityContextMock.close();
  }

  @Test
  void sendInvitationWhenCrossOrgInviteShouldUseFindFirstEnabledGlobalByTypeName() {
    // Given
    var request = buildRequest(
        orgWithProjects(ORG_ID, List.of()),
        orgWithProjects(SECOND_ORG_ID, List.of()));
    when(getIntegrationHandler.findFirstEnabledGlobalByTypeName(EMAIL_TYPE))
        .thenReturn(Optional.empty());

    // When
    userInvitationService.sendInvitation(request);

    // Then
    verify(getIntegrationHandler).findFirstEnabledGlobalByTypeName(EMAIL_TYPE);
    verify(getIntegrationHandler, never()).findFirstEnabledByTypeName(anyLong(), anyLong(), anyString());
    verify(getIntegrationHandler, never()).findFirstEnabledByOrganizationAndTypeName(anyLong(), anyString());
  }

  @Test
  void sendInvitationWhenEmptyOrgsShouldUseFindFirstEnabledGlobalByTypeName() {
    // Given
    var request = new InvitationRequest();
    request.setEmail(INVITEE_EMAIL);
    request.setOrganizations(List.of());
    when(getIntegrationHandler.findFirstEnabledGlobalByTypeName(EMAIL_TYPE))
        .thenReturn(Optional.empty());

    // When
    userInvitationService.sendInvitation(request);

    // Then
    verify(getIntegrationHandler).findFirstEnabledGlobalByTypeName(EMAIL_TYPE);
    verify(getIntegrationHandler, never()).findFirstEnabledByTypeName(anyLong(), anyLong(), anyString());
  }

  @Test
  void sendInvitationWhenSingleProjectInviteShouldUseFindFirstEnabledByTypeName() {
    // Given
    var request = buildRequest(orgWithProjects(ORG_ID, List.of(projectInfo(PROJECT_ID))));
    when(getIntegrationHandler.findFirstEnabledByTypeName(PROJECT_ID, ORG_ID, EMAIL_TYPE))
        .thenReturn(Optional.empty());

    // When
    userInvitationService.sendInvitation(request);

    // Then
    verify(getIntegrationHandler).findFirstEnabledByTypeName(PROJECT_ID, ORG_ID, EMAIL_TYPE);
    verify(getIntegrationHandler, never()).findFirstEnabledGlobalByTypeName(anyString());
    verify(getIntegrationHandler, never()).findFirstEnabledByOrganizationAndTypeName(anyLong(), anyString());
  }

  @Test
  void sendInvitationWhenOrgLevelInviteShouldUseFindFirstEnabledByOrganizationAndTypeName() {
    // Given
    var request = buildRequest(orgWithProjects(ORG_ID, List.of()));
    when(getIntegrationHandler.findFirstEnabledByOrganizationAndTypeName(ORG_ID, EMAIL_TYPE))
        .thenReturn(Optional.empty());

    // When
    userInvitationService.sendInvitation(request);

    // Then
    verify(getIntegrationHandler).findFirstEnabledByOrganizationAndTypeName(ORG_ID, EMAIL_TYPE);
    verify(getIntegrationHandler, never()).findFirstEnabledGlobalByTypeName(anyString());
    verify(getIntegrationHandler, never()).findFirstEnabledByTypeName(anyLong(), anyLong(), anyString());
  }

  @Test
  void sendInvitationWhenMultiProjectInviteShouldUseFindFirstEnabledByOrganizationAndTypeName() {
    // Given
    var request = buildRequest(orgWithProjects(ORG_ID,
        List.of(projectInfo(PROJECT_ID), projectInfo(SECOND_PROJECT_ID))));
    when(getIntegrationHandler.findFirstEnabledByOrganizationAndTypeName(ORG_ID, EMAIL_TYPE))
        .thenReturn(Optional.empty());

    // When
    userInvitationService.sendInvitation(request);

    // Then
    verify(getIntegrationHandler).findFirstEnabledByOrganizationAndTypeName(ORG_ID, EMAIL_TYPE);
    verify(getIntegrationHandler, never()).findFirstEnabledByTypeName(anyLong(), anyLong(), anyString());
  }

  @Test
  void sendInvitationWhenNoIntegrationFoundShouldNotCallEmailService() {
    // Given
    var request = buildRequest(orgWithProjects(ORG_ID, List.of()));
    when(getIntegrationHandler.findFirstEnabledByOrganizationAndTypeName(ORG_ID, EMAIL_TYPE))
        .thenReturn(Optional.empty());

    // When
    userInvitationService.sendInvitation(request);

    // Then
    verify(emailExecutorService, never()).execute(any());
    verify(emailServiceFactory, never()).getDefaultEmailService(any(Integration.class));
  }

  @Test
  void sendInvitationWhenIntegrationFoundShouldSendEmail() {
    // Given
    var integration = new Integration();
    var emailService = mock(EmailService.class);
    var request = buildRequest(orgWithProjects(ORG_ID, List.of(projectInfo(PROJECT_ID))));

    when(getIntegrationHandler.findFirstEnabledByTypeName(PROJECT_ID, ORG_ID, EMAIL_TYPE))
        .thenReturn(Optional.of(integration));
    when(emailServiceFactory.getDefaultEmailService(integration))
        .thenReturn(Optional.of(emailService));
    doAnswer(inv -> {
      inv.getArgument(0, Runnable.class).run();
      return null;
    }).when(emailExecutorService).execute(any());

    // When
    userInvitationService.sendInvitation(request);

    // Then
    verify(emailService).sendCreateUserConfirmationEmail(
        eq(INVITATION_SUBJECT),
        any(String[].class),
        anyString()
    );
  }

  private InvitationRequest buildRequest(InvitationRequestOrganizationsInner... orgs) {
    var request = new InvitationRequest();
    request.setEmail(INVITEE_EMAIL);
    request.setOrganizations(List.of(orgs));
    return request;
  }

  private InvitationRequestOrganizationsInner orgWithProjects(Long orgId, List<UserProjectInfo> projects) {
    var org = new InvitationRequestOrganizationsInner();
    org.setId(orgId);
    org.setOrgRole(OrgRole.MEMBER);
    org.setProjects(projects);
    return org;
  }

  private UserProjectInfo projectInfo(Long projectId) {
    var proj = new UserProjectInfo();
    proj.setId(projectId);
    proj.setProjectRole(ProjectRole.VIEWER);
    return proj;
  }
}
