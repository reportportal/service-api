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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.infrastructure.persistence.entity.enums.IntegrationGroupEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.PluginTypeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import com.epam.reportportal.base.ws.BaseMvcTest;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class IntegrationTypeRepositoryTest extends BaseMvcTest {

  private final static String JIRA_INTEGRATION_TYPE_NAME = "jira";
  private final static String ACCESS_TYPE_NAME = "public";
  private static final long BTS_INTEGRATIONS_COUNT = 2L;
  private static final long PUBLIC_INTEGRATIONS_COUNT = 1L;

  @Autowired
  private IntegrationTypeRepository integrationTypeRepository;

  @Test
  void shouldFindWhenNameExists() {
    Optional<IntegrationType> byName = integrationTypeRepository.findByName(
        JIRA_INTEGRATION_TYPE_NAME);
    assertTrue(byName.isPresent());
  }

  @Test
  void shouldFindAllOrderedByCreationDate() {
    List<IntegrationType> integrationTypes = integrationTypeRepository.findAllByOrderByCreationDate();
    assertNotNull(integrationTypes);
    assertFalse(integrationTypes.isEmpty());
  }

  @Test
  void shouldFindAllByIntegrationGroup() {

    List<IntegrationType> integrationTypes = integrationTypeRepository.findAllByIntegrationGroup(
        IntegrationGroupEnum.BTS);

    assertNotNull(integrationTypes);
    assertEquals(BTS_INTEGRATIONS_COUNT, integrationTypes.size());
  }

  @Test
  void shouldFindAllIntegrationTypesByAccessType() {
    List<IntegrationType> integrationTypes = integrationTypeRepository.findAllByAccessType(
        ACCESS_TYPE_NAME);
    assertNotNull(integrationTypes);
    assertEquals(PUBLIC_INTEGRATIONS_COUNT, integrationTypes.size());
  }

  @Test
  void shouldFindAllIntegrationTypesByPluginType() {
    List<IntegrationType> integrationTypes = integrationTypeRepository.findAllByPluginType(
        PluginTypeEnum.BUILT_IN);
    assertNotNull(integrationTypes);
    assertEquals(1, integrationTypes.size());
  }
}
