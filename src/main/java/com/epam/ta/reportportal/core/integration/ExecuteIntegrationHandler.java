/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.core.integration;

import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import java.util.Map;

/**
 * Executes one of provided commands for configured integration with id at existed plugin.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public interface ExecuteIntegrationHandler {

  /**
   * Executes provided common plugin command
   *
   * @param membershipDetails Membership details
   * @param pluginName      Command name
   * @param command         Command to be executed
   * @param executionParams Parameters for execute
   * @return Result of the command execution
   */
  Object executeCommand(MembershipDetails membershipDetails, String pluginName,
      String command,
      Map<String, Object> executionParams);

  /**
   * Executes provided plugin public command
   *
   * @param pluginName      Command name
   * @param command         Command to be executed
   * @param executionParams Parameters for execute
   * @return Result of the command execution
   */
  Object executePublicCommand(String pluginName, String command,
      Map<String, Object> executionParams);

  /**
   * Executes provided plugin command for existed integration
   *
   * @param membershipDetails Membership details
   * @param integrationId   Integration id
   * @param command         Command to be executed
   * @param executionParams Parameters for execute
   * @return Result of the command execution
   */
  Object executeCommand(MembershipDetails membershipDetails, Long integrationId,
      String command,
      Map<String, Object> executionParams);

}
