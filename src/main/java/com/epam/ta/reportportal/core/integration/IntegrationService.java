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

import com.epam.ta.reportportal.entity.project.Project;

import java.util.Map;

/**
 * Validates parameters of provided integration.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public interface IntegrationService {

	/**
	 * Validates parameters
	 *
	 * @param project               Project
	 * @param integrationParameters Integration parameters
	 * @return true if valid, false if not
	 */
	boolean validateIntegrationParameters(Project project, Map<String, Object> integrationParameters);

}
