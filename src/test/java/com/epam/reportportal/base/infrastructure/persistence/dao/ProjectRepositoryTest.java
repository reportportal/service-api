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

package com.epam.reportportal.base.infrastructure.persistence.dao;

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.OrganizationCriteriaConstant.CRITERIA_ORG_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ProjectCriteriaConstant.CRITERIA_PROJECT_NAME;
import static com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectInfo.USERS_QUANTITY;
import static java.util.Optional.ofNullable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.ws.BaseMvcTest;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Condition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.ProjectAttributeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectInfo;
import java.util.List;
import java.util.Optional;
import org.apache.commons.collections4.CollectionUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * @author Ivan Budaev
 */
class ProjectRepositoryTest extends BaseMvcTest {

  @Autowired
  private ProjectRepository projectRepository;

  @Test
  void findAllIdsAndProjectAttributesTest() {

    Page<Project> projects = projectRepository.findAllIdsAndProjectAttributes(PageRequest.of(0, 2));

    assertNotNull(projects);
    assertTrue(CollectionUtils.isNotEmpty(projects.getContent()));
    projects.getContent().forEach(project -> {
      assertNotNull(project.getId());
      assertTrue(CollectionUtils.isNotEmpty(project.getProjectAttributes()));
      assertEquals(16, project.getProjectAttributes().size());
      assertTrue(project.getProjectAttributes()
          .stream()
          .anyMatch(pa -> ofNullable(pa.getValue()).isPresent() && pa.getAttribute()
              .getName()
              .equals(ProjectAttributeEnum.KEEP_LOGS.getAttribute())));
    });
  }

  @Test
  void findByName() {
    final String projectName = "default_personal";

    final Optional<Project> projectOptional = projectRepository.findByName(projectName);

    assertTrue(projectOptional.isPresent());
    assertEquals(projectName, projectOptional.get().getName());
  }

  @Test
  void findByKey() {
    final String projectKey = "default_personal";

    final Optional<Project> projectOptional = projectRepository.findByKey(projectKey);

    assertTrue(projectOptional.isPresent());
    assertEquals(projectKey, projectOptional.get().getKey());
  }

  @Test
  void existsByName() {
    assertTrue(projectRepository.existsByName("default_personal"));
    assertTrue(projectRepository.existsByName("superadmin_personal"));
    assertFalse(projectRepository.existsByName("not_existed"));
  }

  @Test
  void findAllProjectNames() {
    List<String> names = projectRepository.findAllProjectNames();
    assertThat("Incorrect projects size", names, Matchers.hasSize(2));
    assertThat("Results don't contain all project", names,
        Matchers.hasItems("default_personal", "superadmin_personal"));
  }

  @Test
  void findAllProjectNamesByTerm() {
    List<String> names = projectRepository.findAllProjectNamesByTerm("UpEr");
    assertThat("Incorrect projects size", names, Matchers.hasSize(1));
    assertThat("Results don't contain all project", names,
        Matchers.hasItems("superadmin_personal"));
  }

  @Test
  void findUserProjectsTest() {
    List<Project> projects = projectRepository.findUserProjects("default@reportportal.internal");
    assertNotNull(projects);
    assertEquals(1, projects.size());
  }

  @Test
  void findAllByUserLogin() {
    List<Project> projects = projectRepository.findAllByUserLogin("default@reportportal.internal");
    assertNotNull(projects);
    assertEquals(1, projects.size());
  }


  @Test
  void shouldFindProjectByName() {
    final Optional<Project> project = projectRepository.findRawByName("superadmin_personal");
    assertTrue(project.isPresent());
  }

  @Test
  void shouldNotFindProjectByName() {
    final Optional<Project> project = projectRepository.findRawByName("some_random_name");
    assertFalse(project.isPresent());
  }

  @Test
  void findProjectInfoByFilter() {
    final List<ProjectInfo> projectInfos = projectRepository.findProjectInfoByFilter(
        new Filter(ProjectInfo.class,
            Condition.GREATER_THAN_OR_EQUALS,
            false,
            "1",
            USERS_QUANTITY
        ));
    assertEquals(2, projectInfos.size());
    var keyNotNull = projectInfos
        .stream()
        .allMatch(prInfo -> prInfo.getKey() != null);
    assertTrue(keyNotNull);

    var slugNotNull = projectInfos
        .stream()
        .allMatch(prInfo -> prInfo.getSlug() != null);
    assertTrue(slugNotNull);
  }

  @Test
  void findProjectInfoByFilterWithPagination() {
    final Page<ProjectInfo> projectInfoPage = projectRepository.findProjectInfoByFilter(
        new Filter(ProjectInfo.class,
            Condition.EQUALS,
            false,
            "default_personal",
            CRITERIA_PROJECT_NAME
        ), PageRequest.of(0, 10));
    assertEquals(1, projectInfoPage.getTotalElements());
  }

  @Test
  void findProjectInfoByOrgIdFilterWithPagination() {
    final Page<ProjectInfo> projectInfoPage = projectRepository.findProjectInfoByFilter(
        new Filter(ProjectInfo.class,
            Condition.EQUALS,
            false,
            "1",
            CRITERIA_ORG_ID
        ), PageRequest.of(0, 10));
    assertEquals(2, projectInfoPage.getTotalElements());
  }

  @Test
  void findAllByOrganizationId_WhenOrganizationExists_ShouldReturnAllProjectsForOrganization() {
    // Given
    Long organizationId = 1L;
    
    // When
    List<Project> projects = projectRepository.findAllByOrganizationId(organizationId);
    
    // Then
    assertEquals(2, projects.size());
    assertEquals("superadmin_personal", projects.get(0).getName());
    assertEquals("default_personal", projects.get(1).getName());
    assertEquals(organizationId, projects.get(0).getOrganizationId());
    assertEquals(organizationId, projects.get(1).getOrganizationId());
  }

  @Test
  void findAllByOrganizationId_WhenOrganizationDoesNotExist_ShouldReturnEmptyList() {
    // Given
    Long nonExistentOrganizationId = 999L;
    
    // When
    List<Project> projects = projectRepository.findAllByOrganizationId(nonExistentOrganizationId);
    
    // Then
    assertTrue(projects.isEmpty());
  }

}
