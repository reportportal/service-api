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
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.integration.util.property.JiraProperties;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.enums.AuthType;
import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static com.epam.ta.reportportal.commons.Predicates.isPresent;
import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.UNABLE_INTERACT_WITH_INTEGRATION;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class JiraIntegrationService implements IntegrationService {

	private final IntegrationTypeRepository integrationTypeRepository;

	private final IntegrationRepository integrationRepository;

	private final PluginBox pluginBox;

	private final BasicTextEncryptor basicTextEncryptor;

	@Autowired
	public JiraIntegrationService(IntegrationTypeRepository integrationTypeRepository, IntegrationRepository integrationRepository,
			PluginBox pluginBox, BasicTextEncryptor basicTextEncryptor) {
		this.integrationTypeRepository = integrationTypeRepository;
		this.integrationRepository = integrationRepository;
		this.pluginBox = pluginBox;
		this.basicTextEncryptor = basicTextEncryptor;
	}

	@Override
	public Integration createGlobalIntegration(String integrationName, Map<String, Object> integrationParams) {

		BusinessRule.expect(integrationParams, MapUtils::isNotEmpty).verify(ErrorType.BAD_REQUEST_ERROR, "No integration params provided");

		IntegrationType integrationType = integrationTypeRepository.findByNameAndIntegrationGroup(integrationName, IntegrationGroupEnum.BTS)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
						Suppliers.formattedSupplier("BTS integration with name - '{}' not found.", integrationName).get()
				));

		String authName = JiraProperties.AUTH_TYPE.getParam(integrationParams)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
						"No auth property provided for Jira integration"
				));
		AuthType authType = AuthType.findByName(authName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_AUTHENTICATION_TYPE, authName));

		if (AuthType.BASIC.equals(authType)) {
			expect(JiraProperties.USER_NAME.getParam(integrationParams), isPresent()).verify(UNABLE_INTERACT_WITH_INTEGRATION,
					"Username value cannot be NULL"
			);
			expect(JiraProperties.PASSWORD.getParam(integrationParams), isPresent()).verify(UNABLE_INTERACT_WITH_INTEGRATION,
					"Password value cannot be NULL"
			);

			integrationParams.put(JiraProperties.PASSWORD.getName(),
					basicTextEncryptor.encrypt(JiraProperties.PASSWORD.getParam(integrationParams).get())
			);

		} else if (AuthType.OAUTH.equals(authType)) {
			expect(JiraProperties.OAUTH_ACCESS_KEY.getParam(integrationParams), isPresent()).verify(UNABLE_INTERACT_WITH_INTEGRATION,
					"AccessKey value cannot be NULL"
			);
		} else {
			throw new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
					"Unsupported auth type for Jira integration - " + authType.name()
			);
		}
		expect(JiraProperties.PROJECT.getParam(integrationParams), isPresent()).verify(UNABLE_INTERACT_WITH_INTEGRATION,
				"JIRA project value cannot be NULL"
		);
		expect(JiraProperties.URL.getParam(integrationParams), isPresent()).verify(UNABLE_INTERACT_WITH_INTEGRATION,
				"JIRA URL value cannot be NULL"
		);

		Integration integration = new Integration();
		integration.setCreationDate(LocalDateTime.now());
		integration.setParams(new IntegrationParams(integrationParams));
		integration.setType(integrationType);

		Optional<BtsExtension> extension = pluginBox.getInstance(integrationType.getName(), BtsExtension.class);
		expect(extension, Optional::isPresent).verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				Suppliers.formattedSupplier("Could not find plugin with name '{}'.", integrationType.getName())
		);

		expect(extension.get().connectionTest(integration), BooleanUtils::isTrue).verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				"Connection refused."
		);

		integrationRepository.save(integration);

		return integration;
	}

	@Override
	public Integration createProjectIntegration(String integrationName, ReportPortalUser.ProjectDetails projectDetails,
			Map<String, Object> integrationParams) {

		//checkUnique(integration, projectDetails.getId());

		return null;
	}

	private void checkUnique(Integration integration, Long projectId) {
		String url = (String) BtsConstants.URL.getParam(integration.getParams())
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Url is not specified."));
		String btsProject = (String) BtsConstants.PROJECT.getParam(integration.getParams())
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "BTS project is not specified."));
		expect(integrationRepository.findProjectBtsByUrlAndLinkedProject(url, btsProject, projectId),
				not(isPresent())
		).verify(ErrorType.INTEGRATION_ALREADY_EXISTS, url + " & " + btsProject);
	}
}
