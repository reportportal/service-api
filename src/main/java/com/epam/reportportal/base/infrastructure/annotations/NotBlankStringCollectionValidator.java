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
import java.util.Collection;

/**
 * Bean Validation implementation for {@link NotBlankStringCollection}.
 *
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class NotBlankStringCollectionValidator implements
    ConstraintValidator<NotBlankStringCollection, Collection<String>> {

  @Override
  public boolean isValid(Collection<String> value, ConstraintValidatorContext context) {
    if (null == value) {
      return true;
    }
    for (String next : value) {
      if (next.trim().isEmpty()) {
        return false;
      }
    }
    return true;
  }
}
