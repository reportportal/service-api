/*
 * Copyright 2019 EPAM Systems
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

import com.epam.reportportal.extension.bugtracking.BtsExtension;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.integration.impl.util.IntegrationTestUtil;
import com.epam.ta.reportportal.core.integration.util.property.BtsProperties;
import com.epam.ta.reportportal.core.integration.util.property.ReportPortalIntegrationEnum;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.enums.AuthType;
import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.integration.IntegrationTypeDetails;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class JiraIntegrationServiceTest {

	private static final String JIRA_INTEGRATION_TYPE_NAME = ReportPortalIntegrationEnum.JIRA.name();
	private static final String UNSUPPORTED_AUTH_TYPE_NAME = AuthType.NTLM.name();

	private IntegrationTypeRepository integrationTypeRepository = mock(IntegrationTypeRepository.class);
	private IntegrationRepository integrationRepository = mock(IntegrationRepository.class);
	private PluginBox pluginBox = mock(PluginBox.class);
	private IntegrationType integrationType = mock(IntegrationType.class);
	private IntegrationType wrongIntegrationType = mock(IntegrationType.class);
	private IntegrationTypeDetails details = mock(IntegrationTypeDetails.class);
	private BtsExtension btsExtension = mock(BtsExtension.class);

	private JiraIntegrationService jiraIntegrationService;

	@BeforeEach
	void setUp() {
		BasicTextEncryptor basicTextEncryptor = new BasicTextEncryptor();
		basicTextEncryptor.setPassword("123");
		jiraIntegrationService = new JiraIntegrationService(integrationTypeRepository, integrationRepository, pluginBox, basicTextEncryptor
		);
	}

	@Test
	void shouldCreateGlobalIntegrationWhenValidParams() {

		when(integrationType.getName()).thenReturn(JIRA_INTEGRATION_TYPE_NAME);

		when(integrationTypeRepository.findByNameAndIntegrationGroup(JIRA_INTEGRATION_TYPE_NAME,
				IntegrationGroupEnum.BTS
		)).thenReturn(java.util.Optional.ofNullable(integrationType));

		when(integrationRepository.findGlobalBtsByUrlAndLinkedProject(anyString(), anyString())).thenReturn(Optional.empty());

		when(pluginBox.getInstance(JIRA_INTEGRATION_TYPE_NAME, BtsExtension.class)).thenReturn(Optional.ofNullable(btsExtension));

		when(btsExtension.testConnection(any(Integration.class))).thenReturn(true);

		jiraIntegrationService.createGlobalIntegration(JIRA_INTEGRATION_TYPE_NAME, getCorrectJiraIntegrationParams());
	}

	@Test
	void shouldNotCreateGlobalIntegrationWhenNoBtsProjectProvided() {
		Map<String, Object> params = getCorrectJiraIntegrationParams();
		params.remove(BtsProperties.PROJECT.getName());

		when(integrationTypeRepository.findByNameAndIntegrationGroup(JIRA_INTEGRATION_TYPE_NAME, IntegrationGroupEnum.BTS)).thenReturn(
				Optional.ofNullable(integrationType));

		when(integrationType.getDetails()).thenReturn(details);
		when(details.getDetails()).thenReturn(params);
		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> jiraIntegrationService.createGlobalIntegration(JIRA_INTEGRATION_TYPE_NAME, params)
		);

		assertEquals("Impossible interact with integration. JIRA project value cannot be NULL", exception.getMessage());
	}

	@Test
	void shouldNotCreateGlobalIntegrationWhenNoUrlProvided() {
		Map<String, Object> params = getCorrectJiraIntegrationParams();
		params.remove(BtsProperties.URL.getName());

		when(integrationTypeRepository.findByNameAndIntegrationGroup(JIRA_INTEGRATION_TYPE_NAME, IntegrationGroupEnum.BTS)).thenReturn(
				Optional.ofNullable(integrationType));

		when(integrationType.getDetails()).thenReturn(details);
		when(details.getDetails()).thenReturn(params);
		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> jiraIntegrationService.createGlobalIntegration(JIRA_INTEGRATION_TYPE_NAME, params)
		);

		assertEquals("Impossible interact with integration. JIRA URL value cannot be NULL", exception.getMessage());
	}

	@Test
	void shouldNotCreateGlobalIntegrationWhenNoAuthTypeProvided() {
		Map<String, Object> params = getCorrectJiraIntegrationParams();
		params.remove(BtsProperties.AUTH_TYPE.getName());

		when(integrationTypeRepository.findByNameAndIntegrationGroup(JIRA_INTEGRATION_TYPE_NAME, IntegrationGroupEnum.BTS)).thenReturn(
				Optional.ofNullable(integrationType));

		when(integrationType.getDetails()).thenReturn(details);
		when(details.getDetails()).thenReturn(params);

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> jiraIntegrationService.createGlobalIntegration(JIRA_INTEGRATION_TYPE_NAME, params)
		);
		assertEquals("Impossible interact with integration. No auth property provided for Jira integration", exception.getMessage());
	}

	@Test
	void shouldNotCreateGlobalIntegrationWhenNoUsernameProvided() {
		Map<String, Object> params = getCorrectJiraIntegrationParams();
		params.remove(BtsProperties.USER_NAME.getName());
		when(integrationTypeRepository.findByNameAndIntegrationGroup(JIRA_INTEGRATION_TYPE_NAME, IntegrationGroupEnum.BTS)).thenReturn(
				Optional.ofNullable(integrationType));

		when(integrationType.getDetails()).thenReturn(details);
		when(details.getDetails()).thenReturn(params);

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> jiraIntegrationService.createGlobalIntegration(JIRA_INTEGRATION_TYPE_NAME, params)
		);
		assertEquals("Impossible interact with integration. Username value cannot be NULL", exception.getMessage());
	}

	@Test
	void shouldNotCreateGlobalIntegrationWhenNoPasswordProvided() {
		Map<String, Object> params = getCorrectJiraIntegrationParams();
		params.remove(BtsProperties.PASSWORD.getName());
		when(integrationTypeRepository.findByNameAndIntegrationGroup(JIRA_INTEGRATION_TYPE_NAME, IntegrationGroupEnum.BTS)).thenReturn(
				Optional.ofNullable(integrationType));

		when(integrationType.getDetails()).thenReturn(details);
		when(details.getDetails()).thenReturn(params);

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> jiraIntegrationService.createGlobalIntegration(JIRA_INTEGRATION_TYPE_NAME, params)
		);
		assertEquals("Impossible interact with integration. Password value cannot be NULL", exception.getMessage());
	}

	@Test
	void shouldNotCreateGlobalIntegrationWhenNoOauthTokenProvided() {
		Map<String, Object> params = getCorrectJiraIntegrationParams();
		params.put(BtsProperties.AUTH_TYPE.getName(), AuthType.OAUTH.name());
		params.remove(BtsProperties.OAUTH_ACCESS_KEY.getName());
		when(integrationTypeRepository.findByNameAndIntegrationGroup(JIRA_INTEGRATION_TYPE_NAME, IntegrationGroupEnum.BTS)).thenReturn(
				Optional.ofNullable(integrationType));

		when(integrationType.getDetails()).thenReturn(details);
		when(details.getDetails()).thenReturn(params);

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> jiraIntegrationService.createGlobalIntegration(JIRA_INTEGRATION_TYPE_NAME, params)
		);
		assertEquals("Impossible interact with integration. AccessKey value cannot be NULL", exception.getMessage());
	}

	@Test
	void shouldNotCreateGlobalIntegrationWhenNotSupportedAuthTypeProvided() {
		Map<String, Object> params = getCorrectJiraIntegrationParams();
		params.put(BtsProperties.AUTH_TYPE.getName(), UNSUPPORTED_AUTH_TYPE_NAME);
		when(integrationTypeRepository.findByNameAndIntegrationGroup(JIRA_INTEGRATION_TYPE_NAME, IntegrationGroupEnum.BTS)).thenReturn(
				Optional.ofNullable(integrationType));

		when(integrationType.getDetails()).thenReturn(details);
		when(details.getDetails()).thenReturn(params);

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> jiraIntegrationService.createGlobalIntegration(JIRA_INTEGRATION_TYPE_NAME, params)
		);
		assertEquals(
				"Impossible interact with integration. Unsupported auth type for Jira integration - " + UNSUPPORTED_AUTH_TYPE_NAME,
				exception.getMessage()
		);
	}

	@Test
	void shouldCreateProjectIntegrationWhenValidParams() {

		Map<String, Object> params = getCorrectJiraIntegrationParams();

		when(integrationType.getName()).thenReturn(JIRA_INTEGRATION_TYPE_NAME);

		when(integrationTypeRepository.findByNameAndIntegrationGroup(JIRA_INTEGRATION_TYPE_NAME,
				IntegrationGroupEnum.BTS
		)).thenReturn(java.util.Optional.ofNullable(integrationType));

		when(integrationRepository.findProjectBtsByUrlAndLinkedProject(anyString(), anyString(), anyLong())).thenReturn(Optional.empty());

		when(pluginBox.getInstance(JIRA_INTEGRATION_TYPE_NAME, BtsExtension.class)).thenReturn(Optional.ofNullable(btsExtension));

		when(btsExtension.testConnection(any(Integration.class))).thenReturn(true);

		final long projectId = 1L;

		jiraIntegrationService.createProjectIntegration(JIRA_INTEGRATION_TYPE_NAME,
				new ReportPortalUser.ProjectDetails(projectId, "admin_personal", ProjectRole.PROJECT_MANAGER),
				params
		);
	}

	@Test
	void shouldUpdateGlobalIntegrationWhenValidParams() {
		Map<String, Object> correctJiraIntegrationParams = getCorrectJiraIntegrationParams();

		Integration jiraIntegration = IntegrationTestUtil.getGlobalJiraIntegration(1L, new HashMap<>());

		when(integrationRepository.findGlobalById(1L)).thenReturn(Optional.ofNullable(jiraIntegration));

		Integration integration = jiraIntegrationService.updateGlobalIntegration(1L, correctJiraIntegrationParams);

		Map<String, Object> params = integration.getParams().getParams();
		Assertions.assertNotNull(params);
		params.entrySet()
				.stream()
				.filter(e -> !e.getKey().equals(BtsProperties.PASSWORD.getName()))
				.forEach(e -> Assertions.assertEquals(e.getValue(), correctJiraIntegrationParams.get(e.getKey())));
	}

	@Test
	void shouldNotUpdateIntegrationWhenInvalidIntegrationGroup() {

		Map<String, Object> correctJiraIntegrationParams = getCorrectJiraIntegrationParams();

		Integration emailIntegration = IntegrationTestUtil.getGlobalEmailIntegration(1L);

		when(integrationRepository.findGlobalById(1L)).thenReturn(Optional.ofNullable(emailIntegration));

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> jiraIntegrationService.updateGlobalIntegration(1L, correctJiraIntegrationParams)
		);

		assertEquals(
				"Impossible interact with integration. Unable to update integration with type - 'NOTIFICATION'. Required type - 'BTS'",
				exception.getMessage()
		);

	}

	@Test
	void shouldUpdateProjectIntegrationWhenValidParams() {

		Map<String, Object> correctJiraIntegrationParams = getCorrectJiraIntegrationParams();

		Integration jiraIntegration = IntegrationTestUtil.getGlobalJiraIntegration(1L, new HashMap<>());

		final long projectId = 1L;
		when(integrationRepository.findByIdAndProjectId(1L, projectId)).thenReturn(Optional.ofNullable(jiraIntegration));

		Integration integration = jiraIntegrationService.updateProjectIntegration(1L,
				new ReportPortalUser.ProjectDetails(projectId, "admin_personal", ProjectRole.PROJECT_MANAGER),
				correctJiraIntegrationParams
		);

		Map<String, Object> params = integration.getParams().getParams();
		Assertions.assertNotNull(params);
		params.entrySet()
				.stream()
				.filter(e -> !e.getKey().equals(BtsProperties.PASSWORD.getName()))
				.forEach(e -> Assertions.assertEquals(e.getValue(), correctJiraIntegrationParams.get(e.getKey())));
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