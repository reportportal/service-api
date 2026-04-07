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

package com.epam.reportportal.base.core.organization.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.organization.OrganizationUserService;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser.OrganizationDetails;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepositoryCustom;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationUsersRepositoryCustom;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.OrganizationType;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.Organization;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.OrganizationUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.util.SecurityContextUtils;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class OrganizationUsersHandlerImplTest {

  @Mock
  private OrganizationUsersRepositoryCustom organizationUsersRepositoryCustom;

  @Mock
  private ProjectUserRepository projectUserRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private OrganizationUserRepository organizationUserRepository;

  @Mock
  private OrganizationRepositoryCustom organizationRepositoryCustom;

  @Mock
  private OrganizationUserService organizationUserService;

  @InjectMocks
  private OrganizationUsersHandlerImpl organizationUsersHandler;

  private static final Long ORG_ID = 1L;
  private static final Long USER_ID = 100L;
  private static final String USER_LOGIN = "testuser";
  private static final String USER_EMAIL = "testuser@example.com";

  private Organization organization;
  private OrganizationUser organizationUser;
  private ReportPortalUser reportPortalUser;

  @BeforeEach
  void setUp() {
    User testUser = new User();
    testUser.setId(USER_ID);
    testUser.setLogin(USER_LOGIN);
    testUser.setEmail(USER_EMAIL);
    testUser.setRole(UserRole.USER);

    organization = new Organization();
    organization.setId(ORG_ID);
    organization.setName("Personal Organization");
    organization.setSlug("personal-org");
    organization.setOrganizationType(OrganizationType.PERSONAL);
    organization.setOwnerId(USER_ID);
    organization.setCreatedAt(Instant.now());
    organization.setUpdatedAt(Instant.now());

    organizationUser = new OrganizationUser();
    organizationUser.setUser(testUser);
    organizationUser.setOrganization(organization);
    organizationUser.setOrganizationRole(OrganizationRole.MEMBER);

    Map<String, OrganizationDetails> orgDetails = new HashMap<>();
    OrganizationDetails orgDetail = new OrganizationDetails(
        ORG_ID,
        "Personal Organization",
        OrganizationRole.MEMBER,
        new HashMap<>()
    );
    orgDetails.put(ORG_ID.toString(), orgDetail);

    reportPortalUser = ReportPortalUser.userBuilder()
        .withUserName(USER_LOGIN)
        .withPassword("test")
        .withUserId(USER_ID)
        .withEmail(USER_EMAIL)
        .withUserRole(UserRole.USER)
        .withAuthorities(java.util.Set.of(new SimpleGrantedAuthority(UserRole.USER.getAuthority())))
        .withOrganizationDetails(orgDetails)
        .build();
  }

  @Test
  @DisplayName("User cannot unassign themselves from personal organization")
  void unassignUser_WhenUserTriesToUnassignFromPersonalOrganization_ShouldThrowException() {
    // Given
    when(organizationUserRepository.findByUserIdAndOrganization_Id(USER_ID, ORG_ID))
        .thenReturn(Optional.of(organizationUser));

    try (MockedStatic<SecurityContextUtils> mockedSecurityContext = org.mockito.Mockito.mockStatic(
        SecurityContextUtils.class)) {
      mockedSecurityContext.when(SecurityContextUtils::getPrincipal).thenReturn(reportPortalUser);

      // When & Then
      ReportPortalException exception = assertThrows(ReportPortalException.class,
          () -> organizationUsersHandler.unassignUser(ORG_ID, USER_ID));

      assertEquals(ErrorType.ACCESS_DENIED, exception.getErrorType());
      assertEquals("You do not have enough permissions. User 100 cannot be unassigned from personal organization",
          exception.getMessage());

      verify(projectUserRepository, never()).deleteProjectUserByProjectOrganizationId(anyLong(), anyLong());
      verify(organizationUserRepository, never()).delete(any(OrganizationUser.class));
    }
  }

  @Test
  @DisplayName("User can unassign themselves from non-personal organization")
  void unassignUser_WhenUserTriesToUnassignFromNonPersonalOrganization_ShouldAllowUnassignment() {
    // Given
    organization.setOrganizationType(OrganizationType.INTERNAL);
    organization.setOwnerId(null);

    when(organizationUserRepository.findByUserIdAndOrganization_Id(USER_ID, ORG_ID))
        .thenReturn(Optional.of(organizationUser));
    doNothing().when(organizationUserService).removeOrganizationUserEntry(organizationUser);

    try (MockedStatic<SecurityContextUtils> mockedSecurityContext = org.mockito.Mockito.mockStatic(
        SecurityContextUtils.class)) {
      mockedSecurityContext.when(SecurityContextUtils::getPrincipal).thenReturn(reportPortalUser);

      // When
      organizationUsersHandler.unassignUser(ORG_ID, USER_ID);

      // Then
      verify(organizationUserService).removeOrganizationUserEntry(organizationUser);
    }
  }

  @Test
  @DisplayName("User can unassign another user from personal organization if they have manager role")
  void unassignUser_WhenManagerTriesToUnassignAnotherUserFromPersonalOrganization_ShouldAllowUnassignment() {
    // Given
    Long differentUserId = 200L;
    User differentUser = new User();
    differentUser.setId(differentUserId);
    differentUser.setLogin("differentuser");
    differentUser.setEmail("differentuser@example.com");

    OrganizationUser differentOrgUser = new OrganizationUser();
    differentOrgUser.setUser(differentUser);
    differentOrgUser.setOrganization(organization);
    differentOrgUser.setOrganizationRole(OrganizationRole.MEMBER);

    Map<String, OrganizationDetails> orgDetails = new HashMap<>();
    OrganizationDetails orgDetail = new OrganizationDetails(
        ORG_ID,
        "Personal Organization",
        OrganizationRole.MANAGER,
        new HashMap<>()
    );
    orgDetails.put(ORG_ID.toString(), orgDetail);

    ReportPortalUser managerUser = ReportPortalUser.userBuilder()
        .withUserName(USER_LOGIN)
        .withPassword("test")
        .withUserId(USER_ID)
        .withEmail(USER_EMAIL)
        .withUserRole(UserRole.USER)
        .withAuthorities(java.util.Set.of(new SimpleGrantedAuthority(UserRole.USER.getAuthority())))
        .withOrganizationDetails(orgDetails)
        .build();

    when(organizationUserRepository.findByUserIdAndOrganization_Id(differentUserId, ORG_ID))
        .thenReturn(Optional.of(differentOrgUser));
    doNothing().when(organizationUserService).removeOrganizationUserEntry(differentOrgUser);

    try (MockedStatic<SecurityContextUtils> mockedSecurityContext = org.mockito.Mockito.mockStatic(
        SecurityContextUtils.class)) {
      mockedSecurityContext.when(SecurityContextUtils::getPrincipal).thenReturn(managerUser);

      // When
      organizationUsersHandler.unassignUser(ORG_ID, differentUserId);

      // Then
      verify(organizationUserService).removeOrganizationUserEntry(differentOrgUser);
    }
  }

  @Test
  @DisplayName("User can unassign themselves from personal organization if they are not the owner")
  void unassignUser_WhenUserTriesToUnassignFromPersonalOrganizationAsNonOwner_ShouldAllowUnassignment() {
    // Given
    organization.setOwnerId(300L);

    when(organizationUserRepository.findByUserIdAndOrganization_Id(USER_ID, ORG_ID))
        .thenReturn(Optional.of(organizationUser));
    doNothing().when(organizationUserService).removeOrganizationUserEntry(organizationUser);

    try (MockedStatic<SecurityContextUtils> mockedSecurityContext = org.mockito.Mockito.mockStatic(
        SecurityContextUtils.class)) {
      mockedSecurityContext.when(SecurityContextUtils::getPrincipal).thenReturn(reportPortalUser);

      // When
      organizationUsersHandler.unassignUser(ORG_ID, USER_ID);

      // Then
      verify(organizationUserService).removeOrganizationUserEntry(organizationUser);
    }
  }
}
