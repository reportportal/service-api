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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.user.PasswordPolicyService;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.annotation.Annotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ValidPasswordValidatorTest {

  @Mock
  private PasswordPolicyService passwordPolicyService;
  @Mock
  private ConstraintValidatorContext context;

  @InjectMocks
  private ValidPasswordValidator validator;

  @Test
  void isValidWhenNullAndAllowNullTrueShouldReturnTrueWithoutDelegating() {
    // given
    initValidator(true);

    // when
    boolean result = validator.isValid(null, context);

    // then
    assertThat(result).isTrue();
    verify(passwordPolicyService, never()).isValid(null);
  }

  @Test
  void isValidWhenNullAndAllowNullFalseShouldDelegate() {
    // given
    when(passwordPolicyService.isValid(null)).thenReturn(false);

    // when
    boolean result = validator.isValid(null, context);

    // then
    assertThat(result).isFalse();
    verify(passwordPolicyService).isValid(null);
  }

  @Test
  void isValidWhenValueProvidedShouldReturnPolicyResult() {
    // given
    when(passwordPolicyService.isValid("Abcdef!1Abcd")).thenReturn(true);

    // when
    boolean result = validator.isValid("Abcdef!1Abcd", context);

    // then
    assertThat(result).isTrue();
  }

  private void initValidator(boolean allowNull) {
    validator.initialize(new ValidPassword() {
      @Override
      public String message() {
        return ValidPassword.DEFAULT_MESSAGE;
      }

      @Override
      public Class<?>[] groups() {
        return new Class[0];
      }

      @Override
      public Class<? extends Annotation> annotationType() {
        return ValidPassword.class;
      }

      @Override
      public Class<? extends jakarta.validation.Payload>[] payload() {
        return new Class[0];
      }

      @Override
      public boolean allowNull() {
        return allowNull;
      }
    });
  }

}
