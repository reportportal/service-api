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

import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.integration.util.property.BtsProperties;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.entity.enums.AuthType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

import static com.epam.ta.reportportal.ws.model.ErrorType.UNABLE_INTERACT_WITH_INTEGRATION;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class RallyIntegrationService extends AbstractBtsIntegrationService {

	private BasicTextEncryptor basicTextEncryptor;

	@Autowired
	public RallyIntegrationService(IntegrationRepository integrationRepository, PluginBox pluginBox,
			BasicTextEncryptor basicTextEncryptor) {
		super(integrationRepository, pluginBox);
		this.basicTextEncryptor = basicTextEncryptor;
	}

	@Override
	public Map<String, Object> retrieveIntegrationParams(Map<String, Object> integrationParams) {
		BusinessRule.expect(integrationParams, MapUtils::isNotEmpty).verify(ErrorType.BAD_REQUEST_ERROR, "No integration params provided");

		Map<String, Object> resultParams = Maps.newHashMapWithExpectedSize(BtsProperties.values().length);

		BtsProperties.AUTH_TYPE.getParam(integrationParams).ifPresent(authName -> {
			AuthType authType = AuthType.findByName(authName)
					.orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_AUTHENTICATION_TYPE, authName));

			if (AuthType.OAUTH.equals(authType)) {
				final String encryptedAccessKey = basicTextEncryptor.encrypt(BtsProperties.OAUTH_ACCESS_KEY.getParam(integrationParams)
						.orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "AccessKey value cannot be NULL")));
				resultParams.put(BtsProperties.OAUTH_ACCESS_KEY.getName(), encryptedAccessKey);
			} else {
				throw new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
						"Unsupported auth type for Rally integration - " + authType.name()
				);
			}

			resultParams.put(BtsProperties.AUTH_TYPE.getName(), authName);
		});

		BtsProperties.PROJECT.getParam(integrationParams)
				.ifPresent(btsProject -> resultParams.put(BtsProperties.PROJECT.getName(), btsProject));
		BtsProperties.URL.getParam(integrationParams).ifPresent(btsProject -> resultParams.put(BtsProperties.URL.getName(), btsProject));

		Optional.ofNullable(integrationParams.get("defectFormFields"))
				.ifPresent(defectFormFields -> resultParams.put("defectFormFields", defectFormFields));

		return resultParams;
	}
}
