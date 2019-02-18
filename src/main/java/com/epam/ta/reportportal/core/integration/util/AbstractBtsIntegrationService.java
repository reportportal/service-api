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

import com.epam.reportportal.extension.bugtracking.BtsConstants;
import com.epam.reportportal.extension.bugtracking.BtsExtension;
import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static com.epam.ta.reportportal.commons.Predicates.isPresent;
import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public abstract class AbstractBtsIntegrationService implements IntegrationService {

	private final IntegrationTypeRepository integrationTypeRepository;

	private final IntegrationRepository integrationRepository;

	private final PluginBox pluginBox;

	private final ProjectRepository projectRepository;

	@Autowired
	public AbstractBtsIntegrationService(IntegrationTypeRepository integrationTypeRepository, IntegrationRepository integrationRepository,
			PluginBox pluginBox, ProjectRepository projectRepository) {
		this.integrationTypeRepository = integrationTypeRepository;
		this.integrationRepository = integrationRepository;
		this.pluginBox = pluginBox;
		this.projectRepository = projectRepository;
	}

	@Override
	public Integration createGlobalIntegration(String integrationName, Map<String, Object> integrationParams) {

		Integration integration = createIntegration(integrationName, integrationParams);
		checkUniqueGlobalIntegration(integration);

		checkConnection(integration);

		return integration;
	}

	@Override
	public Integration createProjectIntegration(String integrationName, ReportPortalUser.ProjectDetails projectDetails,
			Map<String, Object> integrationParams) {

		Integration integration = createIntegration(integrationName, integrationParams);
		checkUniqueProjectIntegration(integration, projectDetails.getProjectId());

		checkConnection(integration);

		return integration;
	}

	protected abstract void validateIntegrationParams(Map<String, Object> integrationParams);

	private Integration createIntegration(String integrationName, Map<String, Object> integrationParams) {
		validateIntegrationParams(integrationParams);

		IntegrationType integrationType = integrationTypeRepository.findByNameAndIntegrationGroup(integrationName, IntegrationGroupEnum.BTS)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
						Suppliers.formattedSupplier("BTS integration with name - '{}' not found.", integrationName).get()
				));

		Integration integration = new Integration();
		integration.setCreationDate(LocalDateTime.now());
		integration.setParams(new IntegrationParams(integrationParams));
		integration.setType(integrationType);

		return integration;
	}

	private void checkUniqueGlobalIntegration(Integration integration) {
		String url = BtsConstants.URL.getParam(integration.getParams(), String.class)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Url is not specified."));
		String btsProject = BtsConstants.PROJECT.getParam(integration.getParams(), String.class)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "BTS project is not specified."));
		expect(integrationRepository.findGlobalBtsByUrlAndLinkedProject(url, btsProject),
				not(isPresent())
		).verify(ErrorType.INTEGRATION_ALREADY_EXISTS, url + " & " + btsProject);
	}

	private void checkUniqueProjectIntegration(Integration integration, Long projectId) {
		String url = BtsConstants.URL.getParam(integration.getParams(), String.class)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Url is not specified."));
		String btsProject = BtsConstants.PROJECT.getParam(integration.getParams(), String.class)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "BTS project is not specified."));
		expect(integrationRepository.findProjectBtsByUrlAndLinkedProject(url, btsProject, projectId),
				not(isPresent())
		).verify(ErrorType.INTEGRATION_ALREADY_EXISTS, url + " & " + btsProject);
	}

	private void checkConnection(Integration integration) {
		Optional<BtsExtension> extension = pluginBox.getInstance(integration.getType().getName(), BtsExtension.class);
		expect(extension, Optional::isPresent).verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				Suppliers.formattedSupplier("Could not find plugin with name '{}'.", integration.getType().getName())
		);

		expect(extension.get().connectionTest(integration), BooleanUtils::isTrue).verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				"Connection refused."
		);
	}
}
