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

package com.epam.reportportal.base.core.user;

import static com.epam.reportportal.base.infrastructure.model.ValidationConstraints.USER_PASSWORD_REGEXP;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.INCORRECT_REQUEST;

import com.epam.reportportal.base.infrastructure.persistence.dao.ServerSettingsRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.ServerSettings;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * Single source of truth for the password policy: static complexity rules plus the server-configurable minimum length
 * ({@code server.password.min.length}). Used both by
 * {@link com.epam.reportportal.base.ws.validation.ValidPasswordValidator} for legacy DTOs and, programmatically, by the
 * OpenAPI-generated request handlers whose contract-derived {@code @Pattern} cannot express a length resolved from a
 * runtime server setting.
 */
@Service
@RequiredArgsConstructor
public class PasswordPolicyService {

  private static final String MIN_LENGTH_KEY = "server.password.min.length";
  private static final Pattern COMPLEXITY = Pattern.compile(USER_PASSWORD_REGEXP);
  private static final int DEFAULT_MIN_LENGTH = 8;
  private static final int MAX_LENGTH = 36;

  private final ServerSettingsRepository serverSettingsRepository;

  /**
   * Checks whether the given password satisfies the full policy.
   *
   * @param password raw password to check
   * @return {@code true} if the password meets the complexity and length rules
   */
  public boolean isValid(String password) {
    return matchesComplexity(password) && password.length() >= resolveMinLength();
  }

  /**
   * Validates the given password against the full policy, throwing a {@code 400} error when it does not comply.
   *
   * @param password raw password to validate
   * @throws ReportPortalException with {@code INCORRECT_REQUEST} if the password is invalid
   */
  public void validate(String password) {
    if (!matchesComplexity(password)) {
      throw new ReportPortalException(INCORRECT_REQUEST,
          "[Field 'password' should match '%s' pattern.]".formatted(USER_PASSWORD_REGEXP));
    }
    int minLength = resolveMinLength();
    if (password.length() < minLength) {
      throw new ReportPortalException(INCORRECT_REQUEST,
          "[Field 'password' should be at least %d characters long.]".formatted(minLength));
    }
  }

  private boolean matchesComplexity(String password) {
    return StringUtils.isNotBlank(password)
        && password.length() <= MAX_LENGTH
        && COMPLEXITY.matcher(password).matches();
  }

  private int resolveMinLength() {
    return serverSettingsRepository.findByKey(MIN_LENGTH_KEY)
        .map(ServerSettings::getValue)
        .flatMap(this::parseIntegerSafely)
        .orElse(DEFAULT_MIN_LENGTH);
  }

  private Optional<Integer> parseIntegerSafely(String value) {
    try {
      return Optional.of(Integer.parseInt(value));
    } catch (NumberFormatException e) {
      return Optional.empty();
    }
  }
}
