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

package com.epam.ta.reportportal.ws.validation;

import static com.epam.reportportal.model.ValidationConstraints.USER_PASSWORD_REGEXP;

import com.epam.ta.reportportal.dao.ServerSettingsRepository;
import com.epam.ta.reportportal.entity.ServerSettings;
import com.google.common.base.Strings;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Bean Validation ConstraintValidator for {@link ValidPassword}.
 *
 */
public class ValidPasswordValidator implements ConstraintValidator<ValidPassword, String> {

  private static final String MIN_LENGTH_KEY = "server.password.min.length";
  private static final Pattern COMPLEXITY = Pattern.compile(USER_PASSWORD_REGEXP);
  private static final int DEFAULT_MIN = 8;
  private static final int MAX = 256;

  private final ServerSettingsRepository serverSettingsRepository;
  private boolean allowNull;

  public ValidPasswordValidator(ServerSettingsRepository serverSettingsRepository) {
    this.serverSettingsRepository = serverSettingsRepository;
  }

  @Override
  public void initialize(ValidPassword constraintAnnotation) {
    this.allowNull = constraintAnnotation.allowNull();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (allowNull && Objects.isNull(value)) {
      return true;
    }

    if (Strings.isNullOrEmpty(value)) {
      return false;
    }

    if (!COMPLEXITY.matcher(value).matches() || value.length() > MAX) {
      return false;
    }

    int minLength = serverSettingsRepository.findByKey(MIN_LENGTH_KEY)
        .map(ServerSettings::getValue)
        .flatMap(this::parseIntegerSafely)
        .orElse(DEFAULT_MIN);

    return value.length() >= minLength;
  }

  private Optional<Integer> parseIntegerSafely(String value) {
    try {
      return Optional.of(Integer.parseInt(value));
    } catch (NumberFormatException e) {
      return Optional.empty();
    }
  }
}
