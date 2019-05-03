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

import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.util.email.EmailService;
import com.epam.ta.reportportal.util.email.MailServiceFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.mail.MessagingException;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
class EmailServerIntegrationServiceTest {

	private final String integrationName = "email";

	private IntegrationRepository integrationRepository = mock(IntegrationRepository.class);
	private MailServiceFactory mailServiceFactory = mock(MailServiceFactory.class);
	private EmailService emailService = mock(EmailService.class);

	private EmailServerIntegrationService emailServerIntegrationService;

	@BeforeEach
	void setUp() {
		BasicTextEncryptor basicTextEncryptor = new BasicTextEncryptor();
		basicTextEncryptor.setPassword("123");
		emailServerIntegrationService = new EmailServerIntegrationService(integrationRepository, basicTextEncryptor, mailServiceFactory);
	}

	@Test
	void validateGlobalIntegration() throws MessagingException {
		//given
		Integration integration = new Integration();
		IntegrationType integrationType = new IntegrationType();
		integrationType.setName(integrationName);
		integration.setType(integrationType);

		//when
		when(integrationRepository.findAllGlobalByType(integrationType)).thenReturn(Lists.newArrayList());
		doNothing().when(emailService).testConnection();
		when(mailServiceFactory.getEmailService(integration)).thenReturn(Optional.of(emailService));

		boolean b = emailServerIntegrationService.validateGlobalIntegration(integration);

		//then
		assertTrue(b);
	}

	@Test
	void validateGlobalIntegrationNegative() throws MessagingException {
		//given
		Integration integration = new Integration();
		IntegrationType integrationType = new IntegrationType();
		integrationType.setName("email");
		integration.setType(integrationType);

		//when
		when(integrationRepository.findAllGlobalByType(integrationType)).thenReturn(Lists.newArrayList());
		when(mailServiceFactory.getEmailService(integration)).thenReturn(Optional.of(emailService));
		doThrow(MessagingException.class).when(emailService).testConnection();

		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> emailServerIntegrationService.validateGlobalIntegration(integration)
		);

		//then
		assertEquals("Forbidden operation. Email configuration is incorrect. Please, check your configuration. null", exception.getMessage()
		);
	}

	@Test
	void retrieveIntegrationParams() {
		Map<String, Object> map = emailServerIntegrationService.retrieveIntegrationParams(getParams());
		assertEquals(defaultParams(), map);
	}

	@Test
	void retrieveIntegrationParamsInvalidEmail() {
		Map<String, Object> params = Maps.newHashMap();
		params.put("from", "12345");
		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> emailServerIntegrationService.retrieveIntegrationParams(params)
		);
		assertEquals("Error in handled Request. Please, check specified parameters: 'Provided FROM value '12345' is invalid'",
				exception.getMessage()
		);
	}

	@Test
	void retrieveIntegrationParamsInvalidPort() {
		Map<String, Object> params = Maps.newHashMap();
		params.put("from", "from@mail.com");
		params.put("port", "123456789");
		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> emailServerIntegrationService.retrieveIntegrationParams(params)
		);
		assertEquals("Incorrect Request. Incorrect 'Port' value. Allowed value is [1..65535]", exception.getMessage());
	}

	private Map<String, Object> defaultParams() {
		Map<String, Object> res = Maps.newHashMap();
		res.put("protocol", "value2");
		res.put("host", "value3");
		res.put("from", "from@mail.com");
		return res;
	}

	private Map<String, Object> getParams() {
		Map<String, Object> params = Maps.newHashMap();
		params.put("from", "from@mail.com");
		params.put("protocol", "value2");
		params.put("host", "value3");

		return params;
	}
}