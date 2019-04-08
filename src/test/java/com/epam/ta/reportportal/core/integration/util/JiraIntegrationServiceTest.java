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

import com.epam.ta.reportportal.core.integration.util.property.BtsProperties;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.entity.enums.AuthType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
class JiraIntegrationServiceTest {

	private static final String UNSUPPORTED_AUTH_TYPE_NAME = AuthType.NTLM.name();
	private IntegrationRepository integrationRepository = mock(IntegrationRepository.class);
	private PluginBox pluginBox = mock(PluginBox.class);

	private JiraIntegrationService jiraIntegrationService;

	@BeforeEach
	void setUp() {
		BasicTextEncryptor basicTextEncryptor = new BasicTextEncryptor();
		basicTextEncryptor.setPassword("123");
		jiraIntegrationService = new JiraIntegrationService(integrationRepository, pluginBox, basicTextEncryptor);
	}

	@Test
	void testParameters() {
		Map<String, Object> res = jiraIntegrationService.retrieveIntegrationParams(getCorrectJiraIntegrationParams());
		assertThat(res.keySet(), hasSize(5));
	}

	@Test
	void testParametersWithoutProject() {
		Map<String, Object> params = getCorrectJiraIntegrationParams();
		params.remove(BtsProperties.PROJECT.getName());
		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> jiraIntegrationService.retrieveIntegrationParams(params)
		);
		assertEquals("Impossible interact with integration. JIRA project value cannot be NULL", exception.getMessage());
	}

	@Test
	void testParametersWithoutURL() {
		Map<String, Object> params = getCorrectJiraIntegrationParams();
		params.remove(BtsProperties.URL.getName());
		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> jiraIntegrationService.retrieveIntegrationParams(params)
		);
		assertEquals("Impossible interact with integration. JIRA URL value cannot be NULL", exception.getMessage());
	}

	@Test
	void testParametersWithouAuthType() {
		Map<String, Object> params = getCorrectJiraIntegrationParams();
		params.remove(BtsProperties.AUTH_TYPE.getName());

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> jiraIntegrationService.retrieveIntegrationParams(params)
		);
		assertEquals("Impossible interact with integration. No auth property provided for Jira integration", exception.getMessage());
	}

	@Test
	void testParametersWithoutUsername() {
		Map<String, Object> params = getCorrectJiraIntegrationParams();
		params.remove(BtsProperties.USER_NAME.getName());

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> jiraIntegrationService.retrieveIntegrationParams(params)
		);
		assertEquals("Impossible interact with integration. Username value cannot be NULL", exception.getMessage());
	}

	@Test
	void testParametersWithouPassword() {
		Map<String, Object> params = getCorrectJiraIntegrationParams();
		params.remove(BtsProperties.PASSWORD.getName());

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> jiraIntegrationService.retrieveIntegrationParams(params)
		);
		assertEquals("Impossible interact with integration. Password value cannot be NULL", exception.getMessage());
	}

	@Test
	void testParametersWithouKey() {
		Map<String, Object> params = getCorrectJiraIntegrationParams();
		params.put(BtsProperties.AUTH_TYPE.getName(), AuthType.OAUTH.name());
		params.remove(BtsProperties.OAUTH_ACCESS_KEY.getName());
		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> jiraIntegrationService.retrieveIntegrationParams(params)
		);
		assertEquals("Impossible interact with integration. AccessKey value cannot be NULL", exception.getMessage());
	}

	@Test
	void testParametersUnopportetAuthType() {
		Map<String, Object> params = getCorrectJiraIntegrationParams();
		params.put(BtsProperties.AUTH_TYPE.getName(), UNSUPPORTED_AUTH_TYPE_NAME);

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> jiraIntegrationService.retrieveIntegrationParams(params)
		);
		assertEquals(
				"Impossible interact with integration. Unsupported auth type for Jira integration - " + UNSUPPORTED_AUTH_TYPE_NAME,
				exception.getMessage()
		);
	}

	private Map<String, Object> getCorrectJiraIntegrationParams() {

		Map<String, Object> params = new HashMap<>();
		params.put(BtsProperties.URL.getName(), "jira-url");
		params.put(BtsProperties.PROJECT.getName(), "jira-project");
		params.put(BtsProperties.AUTH_TYPE.getName(), AuthType.BASIC.name());
		params.put(BtsProperties.USER_NAME.getName(), "USERNAME");
		params.put(BtsProperties.PASSWORD.getName(), "PASSWORD");
		params.put(BtsProperties.OAUTH_ACCESS_KEY.getName(), "KEY");

		return params;
	}
}