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

package com.epam.ta.reportportal.core.user.impl;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.epam.reportportal.api.model.NewUserRequest;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.organization.OrganizationRole;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class CreateUserHandlerImplTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private CreateUserHandlerImpl handler;

  @Test
  void createByAdminUserAlreadyExists() {
    final ReportPortalUser rpUser =
        getRpUser("new_user@example.com", UserRole.ADMINISTRATOR, OrganizationRole.MANAGER,
            ProjectRole.EDITOR, 1L);

    doReturn(Optional.of(new User())).when(userRepository).findByEmail("new_user@example.com");

    var request = new NewUserRequest();
    request.setEmail("new_user@example.com");
    final ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.createUser(request, rpUser, "url")
    );
    assertEquals(
        "User with 'email='new_user@example.com'' already exists. You couldn't create the duplicate.",
        exception.getMessage()
    );
  }

  @Test
  void createByAdminWithIncorrectEmail() {
    final ReportPortalUser rpUser =
        getRpUser("admin", UserRole.ADMINISTRATOR, OrganizationRole.MANAGER, ProjectRole.EDITOR,
            1L);

    var request = new NewUserRequest();
    request.setEmail("incorrect.email");
    final ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.createUser(request, rpUser, "url")
    );
    assertEquals(
        "Error in handled Request. Please, check specified parameters: 'wrong email: incorrect.email'",
        exception.getMessage()
    );
  }

  @Test
  void createByAdminWithExistedEmail() {
    final ReportPortalUser rpUser =
        getRpUser("admin", UserRole.ADMINISTRATOR, OrganizationRole.MANAGER, ProjectRole.EDITOR,
            1L);

    when(userRepository.findByEmail("correct@domain.com")).thenReturn(Optional.of(new User()));

    var request = new NewUserRequest();
    request.setEmail("correct@domain.com");
    final ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.createUser(request, rpUser, "url")
    );
    assertEquals(
        "User with 'email='correct@domain.com'' already exists. You couldn't create the duplicate.",
        exception.getMessage()
    );
  }

  @Test
  void createByAdminWithExistedEmailUppercase() {
    final ReportPortalUser rpUser =
        getRpUser("admin", UserRole.ADMINISTRATOR, OrganizationRole.MANAGER, ProjectRole.EDITOR,
            1L);

    when(userRepository.findByEmail("correct@domain.com")).thenReturn(Optional.of(new User()));

    var request = new NewUserRequest();
    request.setEmail("CORRECT@domain.com");
    final ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.createUser(request, rpUser, "url")
    );
    assertEquals(
        "User with 'email='correct@domain.com'' already exists. You couldn't create the duplicate.",
        exception.getMessage()
    );
  }

}
