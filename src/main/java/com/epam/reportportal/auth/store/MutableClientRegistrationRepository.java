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

package com.epam.reportportal.auth.store;

import static com.epam.reportportal.auth.integration.converter.OAuthRegistrationConverters.FROM_INTEGRATION;
import static com.epam.reportportal.auth.integration.converter.OAuthRegistrationConverters.INTEGRATION_TO_SPRING;

import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.IntegrationAuthFlowEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.IntegrationGroupEnum;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;

@Component("mutableClientRegistrationRepository")
public class MutableClientRegistrationRepository implements ClientRegistrationRepository {

  private final IntegrationRepository integrationRepository;

  @Autowired
  public MutableClientRegistrationRepository(IntegrationRepository integrationRepository) {
    this.integrationRepository = integrationRepository;
  }

  @Override
  public ClientRegistration findByRegistrationId(String registrationId) {
    return integrationRepository.findGlobalByNameAndGroup(registrationId, IntegrationGroupEnum.AUTH)
        .map(INTEGRATION_TO_SPRING)
        .orElseThrow(() -> new ReportPortalException(
            ErrorType.AUTH_INTEGRATION_NOT_FOUND,
            Suppliers.formattedSupplier("Client registration with id = {} has not been found.",
                registrationId).get()
        ));
  }

  public Optional<Map<String, Object>> findOAuthRegistrationById(String registrationId) {
    return integrationRepository.findGlobalByNameAndGroup(registrationId, IntegrationGroupEnum.AUTH)
        .map(FROM_INTEGRATION);
  }

  public boolean existsById(String oauthProviderId) {
    return integrationRepository.findGlobalByNameAndGroup(oauthProviderId,
        IntegrationGroupEnum.AUTH).isPresent();
  }

  public void deleteById(String oauthProviderId) {
    integrationRepository.findGlobalByNameAndGroup(oauthProviderId, IntegrationGroupEnum.AUTH)
        .ifPresent(i -> integrationRepository.deleteById(i.getId()));
  }

  public Collection<Map<String, Object>> findAll() {
    return integrationRepository.findAllGlobalByGroupAndAuthFlow(IntegrationGroupEnum.AUTH, IntegrationAuthFlowEnum.OAUTH)
        .stream()
        .map(FROM_INTEGRATION)
        .collect(Collectors.toList());
  }
}
