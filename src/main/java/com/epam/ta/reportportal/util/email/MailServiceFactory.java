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
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.EmailSettingsEnum;
import com.epam.ta.reportportal.entity.enums.IntegrationGroupEnum;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jasypt.util.text.BasicTextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.ws.model.ErrorType.EMAIL_CONFIGURATION_IS_INCORRECT;
import static java.util.Optional.ofNullable;

/**
 * Factory for {@link EmailService}
 *
 * @author Andrei Varabyeu
 */
@Service
public class MailServiceFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(MailServiceFactory.class);
	private static final int DEFAULT_CONNECTION_TIMEOUT = 5000;
	private static final String FROM_ADDRESS = "fromAddress";

	private final TemplateEngine templateEngine;
	private final BasicTextEncryptor encryptor;
	private final IntegrationRepository integrationRepository;
	private final IntegrationTypeRepository integrationTypeRepository;

	@Autowired
	public MailServiceFactory(TemplateEngine templateEngine, BasicTextEncryptor encryptor, IntegrationRepository integrationRepository,
			IntegrationTypeRepository integrationTypeRepository) {
		this.templateEngine = templateEngine;
		this.encryptor = encryptor;
		this.integrationRepository = integrationRepository;
		this.integrationTypeRepository = integrationTypeRepository;
	}

	/**
	 * Build mail service based on provided configs
	 *
	 * @param integration Email {@link Integration}
	 * @return Built email service
	 */
	public Optional<EmailService> getEmailService(Integration integration) {

		BusinessRule.expect(integration, notNull()).verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Integration should be not null.");

		if (!integration.isEnabled()) {
			return Optional.empty();
		}

		Map<String, Object> config = integration.getParams().getParams();

		if (MapUtils.isNotEmpty(config)) {

			boolean authRequired = ofNullable(config.get(EmailSettingsEnum.AUTH_ENABLED.getAttribute())).map(e -> BooleanUtils.toBoolean(
					String.valueOf(e))).orElse(false);

			Properties javaMailProperties = new Properties();
			javaMailProperties.put("mail.smtp.connectiontimeout", DEFAULT_CONNECTION_TIMEOUT);
			javaMailProperties.put("mail.smtp.auth", authRequired);
			javaMailProperties.put("mail.smtp.starttls.enable",
					authRequired
							&& ofNullable(config.get(EmailSettingsEnum.STAR_TLS_ENABLED.getAttribute())).map(e -> BooleanUtils.toBoolean(
							String.valueOf(e))).orElse(false)
			);

			if (ofNullable(config.get(EmailSettingsEnum.SSL_ENABLED.getAttribute())).map(e -> BooleanUtils.toBoolean(String.valueOf(e)))
					.orElse(false)) {
				javaMailProperties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
				javaMailProperties.put("mail.smtp.socketFactory.fallback", "false");
				javaMailProperties.put("mail.smtp.ssl.checkserveridentity", true);
			}

			EmailService service = new EmailService(javaMailProperties);
			service.setTemplateEngine(templateEngine);

			EmailSettingsEnum.RP_HOST.getAttribute(config).ifPresent(service::setRpHost);
			EmailSettingsEnum.HOST.getAttribute(config).ifPresent(service::setHost);
			service.setPort(ofNullable(config.get(EmailSettingsEnum.PORT.getAttribute())).map(p -> NumberUtils.toInt(String.valueOf(p), 25))
					.orElse(25));
			EmailSettingsEnum.PROTOCOL.getAttribute(config).ifPresent(service::setProtocol);
			EmailSettingsEnum.FROM.getAttribute(config).ifPresent(service::setFrom);
			if (authRequired) {
				EmailSettingsEnum.USERNAME.getAttribute(config).ifPresent(service::setUsername);
				EmailSettingsEnum.PASSWORD.getAttribute(config).ifPresent(password -> service.setPassword(encryptor.decrypt(password)));
			}
			return Optional.of(service);

		}

		return Optional.empty();
	}

	/**
	 * Build mail service based on default server configs
	 *
	 * @return Built email service
	 */
	public Optional<EmailService> getDefaultEmailService() {
		return ofNullable(getDefaultEmailService(true)); // NOSONAR
	}

	/**
	 * Build mail service based on default server configs
	 *
	 * @return Built email service
	 */
	public Optional<EmailService> getDefaultEmailService(Integration integration) {

		return getEmailService(integration);

	}

	/**
	 * Build mail service based on default server configs and check connection
	 *
	 * @return Built email service
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public EmailService getEmailService(Integration integration, boolean checkConnection) {

		EmailService emailService = getEmailService(integration).orElseThrow(() -> emailConfigurationFail(null));

		if (checkConnection) {
			checkConnection(emailService);
		}

		return emailService;

	}

	/**
	 * Build mail service based on default server configs and check connection
	 *
	 * @return Built email service
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public EmailService getDefaultEmailService(boolean checkConnection) {

		List<Long> integrationTypeIds = integrationTypeRepository.findAllByIntegrationGroup(IntegrationGroupEnum.NOTIFICATION)
				.stream()
				.map(IntegrationType::getId)
				.collect(Collectors.toList());

		Integration integration = integrationRepository.findAllGlobalInIntegrationTypeIds(integrationTypeIds)
				.stream()
				.filter(Integration::isEnabled)
				.findFirst()
				.orElseThrow(() -> emailConfigurationFail(null));

		EmailService emailService = getEmailService(integration).orElseThrow(() -> emailConfigurationFail(null));

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
