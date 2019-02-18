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

import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.integration.util.property.BtsProperties;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.enums.AuthType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.collections.MapUtils;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.epam.ta.reportportal.commons.Predicates.isPresent;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.UNABLE_INTERACT_WITH_INTEGRATION;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class JiraIntegrationService extends AbstractBtsIntegrationService {

	private final BasicTextEncryptor basicTextEncryptor;

	@Autowired
	public JiraIntegrationService(IntegrationTypeRepository integrationTypeRepository, IntegrationRepository integrationRepository,
			PluginBox pluginBox, BasicTextEncryptor basicTextEncryptor) {
		super(integrationTypeRepository, integrationRepository, pluginBox);
		this.basicTextEncryptor = basicTextEncryptor;
	}

	protected void validateIntegrationParams(Map<String, Object> integrationParams) {
		BusinessRule.expect(integrationParams, MapUtils::isNotEmpty).verify(ErrorType.BAD_REQUEST_ERROR, "No integration params provided");

		String authName = BtsProperties.AUTH_TYPE.getParam(integrationParams)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
						"No auth property provided for Jira integration"
				));
		AuthType authType = AuthType.findByName(authName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_AUTHENTICATION_TYPE, authName));

		if (AuthType.BASIC.equals(authType)) {
			expect(BtsProperties.USER_NAME.getParam(integrationParams), isPresent()).verify(UNABLE_INTERACT_WITH_INTEGRATION,
					"Username value cannot be NULL"
			);
			expect(BtsProperties.PASSWORD.getParam(integrationParams), isPresent()).verify(UNABLE_INTERACT_WITH_INTEGRATION,
					"Password value cannot be NULL"
			);

			integrationParams.put(BtsProperties.PASSWORD.getName(),
					basicTextEncryptor.encrypt(BtsProperties.PASSWORD.getParam(integrationParams).get())
			);

		} else if (AuthType.OAUTH.equals(authType)) {
			expect(BtsProperties.OAUTH_ACCESS_KEY.getParam(integrationParams), isPresent()).verify(UNABLE_INTERACT_WITH_INTEGRATION,
					"AccessKey value cannot be NULL"
			);
		} else {
			throw new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
					"Unsupported auth type for Jira integration - " + authType.name()
			);
		}
		expect(BtsProperties.PROJECT.getParam(integrationParams), isPresent()).verify(UNABLE_INTERACT_WITH_INTEGRATION,
				"JIRA project value cannot be NULL"
		);
		expect(BtsProperties.URL.getParam(integrationParams), isPresent()).verify(UNABLE_INTERACT_WITH_INTEGRATION,
				"JIRA URL value cannot be NULL"
		);
	}
}
