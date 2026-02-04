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

package com.epam.reportportal.base.core.project.impl;

import static com.epam.reportportal.base.OrganizationUtil.TEST_ORG;
import static com.epam.reportportal.base.OrganizationUtil.TEST_ORG_ID;
import static com.epam.reportportal.base.OrganizationUtil.TEST_PROJECT_KEY;
import static com.epam.reportportal.base.OrganizationUtil.TEST_PROJECT_NAME;
import static com.epam.reportportal.base.ReportPortalUserUtil.getRpUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepositoryCustom;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.project.CreateProjectRQ;
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
class CreateProjectHandlerImplTest {

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  OrganizationRepositoryCustom organizationRepositoryCustom;

  @InjectMocks
  private CreateProjectHandlerImpl handler;


  @Test
  void createProjectByNotExistUser() {
    ReportPortalUser rpUser =
        getRpUser("user", UserRole.ADMINISTRATOR, OrganizationRole.MEMBER,ProjectRole.EDITOR, 1L);

    CreateProjectRQ createProjectRQ = new CreateProjectRQ();
    createProjectRQ.setProjectName(TEST_PROJECT_NAME);
    createProjectRQ.setOrganizationId(TEST_ORG_ID);

    when(organizationRepositoryCustom.findById(TEST_ORG_ID)).thenReturn(Optional.of(TEST_ORG));
    when(projectRepository.findByKey(TEST_PROJECT_KEY)).thenReturn(Optional.empty());
    when(userRepository.findRawById(rpUser.getUserId())).thenReturn(Optional.empty());

    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.createProject(createProjectRQ, rpUser)
    );

    assertEquals("User 'user' not found.", exception.getMessage());
  }
}
