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
import com.epam.ta.reportportal.dao.ServerSettingsRepository;
import com.epam.ta.reportportal.entity.ServerSettings;
import com.epam.ta.reportportal.entity.ServerSettingsEnum;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.project.ProjectAttribute;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.jasypt.util.text.BasicTextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

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

	private final TemplateEngine templateEngine;
	private final BasicTextEncryptor encryptor;
	private final ServerSettingsRepository settingsRepository;

	@Autowired
	public MailServiceFactory(TemplateEngine templateEngine, BasicTextEncryptor encryptor, ServerSettingsRepository settingsRepository) {
		this.templateEngine = templateEngine;
		this.encryptor = encryptor;
		this.settingsRepository = settingsRepository;
	}

	/**
	 * Build mail service based on provided configs
	 *
	 * @param projectAttributes Project configuration attributes
	 * @param serverSettings    Server-level configuration
	 * @return Built email service
	 */
	public Optional<EmailService> getEmailService(Set<ProjectAttribute> projectAttributes, List<ServerSettings> serverSettings) {

		Map<String, String> configuration = ProjectUtils.createConfigurationFromProjectAttributes(projectAttributes);

		return getEmailService(serverSettings).flatMap(service -> {
			// if there is server email config, let's check project config
			if (MapUtils.isNotEmpty(configuration)) {
				// if project config is present, check whether sending emails is enabled and replace server properties with project properties
				if (BooleanUtils.toBoolean(configuration.get(ProjectAttributeEnum.EMAIL_ENABLED.getAttribute()))) {
					//update of present on project level
					ofNullable(configuration.get(ProjectAttributeEnum.EMAIL_FROM.getAttribute())).ifPresent(service::setFrom);
				}

			}
			return Optional.of(service);
		});
	}

	/**
	 * Build mail service based on provided configs
	 *
	 * @param serverSettings Server-level configuration
	 * @return Built email service
	 */
	public Optional<EmailService> getEmailService(List<ServerSettings> serverSettings) {

		Map<String, String> config = serverSettings.stream()
				.collect(Collectors.toMap(ServerSettings::getKey, ServerSettings::getValue, (prev, curr) -> prev));

		if (MapUtils.isNotEmpty(config) && BooleanUtils.toBoolean(config.get(ServerSettingsEnum.ENABLED.getAttribute()))) {

			boolean authRequired = BooleanUtils.toBoolean(config.get(ServerSettingsEnum.AUTH_ENABLED.getAttribute()));

			Properties javaMailProperties = new Properties();
			javaMailProperties.put("mail.smtp.connectiontimeout", DEFAULT_CONNECTION_TIMEOUT);
			javaMailProperties.put("mail.smtp.auth", authRequired);
			javaMailProperties.put("mail.smtp.starttls.enable",
					authRequired && BooleanUtils.toBoolean(config.get(ServerSettingsEnum.START_TLS_ENABLED.getAttribute()))
			);

			if (BooleanUtils.toBoolean(ServerSettingsEnum.SSL_ENABLED.getAttribute())) {
				javaMailProperties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
				javaMailProperties.put("mail.smtp.socketFactory.fallback", "false");
			}

			EmailService service = new EmailService(javaMailProperties);
			service.setTemplateEngine(templateEngine);
			service.setHost(ServerSettingsEnum.HOST.getAttribute());
			service.setPort(Integer.parseInt(ServerSettingsEnum.PORT.getAttribute()));
			service.setProtocol(ServerSettingsEnum.PROTOCOL.getAttribute());
			service.setFrom(ServerSettingsEnum.FROM.getAttribute());
			if (authRequired) {
				service.setUsername(ServerSettingsEnum.USERNAME.getAttribute());
				service.setPassword(encryptor.decrypt(ServerSettingsEnum.PASSWORD.getAttribute()));
			}
			return Optional.of(service);

		}

		return Optional.empty();
	}

	/**
	 * Build mail service based on default server configs and checks connection
	 *
	 * @return Built email service
	 */
	public Optional<EmailService> getDefaultEmailService() {
		return getEmailService(settingsRepository.findAll());
	}

	/**
	 * Build mail service based on default server configs and checks connection
	 *
	 * @return Built email service
	 */
	public Optional<EmailService> getDefaultEmailService(Set<ProjectAttribute> projectAttributes) {
		return getEmailService(projectAttributes, settingsRepository.findAll());
	}

	/**
	 * Build mail service based on default server configs and checks connection
	 *
	 * @return Built email service
	 */
	public EmailService getDefaultEmailService(Set<ProjectAttribute> projectAttributes, boolean checkConnection) {
		EmailService emailService = getEmailService(projectAttributes,
				settingsRepository.findAll()
		).orElseThrow(() -> emailConfigurationFail(null));

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
		EmailService emailService = getEmailService(settingsRepository.findAll()).orElseThrow(() -> emailConfigurationFail(null));

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
		return new ReportPortalException(EMAIL_CONFIGURATION_IS_INCORRECT, "Please configure email server in Report Portal settings.");
	}

}
