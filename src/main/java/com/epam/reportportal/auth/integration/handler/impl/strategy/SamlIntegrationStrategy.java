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

import static com.epam.reportportal.auth.integration.converter.SamlConverter.UPDATE_FROM_REQUEST;
import static com.epam.reportportal.auth.integration.parameter.SamlParameter.IDP_ALIAS;
import static com.epam.reportportal.auth.integration.parameter.SamlParameter.IDP_NAME_ID;
import static com.epam.reportportal.auth.integration.parameter.SamlParameter.IDP_URL;

import com.epam.reportportal.auth.event.SamlProvidersReloadEvent;
import com.epam.reportportal.auth.integration.parameter.SamlParameter;
import com.epam.reportportal.auth.integration.validator.duplicate.IntegrationDuplicateValidator;
import com.epam.reportportal.auth.integration.validator.request.AuthRequestValidator;
import com.epam.reportportal.base.infrastructure.model.integration.auth.UpdateAuthRQ;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml.saml2.core.NameID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
public class SamlIntegrationStrategy extends AuthIntegrationStrategy {

  private final ApplicationEventPublisher eventPublisher;

  @Autowired
  public SamlIntegrationStrategy(IntegrationRepository integrationRepository,
      @Qualifier("samlUpdateAuthRequestValidator")
      AuthRequestValidator<UpdateAuthRQ> updateAuthRequestValidator,
      IntegrationDuplicateValidator integrationDuplicateValidator,
      ApplicationEventPublisher eventPublisher) {
    super(integrationRepository, updateAuthRequestValidator, integrationDuplicateValidator);
    this.eventPublisher = eventPublisher;
  }

  @Override
  protected void fill(Integration integration, UpdateAuthRQ updateRequest) {
    UPDATE_FROM_REQUEST.accept(updateRequest, integration);
  }

  @Override
  protected Integration save(Integration integration) {
    populateProviderDetails(integration);
    final Integration result = super.save(integration);
    eventPublisher.publishEvent(new SamlProvidersReloadEvent(result.getType()));
    return result;
  }

  private void populateProviderDetails(Integration samlIntegration) {
    Map<String, Object> params = samlIntegration.getParams().getParams();
    String metadataUrl = SamlParameter.IDP_METADATA_URL.getRequiredParameter(samlIntegration);

    RelyingPartyRegistration relyingPartyRegistration = RelyingPartyRegistrations
        .fromMetadataLocation(metadataUrl)
        .build();

    params.put(IDP_URL.getParameterName(), relyingPartyRegistration.getRegistrationId());
    params.put(IDP_ALIAS.getParameterName(), relyingPartyRegistration.getAssertingPartyMetadata().getEntityId());
    params.put(IDP_NAME_ID.getParameterName(),
        StringUtils.isNotEmpty(relyingPartyRegistration.getNameIdFormat()) ? relyingPartyRegistration.getNameIdFormat()
            : NameID.UNSPECIFIED);
  }
}
