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
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import java.util.Map;

/**
 * Executes one of provided commands for configured integration with id at existed plugin.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public interface ExecuteIntegrationHandler {

  /**
   * Executes a common plugin command using untyped parameter maps.
   *
   * @param membershipDetails membership context supplying project ID and key
   * @param pluginName        name of the target plugin
   * @param command           name of the command to execute
   * @param executionParams   untyped command parameters
   * @return result of the command execution
   * @deprecated Use {@link #executeExtensionCommand(String, String, PluginCommandRQ)} instead.
   */
  @Deprecated
  Object executeCommand(MembershipDetails membershipDetails, String pluginName,
      String command,
      Map<String, Object> executionParams);

  /**
   * Executes a public plugin command (name must start with {@code public_}).
   *
   * @param pluginName      name of the target plugin
   * @param command         name of the command to execute
   * @param executionParams untyped command parameters
   * @return result of the command execution
   * @deprecated Use {@link #executeExtensionCommand(String, String, PluginCommandRQ)} instead.
   */
  @Deprecated
  Object executePublicCommand(String pluginName, String command,
      Map<String, Object> executionParams);

  /**
   * Executes a plugin command against a specific integration instance.
   *
   * @param membershipDetails membership context supplying the project ID
   * @param integrationId     ID of the integration to execute the command against
   * @param command           name of the command to execute
   * @param executionParams   untyped command parameters
   * @return result of the command execution
   * @deprecated Use {@link #executeExtensionCommand(String, String, PluginCommandRQ)} instead.
   */
  @Deprecated
  Object executeCommand(MembershipDetails membershipDetails, Long integrationId,
      String command,
      Map<String, Object> executionParams);

  /**
   * Executes a common plugin command using a structured request object.
   *
   * @param pluginName      name of the target plugin
   * @param commandName     name of the command to execute
   * @param pluginCommandRq structured command parameters including context
   * @return result of the command execution
   * @deprecated Use {@link #executeExtensionCommand(String, String, PluginCommandRQ)} instead.
   */
  @Deprecated
  Object executeCommand(String pluginName, String commandName, PluginCommandRQ pluginCommandRq);

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
