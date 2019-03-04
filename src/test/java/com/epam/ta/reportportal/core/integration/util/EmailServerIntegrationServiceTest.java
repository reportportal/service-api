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

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.integration.impl.util.IntegrationTestUtil;
import com.epam.ta.reportportal.core.integration.util.property.ReportPortalIntegrationEnum;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.util.email.EmailService;
import com.epam.ta.reportportal.util.email.MailServiceFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.mail.MessagingException;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class EmailServerIntegrationServiceTest {

	private final String integrationName = ReportPortalIntegrationEnum.EMAIL.name();

	private IntegrationTypeRepository integrationTypeRepository = mock(IntegrationTypeRepository.class);
	private IntegrationRepository integrationRepository = mock(IntegrationRepository.class);
	private MailServiceFactory mailServiceFactory = mock(MailServiceFactory.class);
	private EmailService emailService = mock(EmailService.class);

	private EmailServerIntegrationService emailServerIntegrationService;

	@BeforeEach
	void setUp() {
		BasicTextEncryptor basicTextEncryptor = new BasicTextEncryptor();
		basicTextEncryptor.setPassword("123");
		emailServerIntegrationService = new EmailServerIntegrationService(integrationTypeRepository,
				integrationRepository,
				basicTextEncryptor,
				mailServiceFactory
		);
	}

	@Test
	void shouldUpdateGlobalIntegration() {

		when(integrationRepository.findById(1L)).thenReturn(Optional.of(IntegrationTestUtil.getGlobalEmailIntegration(1L)));

		Map<String, Object> correctParams = getParams();
		Integration integration = emailServerIntegrationService.updateGlobalIntegration(1L, correctParams);

		Map<String, Object> params = integration.getParams().getParams();
		Assertions.assertNotNull(params);
		correctParams.forEach((key, value) -> Assertions.assertEquals(value, params.get(key)));
	}

	@Test
	void shouldUpdateProjectIntegration() {

		final long projectId = 1L;

		when(integrationRepository.findByIdAndProjectId(1L,
				projectId
		)).thenReturn(Optional.of(IntegrationTestUtil.getProjectEmailIntegration(1L, projectId)));

		Map<String, Object> correctParams = getParams();

		Integration integration = emailServerIntegrationService.updateProjectIntegration(1L,
				new ReportPortalUser.ProjectDetails(projectId, "admin_personal", ProjectRole.PROJECT_MANAGER),
				correctParams
		);

		Map<String, Object> params = integration.getParams().getParams();
		Assertions.assertNotNull(params);
		correctParams.forEach((key, value) -> Assertions.assertEquals(value, params.get(key)));
	}

	@Test
	void shouldCheckConnection() throws MessagingException {

		Map<String, Object> correctParams = getParams();

		IntegrationType emailIntegrationType = IntegrationTestUtil.getEmailIntegrationType();
		when(integrationTypeRepository.findByNameAndIntegrationGroup(integrationName, IntegrationGroupEnum.NOTIFICATION)).thenReturn(
				Optional.ofNullable(emailIntegrationType));

		Integration emailIntegration = IntegrationTestUtil.getGlobalEmailIntegration(1L);
		when(integrationRepository.findAllGlobalByType(emailIntegrationType)).thenReturn(Lists.newArrayList(emailIntegration));

		when(mailServiceFactory.getEmailService(emailIntegration)).thenReturn(Optional.ofNullable(emailService));

		doNothing().when(emailService).testConnection();

		Integration integration = emailServerIntegrationService.createGlobalIntegration(integrationName, correctParams);

		Map<String, Object> params = integration.getParams().getParams();
		Assertions.assertNotNull(params);
		correctParams.forEach((key, value) -> Assertions.assertEquals(value, params.get(key)));
	}

	private Map<String, Object> getParams() {
		Map<String, Object> params = Maps.newHashMap();
		params.put("from", "from@mail.com");
		params.put("protocol", "value2");
		params.put("host", "value3");

		return params;
	}
}
