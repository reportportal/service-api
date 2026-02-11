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
import com.epam.reportportal.base.infrastructure.model.integration.auth.UpdateAuthRQ;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public class UpdateAuthRequestValidator implements AuthRequestValidator<UpdateAuthRQ> {

  private final ParamNamesProvider paramNamesProvider;

  public UpdateAuthRequestValidator(ParamNamesProvider paramNamesProvider) {
    this.paramNamesProvider = paramNamesProvider;
  }

  @Override
  public void validate(UpdateAuthRQ updateRequest) {
    final List<String> paramNames = paramNamesProvider.provide();
    paramNames.stream()
        .map(it -> retrieveParam(updateRequest, it))
        .forEach(it -> expect(it, Optional::isPresent).verify(ErrorType.BAD_REQUEST_ERROR,
            formattedSupplier("parameter '{}' is required.", it)
        ));
  }

  private Optional<String> retrieveParam(UpdateAuthRQ updateRequest, String name) {
    return ofNullable(updateRequest.getIntegrationParams().get(name)).map(String::valueOf)
        .filter(StringUtils::isNotBlank);
  }
}
