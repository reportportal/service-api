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

import com.epam.reportportal.base.core.plugin.PluginBox;
import com.epam.reportportal.base.core.tms.enums.TmsSyncProvider;
import com.epam.reportportal.base.core.tms.sync.TmsSyncConnector;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Integration service for the built-in "qa-space" TMS import integration type. Unlike BTS/notification
 * integrations, this type is not backed by an installed plugin, so it must not go through {@link PluginBox}
 * for parameter retrieval or connection validation.
 */
@Service
public class QaSpaceIntegrationService extends BasicIntegrationServiceImpl {

  private final List<TmsSyncConnector<Integration>> connectors;

  public QaSpaceIntegrationService(IntegrationRepository integrationRepository, PluginBox pluginBox,
      IntegrationParamsEncryptor paramsEncryptor, List<TmsSyncConnector<Integration>> connectors) {
    super(integrationRepository, pluginBox, paramsEncryptor);
    this.connectors = connectors;
  }

  @Override
  public Map<String, Object> retrieveCreateParams(String integrationType, Map<String, Object> integrationParams) {
    return integrationParams;
  }

  @Override
  public Map<String, Object> retrieveUpdatedParams(String integrationType, Map<String, Object> integrationParams) {
    return integrationParams;
  }

  @Override
  public boolean checkConnection(Integration integration) {
    connectors
        .stream()
        .filter(connector -> connector.getSupportedProvider() == TmsSyncProvider.QA_SPACE)
        .findFirst()
        .orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
            "No connector registered for 'qa-space' integration"))
        .validateConfig(integration);
    return true;
  }
}
