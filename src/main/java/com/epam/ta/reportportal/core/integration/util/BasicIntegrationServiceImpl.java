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

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class BasicIntegrationServiceImpl implements IntegrationService {

	private final IntegrationRepository integrationRepository;

	@Autowired
	public BasicIntegrationServiceImpl(IntegrationRepository integrationRepository) {
		this.integrationRepository = integrationRepository;
	}

	@Override
	public Map<String, Object> retrieveIntegrationParams(Map<String, Object> integrationParams) {
		return integrationParams;
	}

	@Override
	public boolean validateGlobalIntegration(Integration globalIntegration) {

		List<Integration> global = integrationRepository.findAllGlobalByType(globalIntegration.getType());
		BusinessRule.expect(global, List::isEmpty).verify(
				ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				"Integration with type " + globalIntegration.getType().getName() + " is already exists"
		);
		return true;
	}

	@Override
	public boolean validateProjectIntegration(Integration integration, ReportPortalUser.ProjectDetails projectDetails) {
		List<Integration> project = integrationRepository.findAllByProjectIdAndType(projectDetails.getProjectId(), integration.getType());
		BusinessRule.expect(project, List::isEmpty).verify(
				ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				"Integration with type " + integration.getType().getName() + " is already exists for project "
						+ projectDetails.getProjectName()
		);
		return true;
	}

	@Override
	public boolean checkConnection(Integration integration) {
		//with plugin logic, check connection is on plugin's side
		return true;
	}
}
