/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.core.integration.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import com.epam.reportportal.base.core.plugin.PluginBox;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationParams;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CiTriggerIntegrationServiceTest {

  private final PluginBox pluginBox = mock(PluginBox.class);

  private CiTriggerIntegrationService sut;

  @BeforeEach
  void setUp() {
    sut = new CiTriggerIntegrationService(mock(IntegrationRepository.class), pluginBox,
        mock(IntegrationParamsEncryptor.class));
  }

  @Test
  void retrieveParams_ShouldReturnThemAsGivenWithoutAskingForAPlugin() {
    Map<String, Object> params = new HashMap<>(Map.of("token", "t", "repo", "org/repo"));

    assertEquals(params, sut.retrieveCreateParams("github-actions", params));
    assertEquals(params, sut.retrieveUpdatedParams("github-actions", params));
    verifyNoInteractions(pluginBox);
  }

  @Test
  void checkConnection_GithubActionsWithTokenAndRepo_ShouldPassWithoutAskingForAPlugin() {
    assertTrue(sut.checkConnection(integration("github-actions", Map.of("token", "t", "repo", "org/repo"))));
    verifyNoInteractions(pluginBox);
  }

  @Test
  void checkConnection_GitlabCiWithProjectIdAndTriggerToken_ShouldPass() {
    assertTrue(sut.checkConnection(integration("gitlab-ci", Map.of("projectId", "grp/proj", "triggerToken", "t"))));
  }

  @Test
  void checkConnection_GithubActionsWithoutRepo_ShouldBeRejectedNamingTheParameter() {
    var exception = assertThrows(ReportPortalException.class,
        () -> sut.checkConnection(integration("github-actions", Map.of("token", "t"))));

    assertEquals(ErrorType.BAD_REQUEST_ERROR, exception.getErrorType());
    assertTrue(exception.getMessage().contains("repo"), exception.getMessage());
  }

  @Test
  void checkConnection_BlankTokenOrNoParams_ShouldBeRejected() {
    assertThrows(ReportPortalException.class,
        () -> sut.checkConnection(integration("gitlab-ci", Map.of("projectId", "p", "triggerToken", " "))));
    assertThrows(ReportPortalException.class, () -> sut.checkConnection(integration("github-actions", null)));
  }

  private static Integration integration(String typeName, Map<String, Object> params) {
    var type = new IntegrationType();
    type.setName(typeName);
    var integration = new Integration();
    integration.setType(type);
    if (params != null) {
      integration.setParams(new IntegrationParams(new HashMap<>(params)));
    }
    return integration;
  }
}
