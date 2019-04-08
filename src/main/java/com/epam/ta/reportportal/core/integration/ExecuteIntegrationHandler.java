/*
 * Copyright 2018 EPAM Systems
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

import com.epam.ta.reportportal.commons.ReportPortalUser;

import java.util.Map;

/**
 * Executes one of provided commands for configured integration with id
 * at existed plugin.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public interface ExecuteIntegrationHandler {

	/**
	 * Executes provided command
	 *
	 * @param projectDetails  Project details
	 * @param integrationId   Integration id
	 * @param command         Command to be executed
	 * @param executionParams Parameters for execute
	 * @return Result of the command execution
	 */
	Object executeCommand(ReportPortalUser.ProjectDetails projectDetails, Long integrationId, String command,
			Map<String, ?> executionParams);

}
