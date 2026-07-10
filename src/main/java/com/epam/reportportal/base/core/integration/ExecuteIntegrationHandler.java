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

package com.epam.reportportal.base.core.integration;

import com.epam.reportportal.api.model.PluginCommandRQ;

/**
 * Executes one of provided commands for configured integration with id at existed plugin.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public interface ExecuteIntegrationHandler {

  /**
   * Executes a plugin extension command. Tries a context-only command first; if not found, resolves the integration
   * from {@link PluginCommandRQ#getContext()} and executes an integration-scoped command.
   *
   * @param pluginName      name of the target plugin
   * @param commandName     name of the command to execute
   * @param pluginCommandRq structured command parameters including context
   * @return result of the command execution
   */
  Object executeExtensionCommand(String pluginName, String commandName, PluginCommandRQ pluginCommandRq);
}
