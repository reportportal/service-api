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

import static com.epam.ta.reportportal.OrganizationUtil.TEST_PROJECT_KEY;
import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.commons.querygen.constant.UserCriteriaConstant.CRITERIA_ROLE;
import static com.epam.ta.reportportal.util.TestProjectExtractor.extractProjectDetails;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.model.user.UserResource;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {"rp.environment.variable.user.suggestions=true"})
class GetProjectHandlerImplTest {

  @Mock
  private ProjectRepository projectRepository;

  @Spy
  @InjectMocks
  private GetProjectHandlerImpl handler;

  @Test
  void getUsersOnNotExistProject() {
    long projectId = 1L;

    String projectKey = TEST_PROJECT_KEY;
    when(projectRepository.findByKey(projectKey)).thenReturn(Optional.empty());

    ReportPortalException exception = assertThrows(ReportPortalException.class, () -> {
      handler.getProjectUsers(projectKey, Filter.builder().withTarget(User.class).withCondition(
              FilterCondition.builder().eq(CRITERIA_ROLE, UserRole.USER.name()).build()).build(),
          PageRequest.of(0, 10)
      );
    });

    assertEquals(
        "Project 'o-slug.project-name' not found. Did you use correct project name?",
        exception.getMessage()
    );
  }

  @Test
  void getEmptyUserList() {
    long projectId = 1L;

    String projectKey = TEST_PROJECT_KEY;
    when(projectRepository.findByKey(projectKey)).thenReturn(Optional.of(new Project()));

    Iterable<UserResource> users =
        handler.getProjectUsers(projectKey, Filter.builder().withTarget(User.class).withCondition(
                FilterCondition.builder().eq(CRITERIA_ROLE, UserRole.USER.name()).build()).build(),
            PageRequest.of(0, 10)
        );

    assertFalse(users.iterator().hasNext());
  }

  @Test
  void getNotExistProject() {
    String projectKey = "not_exist";
    long projectId = 1L;
    ReportPortalUser user =
        getRpUser("user", UserRole.USER, ProjectRole.PROJECT_MANAGER, projectId);

    when(projectRepository.findByKey(projectKey)).thenReturn(Optional.empty());

    ReportPortalException exception =
        assertThrows(ReportPortalException.class, () -> handler.getResource(projectKey, user));

    assertEquals(
        "Project '" + projectKey + "' not found. Did you use correct project name?",
        exception.getMessage()
    );
  }

  @Test
  void getUserNamesByIncorrectTerm() {
    long projectId = 1L;
    ReportPortalUser user =
        getRpUser("user", UserRole.USER, ProjectRole.PROJECT_MANAGER, projectId);

    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> handler.getUserNames(extractProjectDetails(user, TEST_PROJECT_KEY), "")
    );

    assertEquals(
        "Incorrect filtering parameters. Length of the filtering string '' is less than 1 symbol",
        exception.getMessage()
    );
  }

	@Test
	void getUserNamesNegative() {
		ReportPortalException exception = assertThrows(ReportPortalException.class, () -> handler.getUserNames("", UserRole.ADMINISTRATOR,
				new ReportPortalUser.ProjectDetails(1L, "superadmin_personal", ProjectRole.PROJECT_MANAGER, "superadmin_personal"),
				PageRequest.of(0, 10)));
		assertEquals("Incorrect filtering parameters. Length of the filtering string '' is less than 1 symbol", exception.getMessage());
	}
}
