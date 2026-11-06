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
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Integration service for the built-in CI trigger types "github-actions" and "gitlab-ci" that start the agent
 * pipeline. Like "qa-space", these types are not backed by an installed plugin, so they must not go through
 * {@link PluginBox}: the default service would answer "Plugin for ... isn't installed". Nothing is called on
 * creation; instead the parameters the connector cannot work without are required up front, so a missing one
 * is reported when the integration is saved and not when somebody pushes a test case to the agent.
 * <p>
 * Example: {@code github-actions} without {@code repo} is rejected with
 * "Integration parameter 'repo' is required for github-actions".
 */
@Service
public class CiTriggerIntegrationService extends BasicIntegrationServiceImpl {

  private static final Map<String, List<String>> REQUIRED_PARAMS = Map.of(
      "github-actions", List.of("token", "repo"),
      "gitlab-ci", List.of("projectId", "triggerToken"));

  public CiTriggerIntegrationService(IntegrationRepository integrationRepository, PluginBox pluginBox,
      IntegrationParamsEncryptor paramsEncryptor) {
    super(integrationRepository, pluginBox, paramsEncryptor);
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
    var typeName = integration.getType().getName();
    var params = integration.getParams() == null ? null : integration.getParams().getParams();
    for (var name : REQUIRED_PARAMS.getOrDefault(typeName, List.of())) {
      var value = params == null ? null : params.get(name);
      if (!(value instanceof String text) || text.isBlank()) {
        throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
            "Integration parameter '" + name + "' is required for " + typeName);
      }
    }
    return true;
  }
}
