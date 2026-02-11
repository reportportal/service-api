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

import static com.epam.reportportal.auth.integration.parameter.SamlParameter.FIRST_NAME_ATTRIBUTE;
import static com.epam.reportportal.auth.integration.parameter.SamlParameter.FULL_NAME_ATTRIBUTE;
import static com.epam.reportportal.auth.integration.parameter.SamlParameter.LAST_NAME_ATTRIBUTE;
import static com.epam.reportportal.base.infrastructure.persistence.commons.Predicates.equalTo;

import com.epam.reportportal.auth.integration.validator.request.param.provider.ParamNamesProvider;
import com.epam.reportportal.base.infrastructure.model.integration.auth.UpdateAuthRQ;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import java.util.function.Predicate;

public class SamlUpdateAuthRequestValidator extends UpdateAuthRequestValidator {

  private static final Predicate<UpdateAuthRQ> FULL_NAME_IS_EMPTY = request ->
      FULL_NAME_ATTRIBUTE.getParameter(request)
          .isEmpty();
  private static final Predicate<UpdateAuthRQ> FIRST_AND_LAST_NAME_IS_EMPTY = request ->
      LAST_NAME_ATTRIBUTE.getParameter(request).isEmpty() && FIRST_NAME_ATTRIBUTE.getParameter(
          request).isEmpty();

  public SamlUpdateAuthRequestValidator(ParamNamesProvider paramNamesProvider) {
    super(paramNamesProvider);
  }

  @Override
  public void validate(UpdateAuthRQ updateRequest) {
    super.validate(updateRequest);
    BusinessRule.expect(
        FULL_NAME_IS_EMPTY.test(updateRequest) && FIRST_AND_LAST_NAME_IS_EMPTY.test(updateRequest),
        equalTo(Boolean.FALSE)
    ).verify(ErrorType.BAD_REQUEST_ERROR,
        "Fields Full name or combination of Last name and First name are empty");
  }
}
