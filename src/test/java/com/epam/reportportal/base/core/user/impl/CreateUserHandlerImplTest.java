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

package com.epam.reportportal.base.core.user.impl;

import static com.epam.reportportal.base.ReportPortalUserUtil.getRpUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.epam.reportportal.api.model.NewUserRequest;
import com.epam.reportportal.base.core.user.UserMutationService;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
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

  @Mock
  private UserMutationService userMutationService;

  @InjectMocks
  private CreateUserHandlerImpl handler;

  @Test
  void createByAdminUserAlreadyExists() {
    final ReportPortalUser rpUser =
        getRpUser("new_user@example.com", UserRole.ADMINISTRATOR, OrganizationRole.MANAGER,
            ProjectRole.EDITOR, 1L);

    when(userMutationService.normalizeAndValidateEmail("new_user@example.com"))
        .thenReturn("new_user@example.com");
    doThrow(new ReportPortalException(ErrorType.USER_ALREADY_EXISTS, "new_user@example.com"))
        .when(userMutationService).checkEmailUniqueness("new_user@example.com");

    var request = new NewUserRequest();
    request.setEmail("new_user@example.com");
    final ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.createUser(request, rpUser, "url")
    );
    assertEquals(
        "User with 'new_user@example.com' already exists. You couldn't create the duplicate.",
        exception.getMessage()
    );
  }

  @Test
  void createByAdminWithIncorrectEmail() {
    final ReportPortalUser rpUser =
        getRpUser("admin", UserRole.ADMINISTRATOR, OrganizationRole.MANAGER, ProjectRole.EDITOR,
            1L);

    when(userMutationService.normalizeAndValidateEmail("incorrect.email"))
        .thenThrow(
            new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "wrong email: incorrect.email"));

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

    when(userMutationService.normalizeAndValidateEmail("correct@domain.com"))
        .thenReturn("correct@domain.com");
    doThrow(new ReportPortalException(ErrorType.USER_ALREADY_EXISTS, "correct@domain.com"))
        .when(userMutationService).checkEmailUniqueness("correct@domain.com");

    var request = new NewUserRequest();
    request.setEmail("correct@domain.com");
    final ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.createUser(request, rpUser, "url")
    );
    assertEquals(
        "User with 'correct@domain.com' already exists. You couldn't create the duplicate.",
        exception.getMessage()
    );
  }

  @Test
  void createByAdminWithExistedEmailUppercase() {
    final ReportPortalUser rpUser =
        getRpUser("admin", UserRole.ADMINISTRATOR, OrganizationRole.MANAGER, ProjectRole.EDITOR,
            1L);

    when(userMutationService.normalizeAndValidateEmail("CORRECT@domain.com"))
        .thenReturn("correct@domain.com");
    doThrow(new ReportPortalException(ErrorType.USER_ALREADY_EXISTS, "correct@domain.com"))
        .when(userMutationService).checkEmailUniqueness("correct@domain.com");

    var request = new NewUserRequest();
    request.setEmail("CORRECT@domain.com");
    final ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.createUser(request, rpUser, "url")
    );
    assertEquals(
        "User with 'correct@domain.com' already exists. You couldn't create the duplicate.",
        exception.getMessage()
    );
  }

}
