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

package com.epam.reportportal.base.core.settings;

import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Validates and manages the {@code server.password.min.length} configuration setting.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordMinLengthSettingHandler implements ServerSettingHandler {

  private static final int MIN_ALLOWED = 8;
  private static final int MAX_ALLOWED = 256;
  private static final String SERVER_PASSWORD_MIN_LENGTH_KEY = "server.password.min.length";

  /**
   * Validates the value for the password minimum length setting.
   *
   * @param value the configuration value to validate
   * @throws ReportPortalException if the value is not numeric or out of range
   */
  @Override
  public void validate(String value) {
    int parsedValue = parseValue(value);

    if (parsedValue < MIN_ALLOWED || parsedValue > MAX_ALLOWED) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          String.format("Value for '%s' must be between %d and %d. Provided: %d",
              SERVER_PASSWORD_MIN_LENGTH_KEY, MIN_ALLOWED, MAX_ALLOWED, parsedValue)
      );
    }
  }

  /**
   * Retrieves the password minimum length configuration key.
   *
   * @return the configuration key
   */
  @Override
  public String getKey() {
    return SERVER_PASSWORD_MIN_LENGTH_KEY;
  }

  private int parseValue(String value) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          String.format("Invalid value for '%s': '%s'. It must be a valid integer.",
              SERVER_PASSWORD_MIN_LENGTH_KEY, value)
      );
    }
  }
}
