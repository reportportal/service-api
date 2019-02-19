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

import com.epam.reportportal.extension.bugtracking.BtsExtension;
import com.epam.ta.reportportal.core.integration.util.property.BtsProperties;
import com.epam.ta.reportportal.core.integration.util.property.ReportPortalIntegrationEnum;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.enums.AuthType;
import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class JiraIntegrationServiceTest {

	public static final String JIRA_INTEGRATION_TYPE_NAME = ReportPortalIntegrationEnum.JIRA.name();
	public static final String UNSUPPORTED_AUTH_TYPE_NAME = AuthType.NTLM.name();

	private IntegrationTypeRepository integrationTypeRepository = mock(IntegrationTypeRepository.class);
	private IntegrationRepository integrationRepository = mock(IntegrationRepository.class);
	private PluginBox pluginBox = mock(PluginBox.class);
	private IntegrationType integrationType = mock(IntegrationType.class);
	private BtsExtension btsExtension = mock(BtsExtension.class);

	private JiraIntegrationService jiraIntegrationService;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() {
		BasicTextEncryptor basicTextEncryptor = new BasicTextEncryptor();
		basicTextEncryptor.setPassword("123");
		jiraIntegrationService = new JiraIntegrationService(integrationTypeRepository,
				integrationRepository,
				pluginBox, basicTextEncryptor
		);
	}

	@Test
	public void shouldCreateGlobalIntegrationWhenValidParams() {

		when(integrationType.getName()).thenReturn(JIRA_INTEGRATION_TYPE_NAME);

		when(integrationTypeRepository.findByNameAndIntegrationGroup(JIRA_INTEGRATION_TYPE_NAME,
				IntegrationGroupEnum.BTS
		)).thenReturn(java.util.Optional.ofNullable(integrationType));

		when(integrationRepository.findGlobalBtsByUrlAndLinkedProject(anyString(), anyString())).thenReturn(Optional.empty());

		when(pluginBox.getInstance(JIRA_INTEGRATION_TYPE_NAME, BtsExtension.class)).thenReturn(Optional.ofNullable(btsExtension));

		when(btsExtension.connectionTest(any(Integration.class))).thenReturn(true);

		jiraIntegrationService.createGlobalIntegration(JIRA_INTEGRATION_TYPE_NAME, getCorrectJiraIntegrationParams());
	}

	@Test
	public void shouldNotCreateGlobalIntegrationWhenNoBtsProjectProvided() {

		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("JIRA project value cannot be NULL");

		Map<String, Object> params = getCorrectJiraIntegrationParams();
		params.remove(BtsProperties.PROJECT.getName());

		jiraIntegrationService.createGlobalIntegration(JIRA_INTEGRATION_TYPE_NAME, params);
	}

	@Test
	public void shouldNotCreateGlobalIntegrationWhenNoUrlProvided() {

		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("JIRA URL value cannot be NULL");

		Map<String, Object> params = getCorrectJiraIntegrationParams();
		params.remove(BtsProperties.URL.getName());

		jiraIntegrationService.createGlobalIntegration(JIRA_INTEGRATION_TYPE_NAME, params);
	}

	@Test
	public void shouldNotCreateGlobalIntegrationWhenNoAuthTypeProvided() {

		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("No auth property provided for Jira integration");

		Map<String, Object> params = getCorrectJiraIntegrationParams();
		params.remove(BtsProperties.AUTH_TYPE.getName());

		jiraIntegrationService.createGlobalIntegration(JIRA_INTEGRATION_TYPE_NAME, params);
	}

	@Test
	public void shouldNotCreateGlobalIntegrationWhenNoUsernameProvided() {

		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Username value cannot be NULL");

		Map<String, Object> params = getCorrectJiraIntegrationParams();
		params.remove(BtsProperties.USER_NAME.getName());

		jiraIntegrationService.createGlobalIntegration(JIRA_INTEGRATION_TYPE_NAME, params);
	}

	@Test
	public void shouldNotCreateGlobalIntegrationWhenNoPasswordProvided() {

		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Password value cannot be NULL");

		Map<String, Object> params = getCorrectJiraIntegrationParams();
		params.remove(BtsProperties.PASSWORD.getName());

		jiraIntegrationService.createGlobalIntegration(JIRA_INTEGRATION_TYPE_NAME, params);
	}

	@Test
	public void shouldNotCreateGlobalIntegrationWhenNoOauthTokenProvided() {

		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("AccessKey value cannot be NULL");

		Map<String, Object> params = getCorrectJiraIntegrationParams();
		params.put(BtsProperties.AUTH_TYPE.getName(), AuthType.OAUTH.name());
		params.remove(BtsProperties.OAUTH_ACCESS_KEY.getName());

		jiraIntegrationService.createGlobalIntegration(JIRA_INTEGRATION_TYPE_NAME, params);
	}

	@Test
	public void shouldNotCreateGlobalIntegrationWhenNotSupportedAuthTypeProvided() {

		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Unsupported auth type for Jira integration - " + UNSUPPORTED_AUTH_TYPE_NAME);

		Map<String, Object> params = getCorrectJiraIntegrationParams();
		params.put(BtsProperties.AUTH_TYPE.getName(), UNSUPPORTED_AUTH_TYPE_NAME);

		jiraIntegrationService.createGlobalIntegration(JIRA_INTEGRATION_TYPE_NAME, params);
	}

	@Test
	public void shouldCreateProjectIntegrationWhenValidParams() {

		Map<String, Object> params = getCorrectJiraIntegrationParams();

		when(integrationType.getName()).thenReturn(JIRA_INTEGRATION_TYPE_NAME);

		when(integrationTypeRepository.findByNameAndIntegrationGroup(JIRA_INTEGRATION_TYPE_NAME,
				IntegrationGroupEnum.BTS
		)).thenReturn(java.util.Optional.ofNullable(integrationType));

		when(integrationRepository.findProjectBtsByUrlAndLinkedProject(anyString(), anyString(), anyLong())).thenReturn(Optional.empty());

		when(pluginBox.getInstance(JIRA_INTEGRATION_TYPE_NAME, BtsExtension.class)).thenReturn(Optional.ofNullable(btsExtension));

		when(btsExtension.connectionTest(any(Integration.class))).thenReturn(true);
		jiraIntegrationService.createGlobalIntegration(JIRA_INTEGRATION_TYPE_NAME, params);
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