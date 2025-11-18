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

package com.epam.reportportal.core.user.impl;

import static com.epam.reportportal.core.user.impl.CreateUserHandlerImpl.INTERNAL_BID_TYPE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.api.model.InvitationActivation;
import com.epam.reportportal.auth.authenticator.UserAuthenticator;
import com.epam.reportportal.core.events.activity.AssignUserEvent;
import com.epam.reportportal.core.events.activity.UserCreatedEvent;
import com.epam.reportportal.core.launch.util.LinkGenerator;
import com.epam.reportportal.core.organization.OrganizationUserService;
import com.epam.reportportal.core.organization.PersonalOrganizationService;
import com.epam.reportportal.core.user.UserInvitationService;
import com.epam.reportportal.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.infrastructure.persistence.dao.ProjectUserRepository;
import com.epam.reportportal.infrastructure.persistence.dao.UserCreationBidRepository;
import com.epam.reportportal.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.infrastructure.persistence.dao.organization.OrganizationRepositoryCustom;
import com.epam.reportportal.infrastructure.persistence.entity.Metadata;
import com.epam.reportportal.infrastructure.persistence.entity.enums.OrganizationType;
import com.epam.reportportal.infrastructure.persistence.entity.organization.Organization;
import com.epam.reportportal.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.infrastructure.persistence.entity.user.OrganizationUser;
import com.epam.reportportal.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.infrastructure.persistence.entity.user.UserCreationBid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

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
  private PersonalOrganizationService personalOrganizationService;

  @Mock
  private LinkGenerator linkGenerator;

  @InjectMocks
  private UserInvitationHandler handler;

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

    when(userCreationBidRepository.findByUuidAndType("baf08d9f-72ea-46f6-81ef-3950a34deae9", INTERNAL_BID_TYPE))
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
}
