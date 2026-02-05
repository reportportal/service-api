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

import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.TestConstants.DEFAULT_PERSONAL_PROJECT_ID;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.TestConstants.GLOBAL_EMAIL_INTEGRATION_ID;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.TestConstants.SUPERADMIN_PERSONAL_PROJECT_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.infrastructure.persistence.entity.enums.IntegrationGroupEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.ws.BaseMvcTest;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Sql("/db/fill/integration/integrations-fill.sql")
class IntegrationRepositoryTest extends BaseMvcTest {

  private static final long GLOBAL_EMAIL_INTEGRATIONS_COUNT = 1L;
  private static final long SUPERADMIN_PROJECT_BTS_INTEGRATIONS_COUNT = 4L;
  private static final int GLOBAL_BTS_INTEGRATIONS_COUNT = 3;

  private static final Long RALLY_INTEGRATION_TYPE_ID = 5L;
  private static final Long JIRA_INTEGRATION_TYPE_ID = 6L;

  private static final Long RALLY_INTEGRATION_ID = 7L;
  private static final Long JIRA_INTEGRATION_ID = 13L;

  @Autowired
  private IntegrationRepository integrationRepository;

  @Autowired
  private IntegrationTypeRepository integrationTypeRepository;

  @Test
  void shouldUpdateEnabledStateByIntegrationId() {

    integrationRepository.updateEnabledStateById(true, RALLY_INTEGRATION_ID);

    Integration after = integrationRepository.findById(RALLY_INTEGRATION_ID).get();

    assertTrue(after.isEnabled());

  }

  @Test
  void shouldUpdateEnabledStateByIntegrationTypeId() {

    IntegrationType integrationType = integrationTypeRepository.findById(JIRA_INTEGRATION_TYPE_ID)
        .get();

    integrationRepository.updateEnabledStateByIntegrationTypeId(true, integrationType.getId());

    List<Integration> enabledAfter = integrationRepository.findAllByProjectIdAndTypeOrderByCreationDateDesc(
        DEFAULT_PERSONAL_PROJECT_ID, integrationType);

    enabledAfter.forEach(integration -> assertTrue(integration.isEnabled()));
  }

  @Test
  void findByIdAndTypeId() {
    final Integration jiraIntegration = integrationRepository.findByIdAndTypeIdAndProjectIdIsNull(
        JIRA_INTEGRATION_ID, JIRA_INTEGRATION_TYPE_ID).get();
    assertEquals("jira", jiraIntegration.getName());
    assertEquals(JIRA_INTEGRATION_ID, jiraIntegration.getId());
    assertEquals(JIRA_INTEGRATION_TYPE_ID, jiraIntegration.getType().getId());
  }

  @Test
  void findByNameAndTypeId() {
    final Integration jiraIntegration = integrationRepository.findByNameAndTypeIdAndProjectIdIsNull(
        "jira", JIRA_INTEGRATION_TYPE_ID).get();
    assertEquals("jira", jiraIntegration.getName());
    assertEquals(JIRA_INTEGRATION_ID, jiraIntegration.getId());
    assertEquals(JIRA_INTEGRATION_TYPE_ID, jiraIntegration.getType().getId());
  }

  @Test
  void shouldFindAllByProjectIdAndIntegrationTypeWhenExists() {

    IntegrationType integrationType = integrationTypeRepository.findById(JIRA_INTEGRATION_TYPE_ID)
        .get();

    List<Integration> integrations = integrationRepository.findAllByProjectIdAndTypeOrderByCreationDateDesc(
        DEFAULT_PERSONAL_PROJECT_ID, integrationType);

    assertNotNull(integrations);
    assertEquals(1L, integrations.size());
  }

  @Test
  void shouldDeleteAllGlobalByIntegrationTypeId() {

    IntegrationType integrationType = integrationTypeRepository.findById(JIRA_INTEGRATION_TYPE_ID)
        .get();

    integrationRepository.deleteAllGlobalByIntegrationTypeId(integrationType.getId());

    assertThat(integrationRepository.findAllGlobalByType(integrationType), is(empty()));

    assertThat(integrationRepository.findAllByProjectIdAndTypeOrderByCreationDateDesc(
        DEFAULT_PERSONAL_PROJECT_ID, integrationType), is(not(empty())));
    assertThat(integrationRepository.findAllByProjectIdAndTypeOrderByCreationDateDesc(
        SUPERADMIN_PERSONAL_PROJECT_ID, integrationType), is(not(empty())));
  }

  @Test
  void shouldDeleteAllByProjectIdAndIntegrationTypeId() {

    IntegrationType integrationType = integrationTypeRepository.findById(JIRA_INTEGRATION_TYPE_ID)
        .get();

    integrationRepository.deleteAllByProjectIdAndIntegrationTypeId(SUPERADMIN_PERSONAL_PROJECT_ID,
        integrationType.getId());

    assertThat(integrationRepository.findAllByProjectIdAndTypeOrderByCreationDateDesc(
        SUPERADMIN_PERSONAL_PROJECT_ID, integrationType), is(empty()));
    assertThat(integrationRepository.findAllByProjectIdAndTypeOrderByCreationDateDesc(
        DEFAULT_PERSONAL_PROJECT_ID, integrationType), is(not(empty())));
  }

  @Test
  void findAllGlobal() {
    List<Integration> global = integrationRepository.findAllGlobal();
    assertThat(global, hasSize(4));
    global.forEach(it -> assertThat(it.getProject(), equalTo(null)));
  }

  @Test
  void existsByNameTypePositive() {
    boolean exists = integrationRepository.existsByNameIgnoreCaseAndTypeIdAndProjectIdIsNull("jira", 6L);
    assertTrue(exists);
  }

  @Test
  void existsByNameTypeNegative() {
    boolean exists = integrationRepository.existsByNameIgnoreCaseAndTypeIdAndProjectIdIsNull("jira1", 4L);
    assertFalse(exists);
  }

  @Test
  void existsByNameTypeProjectIdPositive() {
    boolean exists = integrationRepository.existsByNameIgnoreCaseAndTypeIdAndProjectId("jira1", 6L, 1L);
    assertTrue(exists);
    exists = integrationRepository.existsByNameIgnoreCaseAndTypeIdAndProjectId("JiRa1", 6L, 1L);
    assertTrue(exists);
  }

  @Test
  void existsByNameTypeProjectIdNegative() {
    boolean exists = integrationRepository.existsByNameIgnoreCaseAndTypeIdAndProjectId("jira", 6L, 2L);
    assertFalse(exists);
  }

  @Test
  void findAllGlobalByIntegrationGroup() {
    List<Integration> integrations = integrationRepository.findAllGlobalByGroup(
        IntegrationGroupEnum.BTS);
    assertThat(integrations, hasSize(GLOBAL_BTS_INTEGRATIONS_COUNT));
    integrations.forEach(
        it -> assertThat(it.getType().getIntegrationGroup(), equalTo(IntegrationGroupEnum.BTS)));
  }

  @Test
  void findAllProjectByIntegrationGroup() {
    Project project = new Project();
    project.setId(1L);
    List<Integration> integrations = integrationRepository.findAllProjectByGroup(project,
        IntegrationGroupEnum.BTS);
    assertThat(integrations, hasSize(4));
  }

  @Test
  void shouldFindAllGlobalByIntegrationType() {

    IntegrationType integrationType = integrationTypeRepository.findById(2L).get();

    List<Integration> globalEmailIntegrations = integrationRepository.findAllGlobalByType(
        integrationType);

    assertNotNull(globalEmailIntegrations);
    assertEquals(GLOBAL_EMAIL_INTEGRATIONS_COUNT, globalEmailIntegrations.size());

    globalEmailIntegrations.forEach(i -> assertNull(i.getProject()));
  }

  @Test
  void shouldFindGlobalBtsIntegrationByUrlAndLinkedProject() {

    Optional<Integration> globalBtsIntegration = integrationRepository.findGlobalBtsByUrlAndLinkedProject(
        "bts.com", "bts_project");

    assertTrue(globalBtsIntegration.isPresent());
    assertNull(globalBtsIntegration.get().getProject());
  }

  @Test
  void shouldFindProjectBtsIntegrationByUrlAndLinkedProject() {

    Optional<Integration> projectBtsIntegration = integrationRepository.findProjectBtsByUrlAndLinkedProject(
        "projectbts.com",
        "project",
        SUPERADMIN_PERSONAL_PROJECT_ID
    );

    assertTrue(projectBtsIntegration.isPresent());
    assertNotNull(projectBtsIntegration.get().getProject());
  }

  @Test
  void shouldFindGlobalIntegrationById() {

    Optional<Integration> globalIntegration = integrationRepository.findGlobalById(GLOBAL_EMAIL_INTEGRATION_ID);

    assertTrue(globalIntegration.isPresent());
    assertNull(globalIntegration.get().getProject());
  }

  @Test
  void shouldFindAllProjectIntegrationsByProjectIdAndIntegrationTypeIds() {

    List<Long> integrationTypeIds = integrationTypeRepository.findAllByIntegrationGroup(
            IntegrationGroupEnum.BTS)
        .stream()
        .map(IntegrationType::getId)
        .collect(Collectors.toList());

    List<Integration> integrations = integrationRepository.findAllByProjectIdAndInIntegrationTypeIds(
        SUPERADMIN_PERSONAL_PROJECT_ID,
        integrationTypeIds
    );

    assertNotNull(integrations);
    assertEquals(SUPERADMIN_PROJECT_BTS_INTEGRATIONS_COUNT, integrations.size());

    integrations.forEach(i -> assertNotNull(i.getProject()));
  }

  @Test
  void shouldFindAllGlobalInIntegrationTypeIds() {

    List<Long> integrationTypeIds = integrationTypeRepository.findAllByIntegrationGroup(
            IntegrationGroupEnum.BTS)
        .stream()
        .map(IntegrationType::getId)
        .collect(Collectors.toList());

    List<Integration> integrations = integrationRepository.findAllGlobalInIntegrationTypeIds(
        integrationTypeIds);

    assertNotNull(integrations);
    assertEquals(GLOBAL_BTS_INTEGRATIONS_COUNT, integrations.size());

    integrations.forEach(i -> assertNull(i.getProject()));
  }

  @Test
  void shouldFindAllGlobalProjectIntegrationsNotInIntegrationTypeIds() {

    List<Long> integrationTypeIds = integrationTypeRepository.findAllByIntegrationGroup(
            IntegrationGroupEnum.BTS)
        .stream()
        .map(IntegrationType::getId)
        .collect(Collectors.toList());

    List<Integration> integrations = integrationRepository.findAllGlobalNotInIntegrationTypeIds(
        integrationTypeIds);

    assertNotNull(integrations);
    assertEquals(GLOBAL_EMAIL_INTEGRATIONS_COUNT, integrations.size());

    integrations.forEach(i -> assertNull(i.getProject()));
  }

  @Test
  void findAllPredefinedIntegrations() {
    List<String> predefinedIntegrationTypes = Arrays.asList("jira", "rally", "email", "saucelabs");
    List<Integration> integrations = integrationRepository.findAllByTypeIn("jira", "rally", "email",
        "saucelabs");
    assertNotNull(integrations);
    assertTrue(CollectionUtils.isNotEmpty(integrations));
    integrations.stream().map(it -> it.getType().getName())
        .map(predefinedIntegrationTypes::contains).forEach(Assertions::assertTrue);
  }
}
