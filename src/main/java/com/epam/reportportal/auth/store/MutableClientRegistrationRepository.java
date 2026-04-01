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

import static com.epam.reportportal.auth.integration.converter.OAuthRegistrationConverters.INTEGRATION_TO_OAUTH_REGISTRATION;
import static com.epam.reportportal.auth.integration.converter.OAuthRegistrationConverters.TO_RESOURCE;

import com.epam.reportportal.auth.model.OAuthRegistrationResource;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.IntegrationAuthFlowEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.IntegrationGroupEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;

@Slf4j
@Component("mutableClientRegistrationRepository")
public class MutableClientRegistrationRepository implements ClientRegistrationRepository {

  public static final String ID_HAS_NOT_BEEN_FOUND = "Client registration with id = {} has not been found.";
  private final IntegrationRepository integrationRepository;

  @Autowired
  public MutableClientRegistrationRepository(IntegrationRepository integrationRepository) {
    this.integrationRepository = integrationRepository;
  }

  @Override
  public ClientRegistration findByRegistrationId(String registrationId) {
    return integrationRepository.findGlobalByNameAndAuthFlowAndGroup(
            registrationId,
            IntegrationGroupEnum.AUTH,
            IntegrationAuthFlowEnum.OAUTH)
        .map(INTEGRATION_TO_OAUTH_REGISTRATION)
        .orElseThrow(() -> new ReportPortalException(ErrorType.AUTH_INTEGRATION_NOT_FOUND,
            Suppliers.formattedSupplier(ID_HAS_NOT_BEEN_FOUND, registrationId).get()
        ));
  }

  public OAuthRegistrationResource findByRegistrationId2(String registrationId) {
    return integrationRepository.findGlobalByNameAndAuthFlowAndGroup(
            registrationId,
            IntegrationGroupEnum.AUTH,
            IntegrationAuthFlowEnum.OAUTH)
        .map(TO_RESOURCE)
        .orElseGet(() -> {
          log.warn("Unable to find client registration with id = {}", registrationId);
          return null;
        });
  }

  public List<Integration> findAll() {
    return integrationRepository.findAllByAuthFlowAndGroup(IntegrationGroupEnum.AUTH,
        IntegrationAuthFlowEnum.OAUTH);
  }

/*  public Optional<OAuthRegistration> findOAuthRegistrationById(String registrationId) {
    return this.oAuthRegistrationRepository.findById(registrationId);
  }

  public boolean existsById(String oauthProviderId) {
    return this.oAuthRegistrationRepository.existsById(oauthProviderId);
  }

  public OAuthRegistration save(OAuthRegistration registration) {
    return this.oAuthRegistrationRepository.save(registration);
  }

  public void deleteById(String oauthProviderId) {
    oAuthRegistrationRepository.deleteById(oauthProviderId);
  }

  public Collection<OAuthRegistration> findAll() {
    return oAuthRegistrationRepository.findAll();
  }*/

}
