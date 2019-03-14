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

package com.epam.ta.reportportal.util.email;

import com.epam.reportportal.commons.template.TemplateEngine;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.EmailSettingsEnum;
import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.mail.MessagingException;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@ExtendWith(MockitoExtension.class)
class MailServiceFactoryTest {

	@Mock
	private TemplateEngine templateEngine;

	@Mock
	private IntegrationRepository integrationRepository;

	@Mock
	private IntegrationTypeRepository integrationTypeRepository;

	private Integration integration = mock(Integration.class);

	private IntegrationType integrationType = mock(IntegrationType.class);

	private EmailService emailService = mock(EmailService.class);

	private IntegrationParams integrationParams = mock(IntegrationParams.class);

	private BasicTextEncryptor basicTextEncryptor;

	private MailServiceFactory mailServiceFactory;

	@BeforeEach
	void setUp() {
		basicTextEncryptor = new BasicTextEncryptor();
		basicTextEncryptor.setPassword("123");
		mailServiceFactory = new MailServiceFactory(templateEngine, basicTextEncryptor, integrationRepository, integrationTypeRepository);
	}

	@Test
	void shouldThrowWhenIntegrationIsNull() {

		final ReportPortalException exception = assertThrows(ReportPortalException.class, () -> mailServiceFactory.getEmailService(null));
		assertEquals("Impossible interact with integration. Integration should be not null.", exception.getMessage());
	}

	@Test
	void shouldReturnEmptyWhenIntegrationIsDisabled() {

		when(integration.isEnabled()).thenReturn(false);

		Optional<EmailService> emailService = mailServiceFactory.getEmailService(integration);

		Assertions.assertFalse(emailService.isPresent());
	}

	@Test
	void shouldReturnEmptyWhenIntegrationParamsAreEmpty() {

		when(integration.isEnabled()).thenReturn(true);
		when(integration.getParams()).thenReturn(integrationParams);
		when(integrationParams.getParams()).thenReturn(null);

		Optional<EmailService> emailService = mailServiceFactory.getEmailService(integration);

		Assertions.assertFalse(emailService.isPresent());
	}

	@Test
	void shouldReturnEmailServiceWithAuthParamsWhenEnabled() {

		Map<String, Object> config = ImmutableMap.<String, Object>builder().put(EmailSettingsEnum.AUTH_ENABLED.getAttribute(), true)
				.put(EmailSettingsEnum.STAR_TLS_ENABLED.getAttribute(), true)
				.put(EmailSettingsEnum.USERNAME.getAttribute(), "user")
				.put(EmailSettingsEnum.PASSWORD.getAttribute(), basicTextEncryptor.encrypt("password"))
				.build();

		when(integration.isEnabled()).thenReturn(true);
		when(integration.getParams()).thenReturn(integrationParams);
		when(integrationParams.getParams()).thenReturn(config);

		Optional<EmailService> emailService = mailServiceFactory.getEmailService(integration);

		Assertions.assertTrue(emailService.isPresent());

		EmailService service = emailService.get();

		Assertions.assertEquals("password", service.getPassword());
		Assertions.assertEquals("user", service.getUsername());
		Properties javaMailProperties = service.getJavaMailProperties();
		Boolean startTlsEnabled = (Boolean) javaMailProperties.get("mail.smtp.starttls.enable");
		Assertions.assertTrue(startTlsEnabled);

	}

	@Test
	void shouldReturnEmailServiceWithoutAuthParamsWhenDisabled() {

		Map<String, Object> config = ImmutableMap.<String, Object>builder().put(EmailSettingsEnum.STAR_TLS_ENABLED.getAttribute(), true)
				.put(EmailSettingsEnum.USERNAME.getAttribute(), "user")
				.put(EmailSettingsEnum.PASSWORD.getAttribute(), basicTextEncryptor.encrypt("password"))
				.build();

		when(integration.isEnabled()).thenReturn(true);
		when(integration.getParams()).thenReturn(integrationParams);
		when(integrationParams.getParams()).thenReturn(config);

		Optional<EmailService> emailService = mailServiceFactory.getEmailService(integration);

		Assertions.assertTrue(emailService.isPresent());

		EmailService service = emailService.get();

		Assertions.assertNull(service.getPassword());
		Assertions.assertNull(service.getUsername());
		Properties javaMailProperties = service.getJavaMailProperties();
		Boolean startTlsEnabled = (Boolean) javaMailProperties.get("mail.smtp.starttls.enable");
		Assertions.assertFalse(startTlsEnabled);

	}

	@Test
	void shouldReturnEmailServiceWithSslEnabled() {

		Map<String, Object> config = ImmutableMap.<String, Object>builder().put(EmailSettingsEnum.AUTH_ENABLED.getAttribute(), true)
				.put(EmailSettingsEnum.SSL_ENABLED.getAttribute(), true)
				.put(EmailSettingsEnum.USERNAME.getAttribute(), "user")
				.put(EmailSettingsEnum.PASSWORD.getAttribute(), basicTextEncryptor.encrypt("password"))
				.build();

		when(integration.isEnabled()).thenReturn(true);
		when(integration.getParams()).thenReturn(integrationParams);
		when(integrationParams.getParams()).thenReturn(config);

		Optional<EmailService> emailService = mailServiceFactory.getEmailService(integration);

		Assertions.assertTrue(emailService.isPresent());

		EmailService service = emailService.get();

		Assertions.assertEquals("password", service.getPassword());
		Assertions.assertEquals("user", service.getUsername());
		Properties javaMailProperties = service.getJavaMailProperties();
		String sslClass = (String) javaMailProperties.get("mail.smtp.socketFactory.class");
		Assertions.assertEquals("javax.net.ssl.SSLSocketFactory", sslClass);

	}

	@Test
	void getDefaultEmailServiceWithoutConnectionCheckPositive() {

		when(integrationType.getId()).thenReturn(1L);
		when(integrationTypeRepository.findAllByIntegrationGroup(IntegrationGroupEnum.NOTIFICATION)).thenReturn(Lists.newArrayList(
				integrationType));
		when(integration.isEnabled()).thenReturn(true);
		when(integrationRepository.findAllGlobalInIntegrationTypeIds(any())).thenReturn(Lists.newArrayList(integration));

		Map<String, Object> config = ImmutableMap.<String, Object>builder().put(EmailSettingsEnum.AUTH_ENABLED.getAttribute(), true)
				.put(EmailSettingsEnum.SSL_ENABLED.getAttribute(), true)
				.put(EmailSettingsEnum.USERNAME.getAttribute(), "user")
				.put(EmailSettingsEnum.PASSWORD.getAttribute(), basicTextEncryptor.encrypt("password"))
				.build();

		when(integration.getParams()).thenReturn(integrationParams);
		when(integrationParams.getParams()).thenReturn(config);

		mailServiceFactory.getDefaultEmailService(false);
	}

	@Test
	void testConnectionPositive() throws MessagingException {

		doNothing().when(emailService).testConnection();

		mailServiceFactory.checkConnection(emailService);
	}

	@Test
	void testConnectionWithNullEmailService() {

		final ReportPortalException exception = assertThrows(ReportPortalException.class, () -> mailServiceFactory.checkConnection(null));

		Assertions.assertEquals(
				"Email server is not configured or configuration is incorrect. Please configure email server in Report Portal settings.",
				exception.getMessage()
		);
	}

	@Test
	void testConnectionNegative() throws MessagingException {

		doThrow(new MessagingException()).when(emailService).testConnection();

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> mailServiceFactory.checkConnection(emailService)
		);

		Assertions.assertEquals(
				"Email server is not configured or configuration is incorrect. Please configure email server in Report Portal settings.",
				exception.getMessage()
		);
	}

}