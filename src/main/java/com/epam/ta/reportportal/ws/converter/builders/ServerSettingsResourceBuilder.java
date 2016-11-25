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

package com.epam.ta.reportportal.ws.converter.builders;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.database.entity.ServerSettings;
import com.epam.ta.reportportal.ws.model.settings.ServerEmailConfig;
import com.epam.ta.reportportal.ws.model.settings.ServerSettingsResource;

/**
 * Email server settings resource response assembler
 * 
 * @author Andrei_Ramanchuk
 */
@Service
@Scope("prototype")
public class ServerSettingsResourceBuilder extends ResourceBuilder<ServerSettingsResource> {

	public ServerSettingsResourceBuilder addServerSettings(ServerSettings doc) {
		ServerSettingsResource resource = getObject();
		resource.setProfile(doc.getId());
		resource.setActive(doc.getActive());
		ServerEmailConfig output = new ServerEmailConfig();
		output.setHost(doc.getServerEmailConfig().getHost());
		output.setPort(doc.getServerEmailConfig().getPort());
		output.setProtocol(doc.getServerEmailConfig().getProtocol());
		output.setAuthEnabled(doc.getServerEmailConfig().getAuthEnabled());
		output.setSslEnabled(doc.getServerEmailConfig().isSslEnabled());
		output.setStarTlsEnabled(doc.getServerEmailConfig().isStarTlsEnabled());
		if (doc.getServerEmailConfig().getAuthEnabled()) {
			output.setUsername(doc.getServerEmailConfig().getUsername());
			/* Password field not provided in response */
		}
		resource.setServerEmailConfig(output);
		return this;
	}

	@Override
	protected ServerSettingsResource initObject() {
		return new ServerSettingsResource();
	}
}