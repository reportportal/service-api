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
package com.epam.ta.reportportal.core.settings;

/**
 * Interface for handling server setting updates based on a specific key. Each implementation
 * handles one particular key.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public interface ServerSettingHandler {

  /**
   * Handles the updated value for the setting.
   *
   * @param value the new value associated with the setting
   */
  void handle(String value);

  /**
   * Returns the key this handler supports.
   *
   * @return the server setting key
   */
  String getKey();

}
