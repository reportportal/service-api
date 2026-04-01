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

package com.epam.reportportal.auth.integration.handler.impl;

import com.epam.reportportal.auth.integration.handler.CreateAuthIntegrationHandler;
import com.epam.reportportal.auth.integration.handler.impl.strategy.AuthIntegrationStrategy;
import com.epam.reportportal.auth.integration.provider.AuthIntegrationStrategyProvider;
import com.epam.reportportal.base.infrastructure.model.integration.auth.AbstractAuthResource;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationTypeRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.integration.IntegrationRQ;
import com.epam.reportportal.base.util.SecurityContextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class CreateAuthIntegrationHandlerImpl implements CreateAuthIntegrationHandler {

  private final AuthIntegrationStrategyProvider strategyProvider;
  private final IntegrationTypeRepository integrationTypeRepository;

  @Autowired
  public CreateAuthIntegrationHandlerImpl(AuthIntegrationStrategyProvider strategyProvider,
      IntegrationTypeRepository integrationTypeRepository) {
    this.strategyProvider = strategyProvider;
    this.integrationTypeRepository = integrationTypeRepository;
  }

  @Override
  public Integration createAuthIntegration(String type, IntegrationRQ request) {
    var user = SecurityContextUtils.getPrincipal();

    final IntegrationType integrationType = getIntegrationType(type);
    final AuthIntegrationStrategy authIntegrationStrategy = getAuthStrategy(type);
    return authIntegrationStrategy.createIntegration(integrationType, request, user.getUsername());
  }

  @Override
  public Integration updateAuthIntegration(Integration integration, IntegrationRQ updateRequest) {
    final AuthIntegrationStrategy authIntegrationStrategy = getAuthStrategy(integration.getType().getName());
    return authIntegrationStrategy.updateIntegration(integration, updateRequest);
  }

  private IntegrationType getIntegrationType(String type) {
    return integrationTypeRepository.findByName(type)
        .orElseThrow(() -> new ReportPortalException(ErrorType.AUTH_INTEGRATION_NOT_FOUND, type));
  }

  private AuthIntegrationStrategy getAuthStrategy(String type) {
    return strategyProvider.provide(type)
        .orElseThrow(() -> new ReportPortalException(ErrorType.AUTH_INTEGRATION_NOT_FOUND, type));
  }

}
