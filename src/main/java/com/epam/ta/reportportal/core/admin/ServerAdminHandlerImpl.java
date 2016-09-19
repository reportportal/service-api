/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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

import static com.epam.ta.reportportal.ws.model.ErrorType.SERVER_SETTINGS_NOT_FOUND;

import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.database.dao.ServerSettingsRepository;
import com.epam.ta.reportportal.database.entity.ServerSettings;
import com.epam.ta.reportportal.util.LazyReference;
import com.epam.ta.reportportal.ws.converter.ServerSettingsResourceAssembler;
import com.epam.ta.reportportal.ws.converter.builders.ServerSettingsBuilder;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.settings.ServerSettingsResource;
import com.epam.ta.reportportal.ws.model.settings.UpdateEmailSettingsRQ;

/**
 * Basic implementation of server administration interface
 * {@link ServerAdminHandler}
 * 
 * @author Andrei_Ramanchuk
 */
@Service
public class ServerAdminHandlerImpl implements ServerAdminHandler {

	@Autowired
	private BasicTextEncryptor simpleEncryptor;

	@Autowired
	private ServerSettingsRepository repository;

	@Autowired
	@Qualifier("serverSettingsBuilder.reference")
	private LazyReference<ServerSettingsBuilder> settingsBuilder;

	@Autowired
	private ServerSettingsResourceAssembler settingsAssembler;

	@Override
	public ServerSettingsResource getServerSettings(String profileId) {
		ServerSettings settings = repository.findOne(profileId);
		BusinessRule.expect(settings, Predicates.notNull()).verify(SERVER_SETTINGS_NOT_FOUND, profileId);
		return settingsAssembler.toResource(settings);
	}

	@Override
	public OperationCompletionRS setServerSettings(String profileId, UpdateEmailSettingsRQ request) {
		ServerSettings settings = repository.findOne(profileId);
		BusinessRule.expect(settings, Predicates.notNull()).verify(ErrorType.SERVER_SETTINGS_NOT_FOUND, profileId);
		if (null != request) {
			if (request.getDebug())
				settings.getServerEmailConfig().setDebug(request.getDebug());
			if (null != request.getHost())
				settings.getServerEmailConfig().setHost(request.getHost());
			if (null != request.getPort()) {
				try {
					int port = Integer.parseInt(request.getPort());
					if ((port <= 0) || (port > 65535))
						BusinessRule.fail().withError(ErrorType.INCORRECT_REQUEST, "Incorrect 'Port' value. Allowed value is [1..65535]");
					settings.getServerEmailConfig().setPort(port);
				} catch (NumberFormatException e) {
					BusinessRule.fail().withError(ErrorType.INCORRECT_REQUEST, "Incorrect 'Port' value. Allowed value is [1..65535]");
				}
			}
			if (null != request.getProtocol())
				settings.getServerEmailConfig().setProtocol(request.getProtocol());
			if (request.getAuthEnabled()) {
				settings.getServerEmailConfig().setAuthEnabled(request.getAuthEnabled());
				if (null != request.getUsername())
					settings.getServerEmailConfig().setUsername(request.getUsername());
				if (null != request.getPassword())
					settings.getServerEmailConfig().setPassword(simpleEncryptor.encrypt(request.getPassword()));
			} else {
				settings.getServerEmailConfig().setAuthEnabled(false);
				/* Auto-drop values on switched-off authentication */
				settings.getServerEmailConfig().setUsername(null);
				settings.getServerEmailConfig().setPassword(null);
			}
		}
		repository.save(settings);
		return new OperationCompletionRS("Server Settings with profile '" + profileId + "' is successfully updated.");
	}
}