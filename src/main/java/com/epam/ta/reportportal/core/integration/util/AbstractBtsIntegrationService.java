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

import com.epam.reportportal.extension.bugtracking.BtsConstants;
import com.epam.reportportal.extension.bugtracking.BtsExtension;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public abstract class AbstractBtsIntegrationService extends BasicIntegrationServiceImpl {

	public AbstractBtsIntegrationService(IntegrationRepository integrationRepository, PluginBox pluginBox) {
		super(integrationRepository, pluginBox);
	}

	@Override
	public boolean validateIntegration(Integration integration) {
		validateCommonBtsParams(integration);
		expect(integrationRepository.existsByNameAndTypeIdAndProjectIdIsNull(integration.getName(), integration.getType().getId()),
				equalTo(Boolean.FALSE)
		).verify(ErrorType.INTEGRATION_ALREADY_EXISTS, integration.getName());
		return true;
	}

	@Override
	public boolean validateIntegration(Integration integration, Project project) {
		validateCommonBtsParams(integration);
		expect(integrationRepository.existsByNameAndTypeIdAndProjectId(integration.getName(),
				integration.getType().getId(),
				project.getId()
		), equalTo(Boolean.FALSE)).verify(ErrorType.INTEGRATION_ALREADY_EXISTS, integration.getName());
		return true;
	}

	private void validateCommonBtsParams(Integration integration) {
		expect(integration.getName(), StringUtils::isNotBlank).verify(ErrorType.BAD_REQUEST_ERROR, "Integration name should be specified");
		expect(BtsConstants.URL.getParam(integration.getParams(), String.class),
				Optional::isPresent
		).verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Url is not specified.");
		expect(BtsConstants.PROJECT.getParam(integration.getParams(), String.class),
				Optional::isPresent
		).verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "BTS project is not specified.");
	}

	@Override
	public boolean checkConnection(Integration integration) {
		BtsExtension extension = pluginBox.getInstance(integration.getType().getName(), BtsExtension.class)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
						Suppliers.formattedSupplier("Could not find plugin with name '{}'.", integration.getType().getName()).get()
				));
		expect(extension.testConnection(integration), BooleanUtils::isTrue).verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				"Connection refused."
		);
		return true;
	}
}
