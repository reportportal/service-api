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

package com.epam.ta.reportportal.ws.converter.builders;

import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.database.entity.ServerSettings;
import com.epam.ta.reportportal.ws.model.settings.CreateServerSettingsRQ;
import com.epam.ta.reportportal.ws.model.settings.ServerEmailConfig;

/**
 * 
 * @author Andrei_Ramanchuk
 *
 */
@Service
@Scope("prototype")
public class ServerSettingsBuilder extends Builder<ServerSettings> {

	@Autowired
	private BasicTextEncryptor simpleEncryptor;

	public ServerSettingsBuilder addServerSettingsRQ(CreateServerSettingsRQ request) {
		if (request != null) {
			getObject().setId(request.getProfile().toLowerCase());
			getObject().setActive(request.getActive());
			ServerEmailConfig config = new ServerEmailConfig();
			config.setHost(request.getServerEmailConfig().getHost());
			config.setPort(request.getServerEmailConfig().getPort());
			config.setProtocol(request.getServerEmailConfig().getProtocol());
			config.setAuthEnabled(request.getServerEmailConfig().getAuthEnabled());
			if (request.getServerEmailConfig().getAuthEnabled()) {
				config.setUsername(request.getServerEmailConfig().getUsername());
				config.setPassword(simpleEncryptor.encrypt(request.getServerEmailConfig().getPassword()));
				config.setStarTlsEnabled(request.getServerEmailConfig().isStarTlsEnabled());
			}
			getObject().setServerEmailConfig(config);
		}
		return this;
	}

	@Override
	protected ServerSettings initObject() {
		return new ServerSettings();
	}
}