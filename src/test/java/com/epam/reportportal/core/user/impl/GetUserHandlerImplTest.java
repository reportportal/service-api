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

package com.epam.reportportal.core.user.impl;

import static com.epam.reportportal.ReportPortalUserUtil.getRpUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.infrastructure.persistence.entity.user.UserRole;
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
class GetUserHandlerImplTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private GetUserHandlerImpl handler;

  @Test
  void getNotExistedUserByUsername() {
    when(userRepository.findByLogin("not_exist")).thenReturn(Optional.empty());

    final ReportPortalException exception = assertThrows(
        ReportPortalException.class,
        () -> handler.getUser("not_exist",
            getRpUser("test", UserRole.ADMINISTRATOR, OrganizationRole.MEMBER, ProjectRole.VIEWER, 1L))
    );
    assertEquals("User 'not_exist' not found.", exception.getMessage());
  }

  @Test
  void getNotExistedUserByLoggedInUser() {
    when(userRepository.findByLogin("not_exist")).thenReturn(Optional.empty());

    final ReportPortalException exception = assertThrows(
        ReportPortalException.class,
        () -> handler.getUser(getRpUser("not_exist", UserRole.USER, OrganizationRole.MANAGER, ProjectRole.VIEWER, 1L))
    );
    assertEquals("User 'not_exist' not found.", exception.getMessage());
  }

}
