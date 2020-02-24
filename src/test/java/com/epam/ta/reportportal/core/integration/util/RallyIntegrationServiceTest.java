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

import com.epam.ta.reportportal.core.integration.util.property.BtsProperties;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.entity.enums.AuthType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@ExtendWith(MockitoExtension.class)
class RallyIntegrationServiceTest {
	private static final String UNSUPPORTED_AUTH_TYPE_NAME = AuthType.NTLM.name();

	@Mock
	private IntegrationRepository integrationRepository;

	@Mock
	private PluginBox pluginBox;

	@Mock
	private BasicTextEncryptor encryptor;

	@InjectMocks
	private RallyIntegrationService rallyIntegrationService;

	@Test
	void testParameters() {
		when(encryptor.encrypt(any())).thenReturn("encrypted");
		Map<String, Object> res = rallyIntegrationService.retrieveIntegrationParams(getCorrectRallyIntegrationParams());
		assertThat(res.keySet(), hasSize(4));
	}

	@Test
	void testParametersWithoutKey() {
		Map<String, Object> params = getCorrectRallyIntegrationParams();
		params.remove(BtsProperties.OAUTH_ACCESS_KEY.getName());

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> rallyIntegrationService.retrieveIntegrationParams(params)
		);
		assertEquals("Impossible interact with integration. AccessKey value cannot be NULL", exception.getMessage());
	}

	@Test
	void testParametersUnsupportedAuthType() {
		Map<String, Object> params = getCorrectRallyIntegrationParams();
		params.put(BtsProperties.AUTH_TYPE.getName(), UNSUPPORTED_AUTH_TYPE_NAME);

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> rallyIntegrationService.retrieveIntegrationParams(params)
		);
		assertEquals(
				"Impossible interact with integration. Unsupported auth type for Rally integration - " + UNSUPPORTED_AUTH_TYPE_NAME,
				exception.getMessage()
		);
	}

	private Map<String, Object> getCorrectRallyIntegrationParams() {

		Map<String, Object> params = new HashMap<>();
		params.put(BtsProperties.URL.getName(), "rally-url");
		params.put(BtsProperties.PROJECT.getName(), "rally-project");
		params.put(BtsProperties.AUTH_TYPE.getName(), AuthType.OAUTH.name());
		params.put(BtsProperties.OAUTH_ACCESS_KEY.getName(), "KEY");

		return params;
	}

}