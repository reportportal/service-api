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

package com.epam.reportportal.core.project.impl;

import static com.epam.reportportal.OrganizationUtil.TEST_PROJECT_KEY;
import static com.epam.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.reportportal.infrastructure.persistence.commons.querygen.constant.UserCriteriaConstant.CRITERIA_ROLE;
import static com.epam.reportportal.infrastructure.persistence.util.MembershipUtils.rpUserToMembership;
import static com.epam.reportportal.util.TestProjectExtractor.extractProjectDetails;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.model.user.UserResource;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class GetProjectHandlerImplTest {

  @Mock
  private ProjectRepository projectRepository;

  @Spy
  @InjectMocks
  private GetProjectHandlerImpl handler;

  @Test
  void getUsersOnNotExistProject() {
    long projectId = 1L;
    ReportPortalUser rpUser =
        getRpUser("user", UserRole.USER, OrganizationRole.MANAGER, ProjectRole.EDITOR, projectId);
    String projectKey = TEST_PROJECT_KEY;

    Filter filter = Filter.builder().withTarget(User.class).withCondition(
        FilterCondition.builder().eq(CRITERIA_ROLE, UserRole.USER.name()).build()).build();
    when(projectRepository.findByKey(projectKey)).thenReturn(Optional.empty());

    ReportPortalException exception = assertThrows(ReportPortalException.class, () -> {
      handler.getProjectUsers(rpUserToMembership(rpUser), filter, PageRequest.of(0, 10), rpUser);
    });

    assertEquals(
        "Project 'o-slug.project-name' not found. Did you use correct project name?",
        exception.getMessage()
    );
  }

  @Test
  void getEmptyUserList() {
    long projectId = 1L;
    ReportPortalUser rpUser =
        getRpUser("user", UserRole.USER, OrganizationRole.MANAGER, ProjectRole.EDITOR, projectId);
    String projectKey = TEST_PROJECT_KEY;
    Filter filter = Filter.builder()
        .withTarget(User.class)
        .withCondition(FilterCondition.builder().eq(CRITERIA_ROLE, UserRole.USER.name()).build())
        .build();

    when(projectRepository.findByKey(projectKey)).thenReturn(Optional.of(new Project()));

    Iterable<UserResource> users =
        handler.getProjectUsers(rpUserToMembership(rpUser), filter, PageRequest.of(0, 10), rpUser);

    assertFalse(users.iterator().hasNext());
  }

  @Test
  void getNotExistProject() {
    String projectKey = "not_exist";
    long projectId = 1L;
    ReportPortalUser user =
        getRpUser("user", UserRole.USER, OrganizationRole.MANAGER, ProjectRole.EDITOR,  projectId);

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
        getRpUser("user", UserRole.USER, OrganizationRole.MANAGER, ProjectRole.EDITOR,  projectId);

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
    ReportPortalException exception = assertThrows(
        ReportPortalException.class, () ->
            handler.getUserNames("",
                new MembershipDetails.MembershipDetailsBuilder()
                    .withOrgId(1L)
                    .withOrgRole(OrganizationRole.MANAGER)
                    .withOrgName("org-name")
                    .withProjectId(1L)
                    .withProjectName("superadmin_personal")
                    .withProjectKey("superadmin_personal")
                    .withProjectRole(ProjectRole.EDITOR)
                    .build(),
                UserRole.ADMINISTRATOR,
                PageRequest.of(0, 10)
            ));
    assertEquals(
        "Incorrect filtering parameters. Length of the filtering string '' is less than 1 symbol",
        exception.getMessage()
    );
  }
}
