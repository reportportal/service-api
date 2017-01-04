/*
 * Copyright 2016 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.util.email;

import com.epam.reportportal.commons.template.TemplateEngine;
import com.epam.ta.reportportal.database.dao.ServerSettingsRepository;
import com.epam.ta.reportportal.database.entity.ServerSettings;
import com.epam.ta.reportportal.ws.model.settings.ServerEmailConfig;
import org.jasypt.util.text.BasicTextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Properties;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.fail;
import static com.epam.ta.reportportal.ws.model.ErrorType.EMAIL_CONFIGURATION_IS_INCORRECT;
import static com.epam.ta.reportportal.ws.model.ErrorType.FORBIDDEN_OPERATION;

/**
 * Factory for {@link EmailService}
 *
 * @author Andrei Varabyeu
 */
public class MailServiceFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(MailServiceFactory.class);
	private static final int DEFAULT_CONNECTION_TIMEOUT = 5000;
	private static final String DEFAULT_SETTINGS_PROFILE = "default";

	@Autowired
	private TemplateEngine templateEngine;
	@Autowired
	private BasicTextEncryptor encryptor;
	@Autowired
	private ServerSettingsRepository settingsRepository;

	/**
	 * Build mail service based on provided configs
	 *
	 * @param config          Email server configs
	 * @param connectionCheck check connection flag
	 * @return Built email service
	 */
	public EmailService getEmailService(ServerEmailConfig config, boolean connectionCheck) {
		boolean authRequired = (null != config.getAuthEnabled() && config.getAuthEnabled());

		Properties javaMailProperties = new Properties();
		javaMailProperties.put("mail.smtp.connectiontimeout", DEFAULT_CONNECTION_TIMEOUT);
		javaMailProperties.put("mail.smtp.auth", authRequired);
		javaMailProperties.put("mail.smtp.starttls.enable", authRequired && config.isStarTlsEnabled());
		javaMailProperties.put("mail.debug", config.isDebug());

		if (config.isSslEnabled()) {
			javaMailProperties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			javaMailProperties.put("mail.smtp.socketFactory.fallback", "false");
		}

		EmailService service = new EmailService(javaMailProperties);
		service.setTemplateEngine(templateEngine);
		service.setHost(config.getHost());
		service.setPort(config.getPort());
		service.setProtocol(config.getProtocol());
		if (authRequired) {
			service.setUsername(config.getUsername());
			service.setPassword(encryptor.decrypt(config.getPassword()));
		}

		if (connectionCheck) {
			checkConnection(service);
		}
		return service;
	}

	/**
	 * Build mail service based on default server configs and checks connection
	 *
	 * @return Built email service
	 */
	public EmailService getDefaultEmailService() {
		EmailService emailService = null;

		ServerSettings serverSettings = settingsRepository.findOne(DEFAULT_SETTINGS_PROFILE);
		if (null == serverSettings || null == serverSettings.getServerEmailConfig()) {
			fail().withError(EMAIL_CONFIGURATION_IS_INCORRECT,
					"Email server is not configured. Please config email server in Report Portal settings.");
		} else {
			emailService = getEmailService(serverSettings.getServerEmailConfig(), true);
		}
		return emailService;
	}

	private void checkConnection(EmailService service) {
		try {
			service.testConnection();
		} catch (Exception e) {
			LOGGER.error("Cannot send email to user", e);
			fail().withError(FORBIDDEN_OPERATION,
					"Email server is incorrect. " + e.getMessage());
		}
	}
}
