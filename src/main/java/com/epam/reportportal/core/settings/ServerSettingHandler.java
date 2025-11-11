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

package com.epam.reportportal.core.settings;

/**
 * Interface for handling server setting updates based on a specific key. Each implementation handles one particular
 * key.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public interface ServerSettingHandler {

  /**
   * Validates an incoming value for this setting. Invoked <b>before</b> persisting the value. Implementations should
   * throw a {@code ReportPortalException} with {@code BAD_REQUEST_ERROR} for invalid values. Default implementation
   * accepts any value to keep backward compatibility.
   */
  default void validate(String value) {
    // no-op by default
  }

  /**
   * Post-persist side effects for the updated setting value. Called only if validation succeeded and the value has been
   * saved.
   *
   * @param value the new, already-persisted value
   */
  default void handle(String value) {
    // no-op by default
  }

  /**
   * Returns the server setting key this handler serves.
   *
   * @return the server setting key
   */
  String getKey();

}
