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

package com.epam.reportportal.auth.integration.parameter;

import static com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers.formattedSupplier;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.base.infrastructure.model.integration.auth.UpdateAuthRQ;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationParams;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Getter
public enum SamlParameter {

  IDP_NAME("identityProviderName", true),
  IDP_METADATA_URL("identityProviderMetadataUrl", true),
  EMAIL_ATTRIBUTE("emailAttribute", true),
  IDP_NAME_ID("identityProviderNameId", false),
  IDP_ALIAS("identityProviderAlias", false),
  IDP_URL("identityProviderUrl", false),
  FULL_NAME_ATTRIBUTE("fullNameAttribute", false),
  FIRST_NAME_ATTRIBUTE("firstNameAttribute", false),
  LAST_NAME_ATTRIBUTE("lastNameAttribute", false),
  ROLES_ATTRIBUTE("rolesAttribute", false);

  private String parameterName;

  private boolean required;

  SamlParameter(String parameterName, boolean required) {
    this.parameterName = parameterName;
    this.required = required;
  }

  public Optional<String> getParameter(Integration integration) {
    return ofNullable((String) integration.getParams().getParams().get(parameterName));
  }

  public Optional<String> getParameter(Map<String, Object> parameterMap) {
    return ofNullable(parameterMap.get(parameterName)).map(it -> (String) it)
        .filter(StringUtils::isNotBlank);
  }

  public Optional<String> getParameter(UpdateAuthRQ request) {
    return ofNullable(request.getIntegrationParams()).flatMap(this::getParameter);
  }

  public void setParameter(Integration integration, String value) {
    if (Objects.isNull(integration.getParams())) {
      integration.setParams(new IntegrationParams(new HashMap<>()));
    }
    if (Objects.isNull(integration.getParams().getParams())) {
      integration.getParams().setParams(new HashMap<>());
    }
    integration.getParams().getParams().put(parameterName, value);
  }

  public void setParameter(UpdateAuthRQ request, Integration integration) {
    getParameter(request).ifPresent(it -> setParameter(integration, it));
  }

  public void removeParameter(Integration integration) {
    ofNullable(integration.getParams()).map(IntegrationParams::getParams)
        .ifPresent(params -> params.remove(parameterName));
  }

  public String getRequiredParameter(Integration integration) {
    Optional<String> parameter = getParameter(integration);
    if (required) {
      if (parameter.isPresent()) {
        return parameter.get();
      } else {
        throw new ReportPortalException(ErrorType.INCORRECT_REQUEST,
            formattedSupplier("'{}' should be present.", parameterName));
      }
    } else {
      throw new ReportPortalException(ErrorType.INCORRECT_REQUEST,
          formattedSupplier("'{}' is not required."));
    }
  }

  public String getRequiredParameter(UpdateAuthRQ request) {
    Optional<String> parameter = getParameter(request);
    if (required) {
      if (parameter.isPresent()) {
        return parameter.get();
      } else {
        throw new ReportPortalException(ErrorType.INCORRECT_REQUEST,
            formattedSupplier("'{}' should be present.", parameterName));
      }
    } else {
      throw new ReportPortalException(ErrorType.INCORRECT_REQUEST,
          formattedSupplier("'{}' is not required."));
    }
  }

  public boolean exist(Integration integration) {
    return getParameter(integration).isPresent();
  }

}
