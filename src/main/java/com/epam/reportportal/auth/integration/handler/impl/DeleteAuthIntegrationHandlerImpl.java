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

import static com.epam.reportportal.base.infrastructure.persistence.commons.Predicates.equalTo;

import com.epam.reportportal.auth.event.SamlProvidersReloadEvent;
import com.epam.reportportal.auth.integration.AuthIntegrationType;
import com.epam.reportportal.auth.integration.handler.DeleteAuthIntegrationHandler;
import com.epam.reportportal.auth.store.MutableClientRegistrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.IntegrationGroupEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.oauth.OAuthRegistration;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.reporting.OperationCompletionRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class DeleteAuthIntegrationHandlerImpl implements DeleteAuthIntegrationHandler {

  private final IntegrationRepository integrationRepository;

  private final MutableClientRegistrationRepository clientRegistrationRepository;

  private final ApplicationEventPublisher eventPublisher;

  @Autowired
  public DeleteAuthIntegrationHandlerImpl(IntegrationRepository integrationRepository,
      MutableClientRegistrationRepository clientRegistrationRepository,
      ApplicationEventPublisher eventPublisher) {
    this.integrationRepository = integrationRepository;
    this.clientRegistrationRepository = clientRegistrationRepository;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public OperationCompletionRS deleteAuthIntegrationById(Long integrationId) {
    Integration integration = integrationRepository.findById(integrationId)
        .orElseThrow(
            () -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, integrationId));

    BusinessRule.expect(integration.getType().getIntegrationGroup(), equalTo(IntegrationGroupEnum.AUTH))
        .verify(ErrorType.BAD_REQUEST_ERROR, "Integration should have an 'AUTH' integration group type.");

    integrationRepository.deleteById(integrationId);

    if (AuthIntegrationType.SAML.getName().equals(integration.getType().getName())) {
      eventPublisher.publishEvent(new SamlProvidersReloadEvent(integration.getType()));
    }

    return new OperationCompletionRS(
        "Auth integration with id= " + integrationId + " has been successfully removed.");

  }

  @Override
  public OperationCompletionRS deleteOauthSettingsById(String oauthProviderId) {

    OAuthRegistration oAuthRegistration = clientRegistrationRepository.findOAuthRegistrationById(
            oauthProviderId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.AUTH_INTEGRATION_NOT_FOUND,
            Suppliers.formattedSupplier("Oauth settings with id = {} have not been found.",
                oauthProviderId).get()
        ));

    clientRegistrationRepository.deleteById(oAuthRegistration.getId());

    return new OperationCompletionRS(
        Suppliers.formattedSupplier("Oauth settings with id = '{}' have been successfully removed.",
            oauthProviderId
        ).get());
  }
}
