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

package com.epam.reportportal.auth.integration.converter;

import static com.epam.reportportal.auth.integration.parameter.SamlParameter.EMAIL_ATTRIBUTE;
import static com.epam.reportportal.auth.integration.parameter.SamlParameter.FIRST_NAME_ATTRIBUTE;
import static com.epam.reportportal.auth.integration.parameter.SamlParameter.FULL_NAME_ATTRIBUTE;
import static com.epam.reportportal.auth.integration.parameter.SamlParameter.IDP_ALIAS;
import static com.epam.reportportal.auth.integration.parameter.SamlParameter.IDP_METADATA_URL;
import static com.epam.reportportal.auth.integration.parameter.SamlParameter.IDP_NAME;
import static com.epam.reportportal.auth.integration.parameter.SamlParameter.IDP_NAME_ID;
import static com.epam.reportportal.auth.integration.parameter.SamlParameter.IDP_URL;
import static com.epam.reportportal.auth.integration.parameter.SamlParameter.LAST_NAME_ATTRIBUTE;
import static com.epam.reportportal.auth.integration.parameter.SamlParameter.ROLES_ATTRIBUTE;

import com.epam.reportportal.auth.integration.parameter.ParameterUtils;
import com.epam.reportportal.auth.model.SamlProvidersResource;
import com.epam.reportportal.auth.model.SamlResource;
import com.epam.reportportal.base.infrastructure.model.integration.auth.UpdateAuthRQ;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.util.CollectionUtils;

/**
 * Used for mapping between SAML resource model and entity.
 *
 * @author Yevgeniy Svalukhin
 */
public class SamlConverter {

  public static final BiConsumer<UpdateAuthRQ, Integration> UPDATE_FROM_REQUEST =
      (request, integration) -> {
        integration.setEnabled(request.getEnabled());
        integration.setName(IDP_NAME.getRequiredParameter(request));
        ParameterUtils.setSamlParameters(request, integration);
      };

  public static final Function<Integration, SamlResource> TO_RESOURCE = integration -> {
    SamlResource resource = new SamlResource();
    resource.setId(integration.getId());
    resource.setIdentityProviderName(integration.getName());
    resource.setEnabled(integration.isEnabled());

    EMAIL_ATTRIBUTE.getParameter(integration).ifPresent(resource::setEmailAttribute);
    FIRST_NAME_ATTRIBUTE.getParameter(integration).ifPresent(resource::setFirstNameAttribute);
    LAST_NAME_ATTRIBUTE.getParameter(integration).ifPresent(resource::setLastNameAttribute);
    FULL_NAME_ATTRIBUTE.getParameter(integration).ifPresent(resource::setFullNameAttribute);
    IDP_ALIAS.getParameter(integration).ifPresent(resource::setIdentityProviderAlias);
    IDP_METADATA_URL.getParameter(integration).ifPresent(resource::setIdentityProviderMetadataUrl);
    IDP_URL.getParameter(integration).ifPresent(resource::setIdentityProviderUrl);
    IDP_NAME_ID.getParameter(integration).ifPresent(resource::setIdentityProviderNameId);
    ROLES_ATTRIBUTE.getParameter(integration).ifPresent(resource::setRolesAttribute);
    return resource;
  };

  public static final Function<List<Integration>, SamlProvidersResource> TO_PROVIDERS_RESOURCE =
      integrations -> {
        if (CollectionUtils.isEmpty(integrations)) {
          SamlProvidersResource emptyResource = new SamlProvidersResource();
          emptyResource.setProviders(Collections.emptyList());
          return emptyResource;
        }
        SamlProvidersResource resource = new SamlProvidersResource();
        resource.setProviders(integrations.stream().map(TO_RESOURCE).collect(Collectors.toList()));
        return resource;
      };

}
