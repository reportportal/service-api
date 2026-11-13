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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.epam.reportportal.base.core.plugin.PluginBox;
import com.epam.reportportal.base.core.tms.enums.TmsSyncProvider;
import com.epam.reportportal.base.core.tms.sync.TmsSyncConnector;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QaSpaceIntegrationServiceTest {

  private final IntegrationRepository integrationRepository = mock(IntegrationRepository.class);
  private final PluginBox pluginBox = mock(PluginBox.class);
  private final IntegrationParamsEncryptor paramsEncryptor = mock(IntegrationParamsEncryptor.class);
  @SuppressWarnings("unchecked")
  private final TmsSyncConnector<Integration> qaSpaceConnector = mock(TmsSyncConnector.class);

  private QaSpaceIntegrationService qaSpaceIntegrationService;

  @BeforeEach
  void setUp() {
    qaSpaceIntegrationService = new QaSpaceIntegrationService(integrationRepository, pluginBox,
        paramsEncryptor, List.of(qaSpaceConnector));
  }

  @Test
  void retrieveCreateParamsDoesNotUsePluginBox() {
    Map<String, Object> params = Maps.newHashMap();
    params.put("url", "https://jira.example.com");

    Map<String, Object> result = qaSpaceIntegrationService.retrieveCreateParams("qa-space", params);

    assertEquals(params, result);
    verifyNoInteractions(pluginBox);
  }

  @Test
  void checkConnectionDelegatesToQaSpaceConnector() {
    Integration integration = new Integration();
    org.mockito.Mockito.when(qaSpaceConnector.getSupportedProvider()).thenReturn(TmsSyncProvider.QA_SPACE);

    boolean result = qaSpaceIntegrationService.checkConnection(integration);

    assertTrue(result);
    verify(qaSpaceConnector).validateConfig(integration);
    verifyNoInteractions(pluginBox);
  }

  @Test
  void checkConnectionFailsWhenNoConnectorRegistered() {
    QaSpaceIntegrationService serviceWithoutConnector = new QaSpaceIntegrationService(
        integrationRepository, pluginBox, paramsEncryptor, List.of());
    Integration integration = new Integration();

    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> serviceWithoutConnector.checkConnection(integration));

    assertEquals(ErrorType.BAD_REQUEST_ERROR, exception.getErrorType());
  }
}
