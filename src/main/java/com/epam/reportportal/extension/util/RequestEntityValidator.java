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

package com.epam.reportportal.extension.util;


import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class RequestEntityValidator {

  public static final String VALIDATION_EXCEPTION_DELIMITER = ", ";

  private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

  private RequestEntityValidator() {
    //static only
  }

  public static <T> void validate(T entity) {
    final List<String> errors = VALIDATOR.validate(entity)
        .stream()
        .map(it -> it.getPropertyPath() + " " + it.getMessage())
        .collect(Collectors.toList());
    if (!errors.isEmpty()) {
      throw new ReportPortalException(
          ErrorType.BAD_REQUEST_ERROR, String.join(VALIDATION_EXCEPTION_DELIMITER, errors));
    }
  }
}
