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

package com.epam.reportportal.base.infrastructure.annotations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class InCollectionValidator implements ConstraintValidator<In, Collection<String>> {

  private Set<String> allowedValues;

  @Override
  public void initialize(In constraintAnnotation) {
    allowedValues = Arrays.stream(constraintAnnotation.allowedValues())
        .map(String::toUpperCase)
        .collect(Collectors.toSet());
  }

  @Override
  public boolean isValid(Collection<String> value, ConstraintValidatorContext context) {
    List<String> upperCaseList = value.stream()
        .map(String::toUpperCase)
        .toList();

    return allowedValues.containsAll(upperCaseList);
  }
}
