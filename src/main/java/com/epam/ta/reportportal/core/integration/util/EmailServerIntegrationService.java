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

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.admin.ServerAdminHandlerImpl;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.entity.EmailSettingsEnum;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.util.email.EmailService;
import com.epam.ta.reportportal.util.email.MailServiceFactory;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.google.common.collect.Maps;
import com.mchange.lang.IntegerUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.jasypt.util.text.BasicTextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.util.Map;
import java.util.Optional;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.fail;
import static com.epam.ta.reportportal.util.UserUtils.isEmailValid;
import static com.epam.ta.reportportal.ws.model.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.ta.reportportal.ws.model.ErrorType.FORBIDDEN_OPERATION;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class EmailServerIntegrationService extends BasicIntegrationServiceImpl {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServerAdminHandlerImpl.class);

	private final BasicTextEncryptor basicTextEncryptor;
	private final MailServiceFactory emailServiceFactory;

	@Autowired
	public EmailServerIntegrationService(IntegrationRepository integrationRepository, BasicTextEncryptor basicTextEncryptor,
			MailServiceFactory emailServiceFactory) {
		super(integrationRepository);
		this.basicTextEncryptor = basicTextEncryptor;
		this.emailServiceFactory = emailServiceFactory;
	}

	@Override
	public boolean validateGlobalIntegration(Integration integration) {
		super.validateGlobalIntegration(integration);
		testConnection(integration);
		return true;
	}

	@Override
	public boolean validateProjectIntegration(Integration integration, ReportPortalUser.ProjectDetails projectDetails) {
		super.validateProjectIntegration(integration, projectDetails);
		testConnection(integration);
		return true;
	}

	@Override
	public Map<String, Object> retrieveIntegrationParams(Map<String, Object> integrationParams) {
		BusinessRule.expect(integrationParams, MapUtils::isNotEmpty).verify(ErrorType.BAD_REQUEST_ERROR, "No integration params provided");

		Map<String, Object> resultParams = Maps.newHashMapWithExpectedSize(EmailSettingsEnum.values().length);

		Optional<String> fromAttribute = EmailSettingsEnum.FROM.getAttribute(integrationParams);

		fromAttribute.ifPresent(from -> {
			expect(isEmailValid(from), equalTo(true)).verify(BAD_REQUEST_ERROR,
					Suppliers.formattedSupplier("Provided FROM value '{}' is invalid", fromAttribute.get())
			);
			resultParams.put(EmailSettingsEnum.FROM.getAttribute(), from);
		});

		int port = ofNullable(integrationParams.get(EmailSettingsEnum.PORT.getAttribute())).map(p -> IntegerUtils.parseInt(String.valueOf(p),
				25
		)).orElse(25);
		if ((port <= 0) || (port > 65535)) {
			BusinessRule.fail().withError(ErrorType.INCORRECT_REQUEST, "Incorrect 'Port' value. Allowed value is [1..65535]");
		}
		resultParams.put(EmailSettingsEnum.PORT.getAttribute(), port);

		if (!EmailSettingsEnum.PROTOCOL.getAttribute(integrationParams).isPresent()) {
			resultParams.put(EmailSettingsEnum.PROTOCOL.getAttribute(), "smtp");
		} else {
			resultParams.put(EmailSettingsEnum.PROTOCOL.getAttribute(), EmailSettingsEnum.PROTOCOL.getAttribute(integrationParams).get());
		}

		Boolean isAuthEnabled = ofNullable(integrationParams.get(EmailSettingsEnum.AUTH_ENABLED.getAttribute())).map(e -> BooleanUtils.toBoolean(
				String.valueOf(e))).orElse(false);
		if (isAuthEnabled) {
			EmailSettingsEnum.USERNAME.getAttribute(integrationParams)
					.ifPresent(username -> resultParams.put(EmailSettingsEnum.USERNAME.getAttribute(), username));
			EmailSettingsEnum.PASSWORD.getAttribute(integrationParams)
					.ifPresent(password -> resultParams.put(EmailSettingsEnum.PASSWORD.getAttribute(),
							basicTextEncryptor.encrypt(password)
					));
		} else {
			/* Auto-drop values on switched-off authentication */
			resultParams.put(EmailSettingsEnum.USERNAME.getAttribute(), null);
			resultParams.put(EmailSettingsEnum.PASSWORD.getAttribute(), null);
		}
		resultParams.put(EmailSettingsEnum.AUTH_ENABLED.getAttribute(), isAuthEnabled);

		EmailSettingsEnum.STAR_TLS_ENABLED.getAttribute(integrationParams)
				.ifPresent(attr -> resultParams.put(EmailSettingsEnum.STAR_TLS_ENABLED.getAttribute(), BooleanUtils.toBoolean(attr)));
		EmailSettingsEnum.SSL_ENABLED.getAttribute(integrationParams)
				.ifPresent(attr -> resultParams.put(EmailSettingsEnum.SSL_ENABLED.getAttribute(), BooleanUtils.toBoolean(attr)));
		EmailSettingsEnum.HOST.getAttribute(integrationParams)
				.ifPresent(attr -> resultParams.put(EmailSettingsEnum.HOST.getAttribute(), attr));

		return resultParams;
	}

	private void testConnection(Integration integration) {
		Optional<EmailService> emailService = emailServiceFactory.getEmailService(integration);
		if (emailService.isPresent()) {
			try {
				emailService.get().testConnection();
			} catch (MessagingException ex) {
				LOGGER.error("Cannot send email to user", ex);
				fail().withError(FORBIDDEN_OPERATION,
						"Email configuration is incorrect. Please, check your configuration. " + ex.getMessage()
				);
			}
		}
	}

}

