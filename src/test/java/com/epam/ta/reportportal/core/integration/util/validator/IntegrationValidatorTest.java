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

package com.epam.ta.reportportal.core.integration.util.validator;

import static com.epam.ta.reportportal.entity.enums.IntegrationAuthFlowEnum.OAUTH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.model.integration.IntegrationRQ;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class IntegrationValidatorTest {

  @Test
  void validateNonGlobalIntegration() {
    Integration integration = new Integration();
    integration.setId(1L);
    integration.setProject(new Project());
    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> IntegrationValidator.validateProjectLevelIntegrationConstraints(new Project(),
            integration)
    );
    assertEquals("Impossible interact with integration. Integration with ID = '1' is not global.",
        exception.getMessage());
  }

  @Test
  void validateIntegrationGroups() {
    Integration integration = new Integration();
    integration.setId(1L);
    IntegrationType type = new IntegrationType();
    type.setName("jira");
    type.setIntegrationGroup(IntegrationGroupEnum.BTS);
    integration.setType(type);

    Project project = new Project();
    Integration projectIntegration = new Integration();
    IntegrationType projectIntegrationType = new IntegrationType();
    projectIntegrationType.setName("jira");
    projectIntegrationType.setIntegrationGroup(IntegrationGroupEnum.BTS);
    projectIntegration.setType(projectIntegrationType);
    project.setIntegrations(Sets.newHashSet(projectIntegration));

    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> IntegrationValidator.validateProjectLevelIntegrationConstraints(project, integration)
    );
    assertEquals(
        "Impossible interact with integration. Global integration with ID = '1' has been found, but you cannot use it, because you have project-level integration(s) of that type",
        exception.getMessage()
    );
  }

  @Test
  void successfullyValidate() {
    Integration integration = new Integration();
    integration.setId(1L);
    IntegrationType type = new IntegrationType();
    type.setName("rally");
    type.setIntegrationGroup(IntegrationGroupEnum.BTS);
    integration.setType(type);

    Project project = new Project();
    Integration projectIntegration = new Integration();
    IntegrationType projectIntegrationType = new IntegrationType();
    projectIntegrationType.setName("jira");
    projectIntegrationType.setIntegrationGroup(IntegrationGroupEnum.BTS);
    projectIntegration.setType(projectIntegrationType);
    project.setIntegrations(Sets.newHashSet(projectIntegration));

    IntegrationValidator.validateProjectLevelIntegrationConstraints(project, integration);
  }

  @ParameterizedTest
  @CsvSource(value = {
      "rally,https://rally1.rallydev.com",
      "rally,https://rally1.rallydev.com/",
      "JIRA Cloud,https://jira.atlassian.com",
      "JIRA Cloud,https://random.atlassian.com/",
      "JIRA Cloud,https://jira.atlassian.net",
      "JIRA Cloud,https://another.atlassian.net/",
      "other, not url at all"
  }, delimiter = ',')
  void validateThirdPartyUrl(String name, String url) {
    Assertions.assertDoesNotThrow(() -> IntegrationValidator
        .validateThirdPartyUrl(getIntegrationRq(url), getIntegrationType(name)));
  }

  @ParameterizedTest
  @CsvSource(value = {
      "rally,http://rally1.rallydev.com",
      "rally,https://zloi.hacker.com",
      "JIRA Cloud,https://atlassian.com/",
      "JIRA Cloud,https://jiraatlassian.com/",
      "JIRA Cloud,https://zloi.hacker.com?jira=fake.atlassian.com",
  }, delimiter = ',')
  void validateThirdPartyUrlFailed(String name, String url) {
    Assertions.assertThrows(ReportPortalException.class, () -> IntegrationValidator
        .validateThirdPartyUrl(getIntegrationRq(url), getIntegrationType(name)));
  }

  private static IntegrationType getIntegrationType(String name) {
    IntegrationType type = new IntegrationType();
    type.setName(name);
    type.setIntegrationGroup(IntegrationGroupEnum.BTS);
    return type;
  }

  private static IntegrationRQ getIntegrationRq(String url) {
    Map<String, Object> params = new HashMap<>();
    params.put("authType", OAUTH);
    params.put("integrationName", "intergration name");
    params.put("url", url);

    IntegrationRQ integrationRq = new IntegrationRQ();
    integrationRq.setEnabled(true);
    integrationRq.setName("test name");
    integrationRq.setIntegrationParams(params);
    return integrationRq;
  }

}
