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
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.project.email.ProjectEmailConfig;
import com.epam.ta.reportportal.ws.model.settings.ServerEmailConfig;
import org.jasypt.util.text.BasicTextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Properties;

import static com.epam.ta.reportportal.ws.model.ErrorType.EMAIL_CONFIGURATION_IS_INCORRECT;
import static java.util.Optional.ofNullable;

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
	 * @param projectConfig Project-level configuration
	 * @param serverConfig  Server-level configuration
	 * @return Built email service
	 */
	public Optional<EmailService> getEmailService(ProjectEmailConfig projectConfig, ServerEmailConfig serverConfig) {

		return getEmailService(serverConfig).flatMap(service -> {
			// if there is server email config, let's check project config
			Optional<ProjectEmailConfig> projectConf = ofNullable(projectConfig);
			if (projectConf.isPresent()) {
				// if project config is present, check whether sending emails is enabled and replace server properties with project properties
				return projectConf.filter(ProjectEmailConfig::getEmailEnabled).flatMap(pc -> {
					service.setAddressFrom(pc.getFrom());
					return Optional.of(service);
				});

			} else {
				return Optional.of(service);
			}
		});
	}

	/**
	 * Build mail service based on provided configs
	 *
	 * @param serverConfig Server-level configuration
	 * @return Built email service
	 */
	public Optional<EmailService> getEmailService(ServerEmailConfig serverConfig) {
		return ofNullable(serverConfig).map(serverConf -> {
			boolean authRequired = (null != serverConf.getAuthEnabled() && serverConf.getAuthEnabled());

			Properties javaMailProperties = new Properties();
			javaMailProperties.put("mail.smtp.connectiontimeout", DEFAULT_CONNECTION_TIMEOUT);
			javaMailProperties.put("mail.smtp.auth", authRequired);
			javaMailProperties.put("mail.smtp.starttls.enable", authRequired && serverConf.isStarTlsEnabled());
			javaMailProperties.put("mail.debug", serverConf.isDebug());

			if (serverConf.isSslEnabled()) {
				javaMailProperties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
				javaMailProperties.put("mail.smtp.socketFactory.fallback", "false");
			}

			EmailService service = new EmailService(javaMailProperties);
			service.setTemplateEngine(templateEngine);
			service.setHost(serverConf.getHost());
			service.setPort(serverConf.getPort());
			service.setProtocol(serverConf.getProtocol());
			if (authRequired) {
				service.setUsername(serverConf.getUsername());
				service.setPassword(encryptor.decrypt(serverConf.getPassword()));
			}
			return service;
		});
	}

	/**
	 * Build mail service based on default server configs and checks connection
	 *
	 * @return Built email service
	 */
	public Optional<EmailService> getDefaultEmailService() {
		return ofNullable(settingsRepository.findOne(DEFAULT_SETTINGS_PROFILE))
				.flatMap(serverSettings -> getEmailService(serverSettings.getServerEmailConfig()));

	}

	/**
	 * Build mail service based on default server configs and checks connection
	 *
	 * @return Built email service
	 */
	public Optional<EmailService> getDefaultEmailService(ProjectEmailConfig projectEmailConfig) {
		return ofNullable(settingsRepository.findOne(DEFAULT_SETTINGS_PROFILE))
				.flatMap(serverSettings -> getEmailService(projectEmailConfig, serverSettings.getServerEmailConfig()));
	}

	/**
	 * Build mail service based on default server configs and checks connection
	 *
	 * @return Built email service
	 */
	public EmailService getDefaultEmailService(ProjectEmailConfig projectEmailConfig, boolean checkConnection) {
		EmailService emailService = ofNullable(settingsRepository.findOne(DEFAULT_SETTINGS_PROFILE))
				.flatMap(serverSettings -> getEmailService(projectEmailConfig, serverSettings.getServerEmailConfig()))
				.orElseThrow(() -> emailConfigurationFail(null));

		if (checkConnection) {
			checkConnection(emailService);
		}
		return emailService;
	}

	/**
	 * Build mail service based on default server configs and checks connection
	 *
	 * @return Built email service
	 */
	public EmailService getDefaultEmailService(boolean checkConnection) {
		EmailService emailService = ofNullable(settingsRepository.findOne(DEFAULT_SETTINGS_PROFILE))
				.flatMap(serverSettings -> getEmailService(serverSettings.getServerEmailConfig()))
				.orElseThrow(() -> emailConfigurationFail(null));

		if (checkConnection) {
			checkConnection(emailService);
		}
		return emailService;
	}

	public void checkConnection(@Nullable EmailService service) {
		try {
			if (null == service) {
				throw emailConfigurationFail(null);
			} else {
				service.testConnection();
			}
		} catch (Exception e) {
			throw emailConfigurationFail(e);
		}
	}

	private ReportPortalException emailConfigurationFail(Throwable e) {
		if (null != e) {
			LOGGER.error("Cannot send email to user", e);
		}
		return new ReportPortalException(EMAIL_CONFIGURATION_IS_INCORRECT,
				"Email server is not configured or configuration is incorrect. Please config email server in Report Portal settings.");
	}

}
