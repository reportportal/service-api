/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.auth.integration.handler.impl.strategy;

import static com.epam.reportportal.auth.integration.converter.LdapConverter.UPDATE_FROM_REQUEST;

import com.epam.reportportal.auth.integration.AuthIntegrationType;
import com.epam.reportportal.auth.integration.parameter.LdapParameter;
import com.epam.reportportal.auth.integration.validator.duplicate.IntegrationDuplicateValidator;
import com.epam.reportportal.auth.integration.validator.request.AuthRequestValidator;
import com.epam.reportportal.base.infrastructure.model.integration.auth.UpdateAuthRQ;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
public class LdapIntegrationStrategy extends AuthIntegrationStrategy {

  private final BasicTextEncryptor encryptor;

  @Autowired
  public LdapIntegrationStrategy(IntegrationRepository integrationRepository,
      @Qualifier("ldapUpdateAuthRequestValidator")
      AuthRequestValidator<UpdateAuthRQ> updateAuthRequestValidator,
      IntegrationDuplicateValidator integrationDuplicateValidator,
      BasicTextEncryptor encryptor) {
    super(integrationRepository, updateAuthRequestValidator, integrationDuplicateValidator);
    this.encryptor = encryptor;
  }

  @Override
  protected void fill(Integration integration, UpdateAuthRQ updateRequest) {
    integration.setName(AuthIntegrationType.LDAP.getName());
    LdapParameter.MANAGER_PASSWORD.getParameter(updateRequest)
        .ifPresent(it -> updateRequest.getIntegrationParams()
            .put(LdapParameter.MANAGER_PASSWORD.getParameterName(), encryptor.encrypt(it)));
    UPDATE_FROM_REQUEST.accept(updateRequest, integration);
  }

}
