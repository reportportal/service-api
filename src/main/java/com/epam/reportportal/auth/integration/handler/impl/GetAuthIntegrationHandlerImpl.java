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

import static com.epam.reportportal.auth.integration.converter.OAuthRegistrationConverters.RESOURCE_KEY_MAPPER;
import static com.epam.reportportal.auth.integration.converter.OAuthRegistrationConverters.TO_RESOURCE;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.auth.integration.AuthIntegrationType;
import com.epam.reportportal.auth.integration.converter.OAuthRegistrationConverters;
import com.epam.reportportal.auth.integration.handler.GetAuthIntegrationHandler;
import com.epam.reportportal.auth.integration.handler.GetAuthIntegrationStrategy;
import com.epam.reportportal.auth.model.ExtendedOAuthRegistrationResource;
import com.epam.reportportal.auth.model.settings.OAuthRegistrationResource;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.auth.store.MutableClientRegistrationRepository;
import com.epam.reportportal.base.infrastructure.model.integration.auth.AbstractAuthResource;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class GetAuthIntegrationHandlerImpl implements GetAuthIntegrationHandler {

  private final Map<AuthIntegrationType, GetAuthIntegrationStrategy> authIntegrationStrategyMapping;

  private final MutableClientRegistrationRepository clientRegistrationRepository;

  @Autowired
  public GetAuthIntegrationHandlerImpl(
      @Qualifier(value = "getAuthIntegrationStrategyMapping")
      Map<AuthIntegrationType, GetAuthIntegrationStrategy> authIntegrationStrategyMapping,
      MutableClientRegistrationRepository clientRegistrationRepository) {
    this.authIntegrationStrategyMapping = authIntegrationStrategyMapping;
    this.clientRegistrationRepository = clientRegistrationRepository;
  }

  @Override
  public AbstractAuthResource getIntegrationByType(AuthIntegrationType integrationType) {
    return ofNullable(authIntegrationStrategyMapping.get(integrationType)).orElseThrow(
            () -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
                "Unable to find suitable auth integration strategy for type= " + integrationType
            ))
        .getIntegration();
  }

  @Override
  public Map<String, OAuthRegistrationResource> getAllOauthIntegrations() {
    return clientRegistrationRepository.findAll().stream().map(TO_RESOURCE)
        .collect(RESOURCE_KEY_MAPPER);
  }

  @Override
  public ResponseEntity<OAuthRegistrationResource> getOauthIntegrationById(String oauthProviderId) {
    return clientRegistrationRepository.findOAuthRegistrationById(oauthProviderId)
        .map(OAuthRegistrationConverters.TO_RESOURCE)
        .map(ResponseEntity::ok)
        .orElseGet(() -> {
          ExtendedOAuthRegistrationResource body = new ExtendedOAuthRegistrationResource();
          body.setErrorCode(ErrorType.AUTH_INTEGRATION_NOT_FOUND.getCode());
          body.setMessage(
              Suppliers.formattedSupplier("Oauth settings with id = {} have not been found.",
                  oauthProviderId).get()
          );
          return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        });
  }
}
