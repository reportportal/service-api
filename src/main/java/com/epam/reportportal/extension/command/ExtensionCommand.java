/*
 * Copyright (C) 2026 EPAM Systems
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

package com.epam.reportportal.extension.command;

import com.epam.reportportal.api.model.PluginCommandRQ;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.extension.NamedPluginCommand;

public interface ExtensionCommand<T> extends NamedPluginCommand {

  /**
   * Executes plugin command with provided PluginCommandRQ.
   *
   * @param pluginCommandRq Plugin command request object
   * @return Result
   */
  default T executeCommand(PluginCommandRQ pluginCommandRq) {
    throw new UnsupportedOperationException("Command does not support execution without an integration");
  }


  /**
   * Executes plugin command with provided PluginCommandRQ.
   *
   * @param integration     Configured ReportPortal integration
   * @param pluginCommandRq Plugin command request object
   * @return Result
   */
  default T executeCommand(Integration integration, PluginCommandRQ pluginCommandRq) {
    throw new UnsupportedOperationException("Command does not support execution with an integration");
  }

}
