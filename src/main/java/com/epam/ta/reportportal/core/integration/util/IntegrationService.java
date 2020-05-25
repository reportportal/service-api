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

package com.epam.ta.reportportal.core.integration.util;

import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.ws.model.integration.IntegrationRQ;

import java.util.Map;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface IntegrationService {

	Integration createIntegration(IntegrationRQ integrationRq, IntegrationType integrationType);

	Integration updateIntegration(Integration integration, IntegrationRQ integrationRQ);

	boolean validateIntegration(Integration integration);

	boolean validateIntegration(Integration integration, Project project);

	boolean checkConnection(Integration integration);

	Map<String, Object> retrieveIntegrationParams(Map<String, Object> integrationParams);

	void decryptParams(Integration integration);

	void encryptParams(Integration integration);
}
