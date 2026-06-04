/*
 * Copyright 2021 EPAM Systems
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

package com.epam.reportportal.auth.integration.validator.request;

import static com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers.formattedSupplier;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.auth.integration.validator.request.param.provider.ParamNamesProvider;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.model.integration.IntegrationRQ;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

/**
 * Validates an {@link IntegrationRQ} update request by ensuring all required integration
 * parameters are present and non-blank. The set of required parameter names is supplied by
 * a {@link ParamNamesProvider}.
 */
public class UpdateAuthRequestValidator implements AuthRequestValidator<IntegrationRQ> {

  private final ParamNamesProvider paramNamesProvider;

  /**
   * Constructs a new validator with the given provider of required parameter names.
   *
   * @param paramNamesProvider supplies the list of parameter names that must be present in the
   *                           integration request
   */
  public UpdateAuthRequestValidator(ParamNamesProvider paramNamesProvider) {
    this.paramNamesProvider = paramNamesProvider;
  }

  /**
   * Validates that every required parameter name returned by {@link ParamNamesProvider#provide()}
   * is present and non-blank in the integration params of the given request.
   *
   * @param updateRequest the integration update request to validate
   * @throws com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException with
   *     {@link ErrorType#BAD_REQUEST_ERROR} if any required parameter is missing or blank
   */
  @Override
  public void validate(IntegrationRQ updateRequest) {
    final List<String> paramNames = paramNamesProvider.provide();
    paramNames.stream()
        .map(it -> retrieveParam(updateRequest, it))
        .forEach(it -> expect(it, Optional::isPresent)
            .verify(ErrorType.BAD_REQUEST_ERROR, formattedSupplier("parameter '{}' is required.", it)
            ));
  }

  /**
   * Retrieves the value of the named parameter from the integration request params.
   *
   * @param updateRequest the integration update request
   * @param name          the parameter name to look up
   * @return an {@link Optional} containing the non-blank string value, or empty if the parameter
   *     is absent or blank
   */
  private Optional<String> retrieveParam(IntegrationRQ updateRequest, String name) {
    return ofNullable(updateRequest.getIntegrationParams().get(name))
        .map(String::valueOf)
        .filter(StringUtils::isNotBlank);
  }
}
