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

package com.epam.ta.reportportal.ws.controller;

import com.epam.reportportal.api.PluginsApi;
import com.epam.reportportal.api.model.PluginCommandRQ;
import com.epam.ta.reportportal.core.integration.ExecuteIntegrationHandler;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for handling plugin command execution. This controller provides endpoints for executing commands on
 * registered plugins through the ReportPortal integration system.
 *
 * @author ReportPortal Team
 */
@RestController
@RequiredArgsConstructor
public class GeneratedPluginsController implements PluginsApi {

  private final ExecuteIntegrationHandler executeIntegrationHandler;

  /**
   * Executes a command on a specified plugin. This method handles the execution of plugin commands by delegating to the
   * ExecuteIntegrationHandler and wrapping the result in a standardized response format.
   *
   * @param pluginName      the name of the plugin to execute the command on
   * @param commandName     the name of the command to execute
   * @param pluginCommandRq the request object containing command parameters and data
   * @return ResponseEntity containing a map with the execution result under the "result" key
   */
  @Override
  @Transactional
  public ResponseEntity<Map<String, Object>> executePluginCommand(String pluginName, String commandName,
      PluginCommandRQ pluginCommandRq) {
    Object result = executeIntegrationHandler.executeCommand(pluginName, commandName, pluginCommandRq);
    Map<String, Object> response = new HashMap<>();
    response.put("result", result);
    return ResponseEntity.ok(response);
  }

}
