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

package com.epam.ta.reportportal.ws.converter.builders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.entity.user.UserType;
import com.epam.ta.reportportal.model.user.CreateUserRQConfirm;
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
}