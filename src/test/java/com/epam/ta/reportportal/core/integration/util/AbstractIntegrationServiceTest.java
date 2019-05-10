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
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.entity.enums.AuthType;
import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
class AbstractIntegrationServiceTest {

	private IntegrationRepository integrationRepository = mock(IntegrationRepository.class);
	private PluginBox pluginBox = mock(PluginBox.class);
	private BtsExtension btsExtension = mock(BtsExtension.class);

	private AbstractBtsIntegrationService abstractIntegrationService;

	@BeforeEach
	void setUp() {
		BasicTextEncryptor basicTextEncryptor = new BasicTextEncryptor();
		basicTextEncryptor.setPassword("123");
		abstractIntegrationService = new AbstractBtsIntegrationService(integrationRepository, pluginBox) {
			@Override
			public Map<String, Object> retrieveIntegrationParams(Map<String, Object> integrationParams) {
				return null;
			}
		};
	}

	@Test
	void validateJira() {
		when(pluginBox.getInstance("jira", BtsExtension.class)).thenReturn(Optional.ofNullable(btsExtension));
		when(btsExtension.testConnection(any(Integration.class))).thenReturn(true);
		abstractIntegrationService.validateIntegration(getCorrectJira());
	}

	@Test
	void validateJiraProjectUnspecified() {
		Integration correctJira = getCorrectJira();
		correctJira.getParams().getParams().remove(BtsProperties.PROJECT.getName());
		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> abstractIntegrationService.validateIntegration(correctJira)
		);
		assertEquals("Impossible interact with integration. BTS project is not specified.", exception.getMessage());
	}

	@Test
	void validateJiraUrlUnspecified() {
		Integration jira = getCorrectJira();
		jira.getParams().getParams().remove(BtsProperties.URL.getName());
		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> abstractIntegrationService.validateIntegration(jira)
		);
		assertEquals("Impossible interact with integration. Url is not specified.", exception.getMessage());
	}

	@Test
	void validateJiraDuplicate() {
		//given
		Integration jira = getCorrectJira();

		//when
		when(integrationRepository.findGlobalBtsByUrlAndLinkedProject("jira-url", "jira-project")).thenReturn(Optional.of(jira));

		//then
		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> abstractIntegrationService.validateIntegration(jira)
		);
		assertEquals("Integration 'jira-url & jira-project' already exists. You couldn't create the duplicate.", exception.getMessage());
	}

	@Test
	void shouldCreateProjectIntegrationWhenValidParams() {
		//given
		final long projectId = 1L;
		Integration correctJira = getCorrectJira();
		Project project = new Project();
		project.setId(projectId);
		correctJira.setProject(project);

		//given
		when(integrationRepository.findProjectBtsByUrlAndLinkedProject("jira-url", "jira-project", projectId)).thenReturn(Optional.empty());
		when(pluginBox.getInstance("jira", BtsExtension.class)).thenReturn(Optional.ofNullable(btsExtension));
		when(btsExtension.testConnection(any(Integration.class))).thenReturn(true);

		//then
		abstractIntegrationService.validateIntegration(correctJira, project);
	}

	@Test
	void validateProjectJiraDuplicate() {
		//given
		Integration jira = getCorrectJira();
		Project project = new Project();
		long projectId = 1L;
		project.setId(projectId);

		//when
		when(integrationRepository.findProjectBtsByUrlAndLinkedProject("jira-url",
				"jira-project",
				projectId
		)).thenReturn(Optional.of(jira));

		//then
		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> abstractIntegrationService.validateIntegration(jira, project)
		);
		assertEquals("Integration 'jira-url & jira-project' already exists. You couldn't create the duplicate.", exception.getMessage());
	}

	private Integration getCorrectJira() {

		Integration integration = new Integration();

		IntegrationParams integrationParams = new IntegrationParams();
		Map<String, Object> params = new HashMap<>();
		params.put(BtsProperties.URL.getName(), "jira-url");
		params.put(BtsProperties.PROJECT.getName(), "jira-project");
		params.put(BtsProperties.AUTH_TYPE.getName(), AuthType.BASIC.name());
		params.put(BtsProperties.USER_NAME.getName(), "USERNAME");
		params.put(BtsProperties.PASSWORD.getName(), "PASSWORD");
		params.put(BtsProperties.OAUTH_ACCESS_KEY.getName(), "KEY");
		integrationParams.setParams(params);

		integration.setParams(integrationParams);

		IntegrationType integrationType = new IntegrationType();
		integrationType.setName("jira");
		integrationType.setIntegrationGroup(IntegrationGroupEnum.BTS);

		integration.setType(integrationType);

		return integration;
	}

}