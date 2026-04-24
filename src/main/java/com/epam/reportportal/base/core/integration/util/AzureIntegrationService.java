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
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Integration service for Azure DevOps.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class AzureIntegrationService extends BasicIntegrationServiceImpl {

  private BtsIntegrationService btsIntegrationService;

  @Autowired
  public AzureIntegrationService(IntegrationRepository integrationRepository, PluginBox pluginBox,
      BtsIntegrationService btsIntegrationService) {
    super(integrationRepository, pluginBox);
    this.btsIntegrationService = btsIntegrationService;
  }

  @Override
  public Map<String, Object> retrieveCreateParams(String integrationType,
      Map<String, Object> integrationParams) {
    return btsIntegrationService.retrieveCreateParams(integrationType, integrationParams);
  }

  @Override
  public Map<String, Object> retrieveUpdatedParams(String integrationType,
      Map<String, Object> integrationParams) {
    return btsIntegrationService.retrieveUpdatedParams(integrationType, integrationParams);
  }
}
