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
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;

/**
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
public interface DeleteIntegrationHandler {

	/**
	 * @param integrationId Integration id
	 * @return Response data
	 */
	OperationCompletionRS deleteGlobalIntegration(Long integrationId);

	/**
	 * @param type {@link com.epam.ta.reportportal.entity.integration.IntegrationType#name}
	 * @return Response data
	 */
	OperationCompletionRS deleteGlobalIntegrationsByType(String type);

	/**
	 * @param integrationId Integration id
	 * @param projectName   Project name
	 * @param user          {@link ReportPortalUser}
	 * @return Response data
	 */
	OperationCompletionRS deleteProjectIntegration(Long integrationId, String projectName, ReportPortalUser user);

	/**
	 * @param type        {@link com.epam.ta.reportportal.entity.integration.IntegrationType#name}
	 * @param projectName Project name
	 * @param user        {@link ReportPortalUser}
	 * @return Response data
	 */
	OperationCompletionRS deleteProjectIntegrationsByType(String type, String projectName, ReportPortalUser user);
}
