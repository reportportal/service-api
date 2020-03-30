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

import com.epam.ta.reportportal.core.integration.util.property.SauceLabsProperties;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.google.common.collect.Maps;
import org.apache.commons.collections.MapUtils;
import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class SauceLabsIntegrationServiceTest {

	@Mock
	private IntegrationRepository integrationRepository;

	@Mock
	private PluginBox pluginBox;

	@Mock
	private BasicTextEncryptor encryptor;

	@InjectMocks
	private SauceLabsIntegrationService sauceLabsIntegrationService;

	@Test
	void retrieveEmptyParamsTest() {
		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> sauceLabsIntegrationService.retrieveIntegrationParams(Collections.emptyMap())
		);
		assertEquals("Error in handled Request. Please, check specified parameters: 'No integration params provided'",
				exception.getMessage()
		);
	}

	@Test
	void retrieveParamsWithoutAccessKeyTest() {
		HashMap<String, Object> integrationParams = Maps.newHashMap();
		integrationParams.put(SauceLabsProperties.USERNAME.getName(), "user");
		ReportPortalException exception = assertThrows(ReportPortalException.class, () -> {
			sauceLabsIntegrationService.retrieveIntegrationParams(integrationParams);
		});
		assertEquals("Error in handled Request. Please, check specified parameters: 'AccessKey value cannot be NULL'",
				exception.getMessage()
		);
	}

	@Test
	void retrieveParamsWithoutUsernameTest() {
		HashMap<String, Object> integrationParams = Maps.newHashMap();
		integrationParams.put(SauceLabsProperties.ACCESS_TOKEN.getName(), "token");
		ReportPortalException exception = assertThrows(ReportPortalException.class, () -> {
			sauceLabsIntegrationService.retrieveIntegrationParams(integrationParams);
		});
		assertEquals("Error in handled Request. Please, check specified parameters: 'Username is not specified'", exception.getMessage());
	}

	@Test
	void retrieveParamsPositiveTest() {
		final HashMap<String, Object> integrationParams = Maps.newHashMap();
		integrationParams.put(SauceLabsProperties.USERNAME.getName(), "username");
		integrationParams.put(SauceLabsProperties.ACCESS_TOKEN.getName(), "token");
		integrationParams.put("param", "value");

		final String encryptedToken = "encryptedToken";
		when(encryptor.encrypt("token")).thenReturn(encryptedToken);

		final Map<String, Object> params = sauceLabsIntegrationService.retrieveIntegrationParams(integrationParams);

		assertNotNull(params);
		assertTrue(MapUtils.isNotEmpty(params));
		assertEquals(3, params.size());
		assertEquals(encryptedToken, params.get(SauceLabsProperties.ACCESS_TOKEN.getName()));
	}
}