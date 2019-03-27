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

package com.epam.ta.reportportal.core.integration.util;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class IntegrationServiceImpl implements IntegrationService {

	@Autowired
	private IntegrationTypeRepository integrationTypeRepository;

	@Override
	public Integration createGlobalIntegration(String integrationTypeName, IntegrationGroupEnum integrationGroup,
			Map<String, Object> integrationParams) {
		IntegrationType integrationType = integrationTypeRepository.findByNameAndIntegrationGroup(integrationTypeName, integrationGroup)
				.orElseThrow(() -> new ReportPortalException(
						ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
						Suppliers.formattedSupplier("Email server integration with name - '{}' not found.", integrationTypeName).get()
				));
		return createIntegration(integrationType, integrationParams);
	}

	@Override
	public Integration createProjectIntegration(String integrationTypeName, IntegrationGroupEnum integrationGroup,
			ReportPortalUser.ProjectDetails projectDetails, Map<String, Object> integrationParams) {
		return null;
	}

	@Override
	public Integration updateGlobalIntegration(Long id, Map<String, Object> integrationParams) {
		return null;
	}

	@Override
	public Integration updateProjectIntegration(Long id, ReportPortalUser.ProjectDetails projectDetails,
			Map<String, Object> integrationParams) {
		return null;
	}

	private Integration createIntegration(IntegrationType integrationType, Map<String, Object> integrationParams) {
		Integration integration = new Integration();
		integration.setCreationDate(LocalDateTime.now());
		integration.setParams(new IntegrationParams(integrationParams));
		integration.setType(integrationType);
		return integration;
	}
}
