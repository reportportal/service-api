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

import static com.epam.reportportal.auth.integration.converter.OAuthRegistrationConverters.TO_RESOURCE;

import com.epam.reportportal.auth.integration.converter.OAuthRegistrationConverters;
import com.epam.reportportal.auth.model.OAuthRegistrationResource;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.IntegrationAuthFlowEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.IntegrationGroupEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;

@Slf4j
@Component("mutableClientRegistrationRepository")
public class MutableClientRegistrationRepository implements ClientRegistrationRepository,
    OAuthRegistrationResourceRepository {

  public static final String ID_HAS_NOT_BEEN_FOUND = "Client registration with id = {} has not been found.";

  private static final Map<String, ClientRegistrationRepository> PLUGIN_DELEGATES = new ConcurrentHashMap<>();

  public static void registerDelegate(String registrationId, ClientRegistrationRepository repository) {
    PLUGIN_DELEGATES.put(registrationId, repository);
  }

  public static void unregisterDelegate(String registrationId) {
    PLUGIN_DELEGATES.remove(registrationId);
  }

  private final IntegrationRepository integrationRepository;

  private final BasicTextEncryptor basicTextEncryptor;

  @Autowired
  public MutableClientRegistrationRepository(IntegrationRepository integrationRepository,
      BasicTextEncryptor basicTextEncryptor) {
    this.integrationRepository = integrationRepository;
    this.basicTextEncryptor = basicTextEncryptor;
  }

  @Override
  public ClientRegistration findByRegistrationId(String registrationId) {
    ClientRegistrationRepository delegate = PLUGIN_DELEGATES.get(registrationId);
    if (delegate != null) {
      return delegate.findByRegistrationId(registrationId);
    }
    return integrationRepository.findGlobalByNameAndAuthFlowAndGroup(
            registrationId,
            IntegrationGroupEnum.AUTH,
            IntegrationAuthFlowEnum.OAUTH)
        .map(integration -> OAuthRegistrationConverters.toClientRegistration(integration, basicTextEncryptor))
        .orElseThrow(() -> new ReportPortalException(ErrorType.AUTH_INTEGRATION_NOT_FOUND,
            Suppliers.formattedSupplier(ID_HAS_NOT_BEEN_FOUND, registrationId).get()
        ));
  }

  public OAuthRegistrationResource findOAuthRegistrationResourceById(String registrationId) {
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
    return integrationRepository.findAllByAuthFlowAndGroup(IntegrationGroupEnum.AUTH, IntegrationAuthFlowEnum.OAUTH);
  }

}
