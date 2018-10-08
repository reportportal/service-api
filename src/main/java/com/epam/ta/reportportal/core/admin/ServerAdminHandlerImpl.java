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

package com.epam.ta.reportportal.core.admin;

import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.dao.ServerSettingsRepository;
import com.epam.ta.reportportal.database.entity.settings.AnalyticsDetails;
import com.epam.ta.reportportal.entity.ServerSettings;
import com.epam.ta.reportportal.entity.ServerSettingsEnum;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.util.email.MailServiceFactory;
import com.epam.ta.reportportal.ws.converter.converters.ServerSettingsConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.settings.AnalyticsResource;
import com.epam.ta.reportportal.ws.model.settings.ServerEmailResource;
import com.epam.ta.reportportal.ws.model.settings.ServerSettingsResource;
import com.mongodb.WriteResult;
import org.jasypt.util.text.BasicTextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.util.*;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.fail;
import static com.epam.ta.reportportal.ws.model.ErrorType.FORBIDDEN_OPERATION;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * Basic implementation of server administration interface
 * {@link ServerAdminHandler}
 *
 * @author Andrei_Ramanchuk
 */
@Service
public class ServerAdminHandlerImpl implements ServerAdminHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServerAdminHandlerImpl.class);

	@Autowired
	private BasicTextEncryptor simpleEncryptor;

	@Autowired
	private ServerSettingsRepository serverSettingsRepository;

	@Autowired
	private MailServiceFactory emailServiceFactory;

	@Override
	public ServerSettingsResource getServerSettings() {
		return ServerSettingsConverter.TO_RESOURCE.apply(findServerSettings());
	}

	@Override
	public OperationCompletionRS saveEmailSettings(ServerEmailResource request) {
		List<ServerSettings> serverSettings = findServerSettings();
		Map<ServerSettingsEnum, ServerSettings> settings = serverSettings.stream()
				.collect(toMap(s -> ServerSettingsEnum.valueOf(s.getKey()), s -> s, (prev, curr) -> prev));
		if (null != request) {
			addOrUpdateServerSettings(settings, ServerSettingsEnum.ENABLED, String.valueOf(request.isEnabled()));
			if (request.isEnabled()) {
				ofNullable(request.getHost()).ifPresent(host -> addOrUpdateServerSettings(settings, ServerSettingsEnum.HOST, host));

				int port = Optional.ofNullable(request.getPort()).orElse(25);
				if ((port <= 0) || (port > 65535)) {
					BusinessRule.fail().withError(ErrorType.INCORRECT_REQUEST, "Incorrect 'Port' value. Allowed value is [1..65535]");
				}
				addOrUpdateServerSettings(settings, ServerSettingsEnum.PORT, String.valueOf(port));

				addOrUpdateServerSettings(settings, ServerSettingsEnum.PROTOCOL, ofNullable(request.getProtocol()).orElse("smtp"));

				ofNullable(request.getAuthEnabled()).ifPresent(authEnabled -> {
					addOrUpdateServerSettings(settings, ServerSettingsEnum.AUTH_ENABLED, String.valueOf(authEnabled));
					if (authEnabled) {
						ofNullable(request.getUsername()).ifPresent(username -> addOrUpdateServerSettings(settings,
								ServerSettingsEnum.USERNAME,
								username
						));
						ofNullable(request.getPassword()).ifPresent(pass -> addOrUpdateServerSettings(settings,
								ServerSettingsEnum.PASSWORD,
								pass
						));
					} else {
						/* Auto-drop values on switched-off authentication */
						serverEmailConfig.setUsername(null);
						serverEmailConfig.setPassword(null);
					}
				});

				addOrUpdateServerSettings(settings,
						ServerSettingsEnum.START_TLS_ENABLED,
						String.valueOf(Boolean.TRUE.equals(request.getStarTlsEnabled()))
				);
				addOrUpdateServerSettings(settings,
						ServerSettingsEnum.SSL_ENABLED,
						String.valueOf(Boolean.TRUE.equals(request.getSslEnabled()))
				);

				ofNullable(request.getFrom()).ifPresent(from -> addOrUpdateServerSettings(settings, ServerSettingsEnum.FROM, from));

				try {
					emailServiceFactory.getEmailService(settings).get().testConnection();
				} catch (MessagingException ex) {
					LOGGER.error("Cannot send email to user", ex);
					fail().withError(FORBIDDEN_OPERATION,
							"Email configuration is incorrect. Please, check your configuration. " + ex.getMessage()
					);
				}

			}

			serverSettingsRepository.saveAll(settings.values());
		}
		return new OperationCompletionRS("Server Settings with are successfully updated.");
	}

	public OperationCompletionRS deleteEmailSettings() {
		WriteResult result = mongoOperations.updateFirst(query(Criteria.where("_id").is(profileId)),
				Update.update("serverEmailDetails", null),
				ServerSettings.class
		);
		BusinessRule.expect(result.getN(), not(equalTo(0))).verify(ErrorType.SERVER_SETTINGS_NOT_FOUND, profileId);

		return new OperationCompletionRS("Server Settings with profile '" + profileId + "' is successfully updated.");
	}

	@Override
	public OperationCompletionRS saveAnalyticsSettings(AnalyticsResource analyticsResource) {
		String analyticsType = analyticsResource.getType();
		List<ServerSettings> serverSettings = findServerSettings();
		Map<String, AnalyticsDetails> serverAnalyticsDetails = ofNullable(settings.getAnalyticsDetails()).orElse(Collections.emptyMap());

		AnalyticsDetails analyticsDetails = ofNullable(serverAnalyticsDetails.get(analyticsType)).orElse(new AnalyticsDetails());
		analyticsDetails.setEnabled(ofNullable(analyticsResource.getEnabled()).orElse(false));
		serverAnalyticsDetails.put(analyticsType, analyticsDetails);
		settings.setAnalyticsDetails(serverAnalyticsDetails);

		repository.save(settings);
		return new OperationCompletionRS("Server Settings with profile '" + profileId + "' is successfully updated.");
	}

	private List<ServerSettings> findServerSettings() {
		return serverSettingsRepository.findAll();
	}

	private void addOrUpdateServerSettings(Map<ServerSettingsEnum, ServerSettings> serverSettings, ServerSettingsEnum serverSettingsEnum,
			String value) {
		if (serverSettings.containsKey(serverSettingsEnum)) {
			serverSettings.get(serverSettingsEnum).setValue(value);
		} else {
			serverSettings.put(serverSettingsEnum, createServerSettings(serverSettingsEnum, value));
		}
	}

	private ServerSettings createServerSettings(ServerSettingsEnum settingsEnum, String value) {
		ServerSettings serverSettings = new ServerSettings();
		serverSettings.setKey(settingsEnum.getAttribute());
		serverSettings.setValue(value);

		return serverSettings;
	}
}