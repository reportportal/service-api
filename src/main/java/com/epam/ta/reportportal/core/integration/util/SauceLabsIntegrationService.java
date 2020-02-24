/*
 * Copyright 2020 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.integration.util;

import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.core.integration.util.property.SauceLabsProperties.ACCESS_TOKEN;
import static com.epam.ta.reportportal.core.integration.util.property.SauceLabsProperties.USERNAME;
import static com.epam.ta.reportportal.ws.model.ErrorType.BAD_REQUEST_ERROR;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
public class SauceLabsIntegrationService extends BasicIntegrationServiceImpl {

	private final BasicTextEncryptor encryptor;

	@Autowired
	public SauceLabsIntegrationService(IntegrationRepository integrationRepository, PluginBox pluginBox, BasicTextEncryptor encryptor) {
		super(integrationRepository, pluginBox);
		this.encryptor = encryptor;
	}

	@Override
	public Map<String, Object> retrieveIntegrationParams(Map<String, Object> integrationParams) {
		expect(integrationParams, MapUtils::isNotEmpty).verify(BAD_REQUEST_ERROR, "No integration params provided");

		final String encryptedToken = encryptor.encrypt(ACCESS_TOKEN.getParameter(integrationParams)
				.orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR, "AccessKey value cannot be NULL")));
		final String username = USERNAME.getParameter(integrationParams)
				.orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR, "Username is not specified"));

		HashMap<String, Object> result = Maps.newHashMap();
		result.put(ACCESS_TOKEN.getName(), encryptedToken);
		result.put(USERNAME.getName(), username);
		integrationParams.entrySet()
				.stream()
				.filter(it -> !it.getKey().equals(ACCESS_TOKEN.getName()) && !it.getKey().equals(USERNAME.getName()))
				.forEach(it -> result.put(it.getKey(), it.getValue()));

		return result;
	}

	@Override
	public boolean checkConnection(Integration integration) {
		decryptParams(integration);
		boolean connection = super.checkConnection(integration);
		ACCESS_TOKEN.getParameter(integration.getParams().getParams())
				.ifPresent(it -> ACCESS_TOKEN.setParameter(integration.getParams(), encryptor.encrypt(it)));
		return connection;
	}

	@Override
	public void decryptParams(Integration integration) {
		ACCESS_TOKEN.getParameter(integration.getParams().getParams())
				.ifPresent(it -> ACCESS_TOKEN.setParameter(integration.getParams(), encryptor.decrypt(it)));
	}
}
