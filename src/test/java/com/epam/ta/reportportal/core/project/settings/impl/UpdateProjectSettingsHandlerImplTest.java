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

package com.epam.ta.reportportal.core.project.settings.impl;


import static com.epam.ta.reportportal.OrganizationUtil.TEST_PROJECT_KEY;
import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.model.project.config.UpdateIssueSubTypeRQ;
import com.epam.ta.reportportal.model.project.config.UpdateOneIssueSubTypeRQ;
import java.util.Collections;
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
class UpdateProjectSettingsHandlerImplTest {

  @Mock
  private ProjectRepository projectRepository;

  @InjectMocks
  private UpdateProjectSettingsHandlerImpl handler;

  @Test
  void emptyRequest() {
    ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.PROJECT_MANAGER, 1L);

    UpdateIssueSubTypeRQ updateIssueSubTypeRQ = new UpdateIssueSubTypeRQ();
    updateIssueSubTypeRQ.setIds(Collections.emptyList());

    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.updateProjectIssueSubType(TEST_PROJECT_KEY, user, updateIssueSubTypeRQ)
    );
    assertEquals("Forbidden operation. Please specify at least one item data for update.",
        exception.getMessage()
    );
  }

  @Test
  void updateSubtypeOnNotExistProject() {
    long projectId = 1L;
    ReportPortalUser user =
        getRpUser("user", UserRole.USER, ProjectRole.PROJECT_MANAGER, projectId);

    UpdateIssueSubTypeRQ updateIssueSubTypeRQ = new UpdateIssueSubTypeRQ();
    updateIssueSubTypeRQ.setIds(Collections.singletonList(new UpdateOneIssueSubTypeRQ()));

    when(projectRepository.findByKey(TEST_PROJECT_KEY)).thenReturn(Optional.empty());

    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.updateProjectIssueSubType(TEST_PROJECT_KEY, user, updateIssueSubTypeRQ)
    );
    assertEquals("Project 'o-slug-project-name' not found. Did you use correct project name?",
        exception.getMessage()
    );
  }

  @Test
  void updateSubtypeWithIncorrectGroup() {
    long projectId = 1L;
    ReportPortalUser user =
        getRpUser("user", UserRole.USER, ProjectRole.PROJECT_MANAGER, projectId);

    UpdateIssueSubTypeRQ updateIssueSubTypeRQ = new UpdateIssueSubTypeRQ();
    UpdateOneIssueSubTypeRQ oneIssueSubTypeRQ = new UpdateOneIssueSubTypeRQ();
    oneIssueSubTypeRQ.setTypeRef("wrongType");
    updateIssueSubTypeRQ.setIds(Collections.singletonList(oneIssueSubTypeRQ));

    when(projectRepository.findByKey(TEST_PROJECT_KEY)).thenReturn(Optional.of(new Project()));

    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.updateProjectIssueSubType(TEST_PROJECT_KEY, user, updateIssueSubTypeRQ)
    );
    assertEquals("Issue Type 'wrongType' not found.", exception.getMessage());
  }

  @Test
  void updateNotExistSubtype() {
    long projectId = 1L;
    ReportPortalUser user =
        getRpUser("user", UserRole.USER, ProjectRole.PROJECT_MANAGER, projectId);

    UpdateIssueSubTypeRQ updateIssueSubTypeRQ = new UpdateIssueSubTypeRQ();
    UpdateOneIssueSubTypeRQ oneIssueSubTypeRQ = new UpdateOneIssueSubTypeRQ();
    oneIssueSubTypeRQ.setTypeRef("product_bug");
    oneIssueSubTypeRQ.setLocator("locator");
    updateIssueSubTypeRQ.setIds(Collections.singletonList(oneIssueSubTypeRQ));

    when(projectRepository.findByKey(TEST_PROJECT_KEY)).thenReturn(Optional.of(new Project()));

    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.updateProjectIssueSubType(TEST_PROJECT_KEY, user, updateIssueSubTypeRQ)
    );
    assertEquals("Issue Type 'locator' not found.", exception.getMessage());
  }
}
