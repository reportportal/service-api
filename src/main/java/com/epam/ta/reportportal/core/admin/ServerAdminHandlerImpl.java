/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.core.admin;

import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.dao.ServerSettingsRepository;
import com.epam.ta.reportportal.entity.ServerSettings;
import com.epam.ta.reportportal.entity.ServerSettingsEnum;
import com.epam.ta.reportportal.util.email.EmailService;
import com.epam.ta.reportportal.util.email.MailServiceFactory;
import com.epam.ta.reportportal.ws.converter.converters.ServerSettingsConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.settings.AnalyticsResource;
import com.epam.ta.reportportal.ws.model.settings.ServerEmailResource;
import com.epam.ta.reportportal.ws.model.settings.ServerSettingsResource;
import org.jasypt.util.text.BasicTextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.fail;
import static com.epam.ta.reportportal.entity.ServerSettingsConstants.ANALYTICS_CONFIG_PREFIX;
import static com.epam.ta.reportportal.entity.ServerSettingsConstants.EMAIL_CONFIG_PREFIX;
import static com.epam.ta.reportportal.ws.model.ErrorType.FORBIDDEN_OPERATION;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

/**
 * Basic implementation of server administration interface
 * {@link ServerAdminHandler}
 *
 * @author Andrei_Ramanchuk
 */
@Service
public class ServerAdminHandlerImpl implements ServerAdminHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServerAdminHandlerImpl.class);

	private final BasicTextEncryptor simpleEncryptor;

	private final ServerSettingsRepository serverSettingsRepository;

	private final MailServiceFactory emailServiceFactory;

	@Autowired
	public ServerAdminHandlerImpl(BasicTextEncryptor simpleEncryptor, ServerSettingsRepository serverSettingsRepository,
			MailServiceFactory emailServiceFactory) {
		this.simpleEncryptor = simpleEncryptor;
		this.serverSettingsRepository = serverSettingsRepository;
		this.emailServiceFactory = emailServiceFactory;
	}

	@Override
	public ServerSettingsResource getServerSettings() {
		return ServerSettingsConverter.TO_RESOURCE.apply(findServerSettings());
	}

	@Override
	public OperationCompletionRS saveEmailSettings(ServerEmailResource request) {
		List<ServerSettings> serverSettings = findServerSettings();
		Map<String, ServerSettings> settings = serverSettings.stream().collect(toMap(ServerSettings::getKey, s -> s, (prev, curr) -> prev));
		if (null != request) {
			addOrUpdateServerSettings(settings, ServerSettingsEnum.ENABLED.getAttribute(), String.valueOf(request.isEnabled()));
			if (request.isEnabled()) {
				ofNullable(request.getHost()).ifPresent(host -> addOrUpdateServerSettings(settings,
						ServerSettingsEnum.HOST.getAttribute(),
						host
				));

				int port = Optional.ofNullable(request.getPort()).orElse(25);
				if ((port <= 0) || (port > 65535)) {
					BusinessRule.fail().withError(ErrorType.INCORRECT_REQUEST, "Incorrect 'Port' value. Allowed value is [1..65535]");
				}
				addOrUpdateServerSettings(settings, ServerSettingsEnum.PORT.getAttribute(), String.valueOf(port));

				addOrUpdateServerSettings(settings,
						ServerSettingsEnum.PROTOCOL.getAttribute(),
						ofNullable(request.getProtocol()).orElse("smtp")
				);

				ofNullable(request.getAuthEnabled()).ifPresent(authEnabled -> {
					addOrUpdateServerSettings(settings, ServerSettingsEnum.AUTH_ENABLED.getAttribute(), String.valueOf(authEnabled));
					if (authEnabled) {
						ofNullable(request.getUsername()).ifPresent(username -> addOrUpdateServerSettings(settings,
								ServerSettingsEnum.USERNAME.getAttribute(),
								username
						));
						ofNullable(request.getPassword()).ifPresent(pass -> addOrUpdateServerSettings(settings,
								ServerSettingsEnum.PASSWORD.getAttribute(),
								simpleEncryptor.encrypt(pass)
						));
					} else {
						/* Auto-drop values on switched-off authentication */
						addOrUpdateServerSettings(settings, ServerSettingsEnum.USERNAME.getAttribute(), null);
						addOrUpdateServerSettings(settings, ServerSettingsEnum.PASSWORD.getAttribute(), null);
					}
				});

				addOrUpdateServerSettings(settings,
						ServerSettingsEnum.STAR_TLS_ENABLED.getAttribute(),
						String.valueOf(Boolean.TRUE.equals(request.getStarTlsEnabled()))
				);
				addOrUpdateServerSettings(settings,
						ServerSettingsEnum.SSL_ENABLED.getAttribute(),
						String.valueOf(Boolean.TRUE.equals(request.getSslEnabled()))
				);

				ofNullable(request.getFrom()).ifPresent(from -> addOrUpdateServerSettings(settings,
						ServerSettingsEnum.FROM.getAttribute(),
						from
				));

				Optional<EmailService> emailService = emailServiceFactory.getEmailService(new ArrayList<>(settings.values()));
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

			serverSettingsRepository.saveAll(settings.values());
		}
		return new OperationCompletionRS("Server Settings are successfully updated.");
	}

	public OperationCompletionRS deleteEmailSettings() {

		serverSettingsRepository.deleteAllByTerm(EMAIL_CONFIG_PREFIX);

		return new OperationCompletionRS("Server Settings are successfully removed.");
	}

	@Override
	public OperationCompletionRS saveAnalyticsSettings(AnalyticsResource analyticsResource) {
		String analyticsType = analyticsResource.getType();
		List<ServerSettings> serverSettings = findServerSettings();
		Map<String, ServerSettings> settings = serverSettings.stream().collect(toMap(ServerSettings::getKey, s -> s, (prev, curr) -> prev));
		Map<String, ServerSettings> serverAnalyticsDetails = settings.entrySet()
				.stream()
				.filter(entry -> entry.getKey().startsWith(ANALYTICS_CONFIG_PREFIX))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		ServerSettings analyticsDetails = ofNullable(serverAnalyticsDetails.get(analyticsType)).orElseGet(ServerSettings::new);
		analyticsDetails.setKey(analyticsType);
		analyticsDetails.setValue(String.valueOf((ofNullable(analyticsResource.getEnabled()).orElse(false))));

		serverSettingsRepository.save(analyticsDetails);
		return new OperationCompletionRS("Server Settings were successfully updated.");
	}

	private List<ServerSettings> findServerSettings() {
		return serverSettingsRepository.findAll();
	}

	private void addOrUpdateServerSettings(Map<String, ServerSettings> serverSettings, String settingsName, String value) {

		serverSettings.put(settingsName, createServerSettings(settingsName, value));
	}

	private ServerSettings createServerSettings(String settingsName, String value) {
		ServerSettings serverSettings = new ServerSettings();
		serverSettings.setKey(settingsName);
		serverSettings.setValue(value);

		return serverSettings;
	}
}