/*
 * Copyright 2026 EPAM Systems
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

package com.epam.reportportal.base.util;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility methods for reading environment variables.
 */
public final class EnvironmentUtils {

  private EnvironmentUtils() {
  }

  /**
   * Returns the value of the given environment variable, or {@code null} if the variable is not set or is empty.
   *
   * @param envVariableName the environment variable name
   * @return the value, or {@code null}
   */
  public static String getEnvVariable(String envVariableName) {
    return StringUtils.defaultIfEmpty(System.getenv(envVariableName), null);
  }

  /**
   * Returns the value of the given environment variable, or {@code defaultValue} if the variable is not set or is
   * empty.
   *
   * @param envVariableName the environment variable name
   * @param defaultValue    the fallback value
   * @return the value, or {@code defaultValue}
   */
  public static String getEnvVariableOrDefault(String envVariableName, String defaultValue) {
    return StringUtils.defaultIfEmpty(System.getenv(envVariableName), defaultValue);
  }
}
