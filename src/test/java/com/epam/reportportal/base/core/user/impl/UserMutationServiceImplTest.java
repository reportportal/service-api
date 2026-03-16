/*
 * Copyright 2026 EPAM Systems
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.events.domain.ChangeUserTypeEvent;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class UserMutationServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @InjectMocks
  private UserMutationServiceImpl userMutationService;

  private User user;
  private ReportPortalUser adminEditor;
  private ReportPortalUser regularEditor;

  @BeforeEach
  void setUp() {
    adminEditor = getRpUser("admin", UserRole.ADMINISTRATOR, OrganizationRole.MANAGER, ProjectRole.EDITOR, 2L);
    regularEditor = getRpUser("regular", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.VIEWER, 3L);
    user = new User();
    user.setId(1L);
    user.setLogin("user@example.com");
    user.setEmail("user@example.com");
    user.setFullName("Test User");
    user.setRole(UserRole.USER);
    user.setActive(true);
    user.setUserType(UserType.INTERNAL);
    user.setExternalId("ext-123");
  }

  @Nested
  @DisplayName("updateEmail")
  class UpdateEmail {

    @Test
    @DisplayName("Should reject non-admin changing external user email")
    void updateEmailWhenNonAdminAndExternalUserShouldThrow() {
      user.setUserType(UserType.LDAP);

      assertThatThrownBy(
          () -> userMutationService.updateEmail(user, "new@example.com", regularEditor))
          .isInstanceOf(ReportPortalException.class)
          .hasMessageContaining("Unable to change email for external user");
    }

    @Test
    @DisplayName("Should allow admin to change external user email")
    void updateEmailWhenAdminAndExternalUserShouldUpdate() {
      user.setUserType(UserType.LDAP);
      user.setEmail("ldap@example.com");
      user.setLogin("ldap@example.com");
      when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
      when(projectRepository.findUserProjects("ldap@example.com")).thenReturn(
          Collections.emptyList());

      userMutationService.updateEmail(user, "new@example.com", adminEditor);

      assertThat(user.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    @DisplayName("Should reject blank email")
    void updateEmailWhenBlankShouldThrow() {
      assertThatThrownBy(
          () -> userMutationService.updateEmail(user, "  ", adminEditor))
          .isInstanceOf(ReportPortalException.class);
    }

    @Test
    @DisplayName("Should reject null email")
    void updateEmailWhenNullShouldThrow() {
      assertThatThrownBy(
          () -> userMutationService.updateEmail(user, null, adminEditor))
          .isInstanceOf(ReportPortalException.class);
    }

    @Test
    @DisplayName("Should reject invalid email format")
    void updateEmailWhenInvalidFormatShouldThrow() {
      assertThatThrownBy(
          () -> userMutationService.updateEmail(user, "not-an-email", adminEditor))
          .isInstanceOf(ReportPortalException.class);
    }

    @Test
    @DisplayName("Should reject duplicate email")
    void updateEmailWhenDuplicateShouldThrow() {
      var existingUser = new User();
      existingUser.setId(99L);
      when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.of(existingUser));

      assertThatThrownBy(
          () -> userMutationService.updateEmail(user, "new@example.com", adminEditor))
          .isInstanceOf(ReportPortalException.class)
          .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should skip update when email is same")
    void updateEmailWhenSameEmailShouldNoOp() {
      userMutationService.updateEmail(user, "user@example.com", adminEditor);

      verify(userRepository, never()).findByEmail(any());
      assertThat(user.getEmail()).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("Should update email and sync login")
    void updateEmailWhenValidShouldUpdateEmailAndLogin() {
      when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
      when(projectRepository.findUserProjects("user@example.com")).thenReturn(
          Collections.emptyList());

      userMutationService.updateEmail(user, "new@example.com", adminEditor);

      assertThat(user.getEmail()).isEqualTo("new@example.com");
      assertThat(user.getLogin()).isEqualTo("new@example.com");
    }
  }

  @Nested
  @DisplayName("updateFullName")
  class UpdateFullName {

    @Test
    @DisplayName("Should reject non-admin changing external user full name")
    void updateFullNameWhenNonAdminAndExternalUserShouldThrow() {
      user.setUserType(UserType.GITHUB);

      assertThatThrownBy(
          () -> userMutationService.updateFullName(user, "New Name", regularEditor))
          .isInstanceOf(ReportPortalException.class)
          .hasMessageContaining("Unable to change full name for external user");
    }

    @Test
    @DisplayName("Should allow admin to change external user full name")
    void updateFullNameWhenAdminAndExternalUserShouldUpdate() {
      user.setUserType(UserType.GITHUB);

      userMutationService.updateFullName(user, "New Name", adminEditor);

      assertThat(user.getFullName()).isEqualTo("New Name");
    }

    @Test
    @DisplayName("Should reject blank full name")
    void updateFullNameWhenBlankShouldThrow() {
      assertThatThrownBy(
          () -> userMutationService.updateFullName(user, "  ", adminEditor))
          .isInstanceOf(ReportPortalException.class)
          .hasMessageContaining("must not be empty");
    }

    @Test
    @DisplayName("Should reject full name shorter than 3 characters")
    void updateFullNameWhenTooShortShouldThrow() {
      assertThatThrownBy(
          () -> userMutationService.updateFullName(user, "Ab", adminEditor))
          .isInstanceOf(ReportPortalException.class)
          .hasMessageContaining("length must be between");
    }

    @Test
    @DisplayName("Should reject full name longer than 60 characters")
    void updateFullNameWhenTooLongShouldThrow() {
      var longName = "A".repeat(61);
      assertThatThrownBy(
          () -> userMutationService.updateFullName(user, longName, adminEditor))
          .isInstanceOf(ReportPortalException.class)
          .hasMessageContaining("length must be between");
    }

    @Test
    @DisplayName("Should reject full name with invalid characters")
    void updateFullNameWhenInvalidCharsShouldThrow() {
      assertThatThrownBy(
          () -> userMutationService.updateFullName(user, "Name<script>", adminEditor))
          .isInstanceOf(ReportPortalException.class)
          .hasMessageContaining("may only contain");
    }

    @Test
    @DisplayName("Should accept valid full name")
    void updateFullNameWhenValidShouldUpdate() {
      userMutationService.updateFullName(user, "John O'Brien-Smith", adminEditor);

      assertThat(user.getFullName()).isEqualTo("John O'Brien-Smith");
    }
  }

  @Nested
  @DisplayName("updateInstanceRole")
  class UpdateInstanceRole {

    @Test
    @DisplayName("Should reject blank role")
    void updateInstanceRoleWhenBlankShouldThrow() {
      assertThatThrownBy(
          () -> userMutationService.updateInstanceRole(user, "  ", adminEditor))
          .isInstanceOf(ReportPortalException.class)
          .hasMessageContaining("must not be empty");
    }

    @Test
    @DisplayName("Should reject null role")
    void updateInstanceRoleWhenNullShouldThrow() {
      assertThatThrownBy(
          () -> userMutationService.updateInstanceRole(user, null, adminEditor))
          .isInstanceOf(ReportPortalException.class)
          .hasMessageContaining("must not be empty");
    }

    @Test
    @DisplayName("Should reject invalid role value")
    void updateInstanceRoleWhenInvalidShouldThrow() {
      assertThatThrownBy(
          () -> userMutationService.updateInstanceRole(user, "SUPER_USER", adminEditor))
          .isInstanceOf(ReportPortalException.class)
          .hasMessageContaining("Incorrect specified Account Role");
    }

    @Test
    @DisplayName("Should update role and publish event")
    void updateInstanceRoleWhenValidShouldUpdateAndPublishEvent() {
      userMutationService.updateInstanceRole(user, "ADMINISTRATOR", adminEditor);

      assertThat(user.getRole()).isEqualTo(UserRole.ADMINISTRATOR);
      verify(eventPublisher).publishEvent(any(ChangeUserTypeEvent.class));
    }
  }

  @Nested
  @DisplayName("updateActive")
  class UpdateActive {

    @Test
    @DisplayName("Should reject null value")
    void updateActiveWhenNullShouldThrow() {
      assertThatThrownBy(() -> userMutationService.updateActive(user, null))
          .isInstanceOf(ReportPortalException.class)
          .hasMessageContaining("must not be null");
    }

    @Test
    @DisplayName("Should reject non-boolean value")
    void updateActiveWhenNonBooleanShouldThrow() {
      assertThatThrownBy(() -> userMutationService.updateActive(user, "yes"))
          .isInstanceOf(ReportPortalException.class)
          .hasMessageContaining("must be a boolean");
    }

    @Test
    @DisplayName("Should accept valid boolean")
    void updateActiveWhenValidShouldUpdate() {
      userMutationService.updateActive(user, false);

      assertThat(user.getActive()).isFalse();
    }
  }

  @Nested
  @DisplayName("updateAccountType")
  class UpdateAccountType {

    @Test
    @DisplayName("Should reject blank account type")
    void updateAccountTypeWhenBlankShouldThrow() {
      assertThatThrownBy(() -> userMutationService.updateAccountType(user, "  "))
          .isInstanceOf(ReportPortalException.class)
          .hasMessageContaining("must not be empty");
    }

    @Test
    @DisplayName("Should reject null account type")
    void updateAccountTypeWhenNullShouldThrow() {
      assertThatThrownBy(() -> userMutationService.updateAccountType(user, null))
          .isInstanceOf(ReportPortalException.class)
          .hasMessageContaining("must not be empty");
    }

    @Test
    @DisplayName("Should reject UPSA account type")
    void updateAccountTypeWhenUpsaShouldThrow() {
      assertThatThrownBy(() -> userMutationService.updateAccountType(user, "UPSA"))
          .isInstanceOf(ReportPortalException.class)
          .hasMessageContaining("can only be set to INTERNAL or SCIM");
    }

    @Test
    @DisplayName("Should reject LDAP account type")
    void updateAccountTypeWhenLdapShouldThrow() {
      assertThatThrownBy(() -> userMutationService.updateAccountType(user, "LDAP"))
          .isInstanceOf(ReportPortalException.class)
          .hasMessageContaining("can only be set to INTERNAL or SCIM");
    }

    @Test
    @DisplayName("Should reject SAML account type")
    void updateAccountTypeWhenSamlShouldThrow() {
      assertThatThrownBy(() -> userMutationService.updateAccountType(user, "SAML"))
          .isInstanceOf(ReportPortalException.class)
          .hasMessageContaining("can only be set to INTERNAL or SCIM");
    }

    @Test
    @DisplayName("Should reject GITHUB account type")
    void updateAccountTypeWhenGithubShouldThrow() {
      assertThatThrownBy(() -> userMutationService.updateAccountType(user, "GITHUB"))
          .isInstanceOf(ReportPortalException.class)
          .hasMessageContaining("can only be set to INTERNAL or SCIM");
    }

    @Test
    @DisplayName("Should reject invalid account type")
    void updateAccountTypeWhenInvalidShouldThrow() {
      assertThatThrownBy(() -> userMutationService.updateAccountType(user, "UNKNOWN"))
          .isInstanceOf(ReportPortalException.class)
          .hasMessageContaining("Incorrect specified Account Type");
    }

    @Test
    @DisplayName("Should accept INTERNAL account type")
    void updateAccountTypeWhenInternalShouldUpdate() {
      userMutationService.updateAccountType(user, "INTERNAL");

      assertThat(user.getUserType()).isEqualTo(UserType.INTERNAL);
    }

    @Test
    @DisplayName("Should accept SCIM account type")
    void updateAccountTypeWhenScimShouldUpdate() {
      userMutationService.updateAccountType(user, "SCIM");

      assertThat(user.getUserType()).isEqualTo(UserType.SCIM);
    }
  }

  @Nested
  @DisplayName("updateExternalId")
  class UpdateExternalId {

    @Test
    @DisplayName("Should reject blank external ID")
    void updateExternalIdWhenBlankShouldThrow() {
      assertThatThrownBy(() -> userMutationService.updateExternalId(user, "  "))
          .isInstanceOf(ReportPortalException.class)
          .hasMessageContaining("must not be empty");
    }

    @Test
    @DisplayName("Should reject duplicate external ID")
    void updateExternalIdWhenDuplicateShouldThrow() {
      var existingUser = new User();
      existingUser.setId(99L);
      when(userRepository.findByExternalId("dup-ext")).thenReturn(Optional.of(existingUser));

      assertThatThrownBy(() -> userMutationService.updateExternalId(user, "dup-ext"))
          .isInstanceOf(ReportPortalException.class)
          .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should allow setting same external ID on same user")
    void updateExternalIdWhenSameUserShouldSucceed() {
      when(userRepository.findByExternalId("ext-123")).thenReturn(Optional.of(user));

      userMutationService.updateExternalId(user, "ext-123");

      assertThat(user.getExternalId()).isEqualTo("ext-123");
    }

    @Test
    @DisplayName("Should accept valid external ID")
    void updateExternalIdWhenValidShouldUpdate() {
      when(userRepository.findByExternalId("new-ext")).thenReturn(Optional.empty());

      userMutationService.updateExternalId(user, "new-ext");

      assertThat(user.getExternalId()).isEqualTo("new-ext");
    }
  }
}
