/*
 * Copyright 2019 EPAM Systems
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

package com.epam.reportportal.base.ws.converter.builders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.api.model.InstanceRole;
import com.epam.reportportal.api.model.NewUserRequest;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserType;
import com.epam.reportportal.base.model.user.CreateUserRQConfirm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class UserBuilderTest {

  @Test
  void userBuilder() {
    final CreateUserRQConfirm request = new CreateUserRQConfirm();
    final String email = "email@domain.com";
    request.setEmail(email);
    final String fullName = "full name";
    request.setFullName(fullName);
    request.setPassword("password");
    final UserRole role = UserRole.USER;

    final User user = new UserBuilder().addCreateUserRQ(request).addUserRole(role)
        .addPassword(request.getPassword()).get();

    assertEquals(email, user.getEmail());
    assertEquals(fullName, user.getFullName());
    assertNotNull(user.getPassword());
    assertEquals(role, user.getRole());
    assertEquals(UserType.INTERNAL, user.getUserType());
    assertNotNull(user.getMetadata());
    assertNotNull(user.getUuid());
    assertTrue(user.getActive());
    assertFalse(user.isExpired());
  }

  @Test
  @DisplayName("Should default instance role to USER when instance_role is omitted")
  void fromNewUserRequestWhenInstanceRoleNullShouldDefaultToUserRole() {
    // Given
    var request = new NewUserRequest("user@example.com", "Test User");

    // When
    var user = new UserBuilder().fromNewUserRequest(request).get();

    // Then
    assertNotNull(user);
    assertEquals("user@example.com", user.getEmail());
    assertEquals("Test User", user.getFullName());
    assertEquals(UserRole.USER, user.getRole());
    assertEquals(UserType.INTERNAL, user.getUserType());
  }

  @Test
  @DisplayName("Should use provided instance role when instance_role is set")
  void fromNewUserRequestWhenInstanceRoleProvidedShouldUseIt() {
    // Given
    var request = new NewUserRequest("admin@example.com", "Admin User");
    request.setInstanceRole(InstanceRole.ADMINISTRATOR);

    // When
    var user = new UserBuilder().fromNewUserRequest(request).get();

    // Then
    assertNotNull(user);
    assertEquals(UserRole.ADMINISTRATOR, user.getRole());
  }
}
