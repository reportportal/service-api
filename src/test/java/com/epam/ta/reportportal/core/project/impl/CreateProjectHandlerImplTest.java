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

package com.epam.ta.reportportal.core.project.impl;

import static com.epam.ta.reportportal.OrganizationUtil.TEST_ORG;
import static com.epam.ta.reportportal.OrganizationUtil.TEST_ORG_ID;
import static com.epam.ta.reportportal.OrganizationUtil.TEST_PROJECT_KEY;
import static com.epam.ta.reportportal.OrganizationUtil.TEST_PROJECT_NAME;
import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.dao.organization.OrganizationRepositoryCustom;
import com.epam.ta.reportportal.entity.organization.OrganizationRole;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.model.project.CreateProjectRQ;
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
