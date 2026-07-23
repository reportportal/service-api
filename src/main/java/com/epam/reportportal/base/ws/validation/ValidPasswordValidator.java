/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.ws.validation;


import com.epam.reportportal.base.core.user.PasswordPolicyService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;
import lombok.RequiredArgsConstructor;

/**
 * Bean Validation ConstraintValidator for {@link ValidPassword}. Delegates the actual policy check to
 * {@link PasswordPolicyService}.
 *
 */
@RequiredArgsConstructor
public class ValidPasswordValidator implements ConstraintValidator<ValidPassword, String> {

  private final PasswordPolicyService passwordPolicyService;
  private boolean allowNull;

  @Override
  public void initialize(ValidPassword constraintAnnotation) {
    this.allowNull = constraintAnnotation.allowNull();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (allowNull && Objects.isNull(value)) {
      return true;
    }
    return passwordPolicyService.isValid(value);
  }
}
