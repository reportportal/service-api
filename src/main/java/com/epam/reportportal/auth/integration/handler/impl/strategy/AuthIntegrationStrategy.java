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

package com.epam.reportportal.auth.integration.handler.impl.strategy;

import com.epam.reportportal.auth.integration.builder.AuthIntegrationBuilder;
import com.epam.reportportal.auth.integration.validator.duplicate.IntegrationDuplicateValidator;
import com.epam.reportportal.auth.integration.validator.request.AuthRequestValidator;
import com.epam.reportportal.base.infrastructure.model.integration.auth.AbstractAuthResource;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationType;
import com.epam.reportportal.base.model.integration.IntegrationRQ;
import java.time.Instant;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public abstract class AuthIntegrationStrategy {

  private final IntegrationRepository integrationRepository;
  private final AuthRequestValidator<IntegrationRQ> updateAuthRequestValidator;
  private final IntegrationDuplicateValidator integrationDuplicateValidator;

  public AuthIntegrationStrategy(IntegrationRepository integrationRepository,
      AuthRequestValidator<IntegrationRQ> updateAuthRequestValidator,
      IntegrationDuplicateValidator integrationDuplicateValidator) {
    this.integrationRepository = integrationRepository;
    this.updateAuthRequestValidator = updateAuthRequestValidator;
    this.integrationDuplicateValidator = integrationDuplicateValidator;
  }

  protected abstract void populateIntegrationDetails(Integration integration, IntegrationRQ updateRequest);

  public abstract AbstractAuthResource toResource(Integration integration);

  public Integration createIntegration(IntegrationType integrationType, IntegrationRQ request, String username) {
    updateAuthRequestValidator.validate(request);

    final Integration integration = new AuthIntegrationBuilder()
        .addCreator(username)
        .addIntegrationType(integrationType)
        .addCreationDate(Instant.now())
        .build();
    populateIntegrationDetails(integration, request);

    return save(integration);
  }

  public Integration updateIntegration(Integration integration, IntegrationRQ request) {
    populateIntegrationDetails(integration, request);
    return save(integration);
  }

  protected Integration save(Integration integration) {
    integrationDuplicateValidator.validate(integration);
    return integrationRepository.save(integration);
  }

}
