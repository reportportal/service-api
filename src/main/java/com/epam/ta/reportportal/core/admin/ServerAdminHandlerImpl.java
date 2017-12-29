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

import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.database.dao.ServerSettingsRepository;
import com.epam.ta.reportportal.database.entity.settings.AnalyticsDetails;
import com.epam.ta.reportportal.database.entity.settings.ServerEmailDetails;
import com.epam.ta.reportportal.database.entity.settings.ServerSettings;
import com.epam.ta.reportportal.util.email.MailServiceFactory;
import com.epam.ta.reportportal.ws.converter.ServerSettingsResourceAssembler;
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
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.fail;
import static com.epam.ta.reportportal.ws.model.ErrorType.FORBIDDEN_OPERATION;
import static java.util.Optional.ofNullable;
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
	private ServerSettingsRepository repository;

	@Autowired
	private ServerSettingsResourceAssembler settingsAssembler;

	@Autowired
	private MailServiceFactory emailServiceFactory;

	@Autowired
	private MongoOperations mongoOperations;

	@Override
	public ServerSettingsResource getServerSettings(String profileId) {
		return settingsAssembler.toResource(findServerSettings(profileId));
	}

	@Override
	public OperationCompletionRS saveEmailSettings(String profileId, ServerEmailResource request) {
		ServerSettings settings = findServerSettings(profileId);
		if (null != request) {
			ServerEmailDetails serverEmailConfig = new ServerEmailDetails();
			serverEmailConfig.setEnabled(request.isEnabled());
			if (request.isEnabled()) {
				ofNullable(request.getHost()).ifPresent(serverEmailConfig::setHost);

				int port = Optional.ofNullable(request.getPort()).orElse(25);
				if ((port <= 0) || (port > 65535)) {
					BusinessRule.fail().withError(ErrorType.INCORRECT_REQUEST, "Incorrect 'Port' value. Allowed value is [1..65535]");
				}
				serverEmailConfig.setPort(port);

				serverEmailConfig.setProtocol(ofNullable(request.getProtocol()).orElse("smtp"));

				ofNullable(request.getAuthEnabled()).ifPresent(authEnabled -> {
					serverEmailConfig.setAuthEnabled(authEnabled);
					if (authEnabled) {
						ofNullable(request.getUsername()).ifPresent(serverEmailConfig::setUsername);
						ofNullable(request.getPassword()).ifPresent(pass -> serverEmailConfig.setPassword(simpleEncryptor.encrypt(pass)));
					} else {
					/* Auto-drop values on switched-off authentication */
						serverEmailConfig.setUsername(null);
						serverEmailConfig.setPassword(null);
					}
				});

				serverEmailConfig.setStarTlsEnabled(Boolean.TRUE.equals(request.getStarTlsEnabled()));
				serverEmailConfig.setSslEnabled(Boolean.TRUE.equals(request.getSslEnabled()));

				ofNullable(request.getFrom()).ifPresent(serverEmailConfig::setFrom);

				try {
					emailServiceFactory.getEmailService(serverEmailConfig).get().testConnection();
				} catch (MessagingException ex) {
					LOGGER.error("Cannot send email to user", ex);
					fail().withError(FORBIDDEN_OPERATION,
							"Email configuration is incorrect. Please, check your configuration. " + ex.getMessage()
					);
				}

			}

			ServerSettings update = new ServerSettings();
			update.setId(settings.getId());
			//TODO active primitive should be replaced with Object(Boolean) type
			update.setActive(settings.getActive());
			update.setServerEmailDetails(serverEmailConfig);

			repository.partialUpdate(update);
		}
		return new OperationCompletionRS("Server Settings with profile '" + profileId + "' is successfully updated.");
	}

	public OperationCompletionRS deleteEmailSettings(String profileId) {
		WriteResult result = mongoOperations.updateFirst(
				query(Criteria.where("_id").is(profileId)), Update.update("serverEmailDetails", null), ServerSettings.class);
		BusinessRule.expect(result.getN(), not(equalTo(0))).verify(ErrorType.SERVER_SETTINGS_NOT_FOUND, profileId);

		return new OperationCompletionRS("Server Settings with profile '" + profileId + "' is successfully updated.");
	}

	@Override
	public OperationCompletionRS saveAnalyticsSettings(String profileId, AnalyticsResource analyticsResource) {
		String analyticsType = analyticsResource.getType();
		ServerSettings settings = findServerSettings(profileId);
		Map<String, AnalyticsDetails> serverAnalyticsDetails = ofNullable(settings.getAnalyticsDetails()).orElse(Collections.emptyMap());

		AnalyticsDetails analyticsDetails = ofNullable(serverAnalyticsDetails.get(analyticsType)).orElse(new AnalyticsDetails());
		analyticsDetails.setEnabled(ofNullable(analyticsResource.getEnabled()).orElse(false));
		serverAnalyticsDetails.put(analyticsType, analyticsDetails);
		settings.setAnalyticsDetails(serverAnalyticsDetails);

		repository.save(settings);
		return new OperationCompletionRS("Server Settings with profile '" + profileId + "' is successfully updated.");
	}

	private ServerSettings findServerSettings(String profileId) {
		ServerSettings settings = repository.findOne(profileId);
		BusinessRule.expect(settings, Predicates.notNull()).verify(ErrorType.SERVER_SETTINGS_NOT_FOUND, profileId);
		return settings;
	}
}