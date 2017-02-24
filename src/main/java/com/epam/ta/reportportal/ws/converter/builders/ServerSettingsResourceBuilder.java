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

import com.epam.ta.reportportal.database.entity.settings.ServerSettings;
import com.epam.ta.reportportal.ws.model.settings.ServerEmailResource;
import com.epam.ta.reportportal.ws.model.settings.ServerSettingsResource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Email server settings resource response assembler
 *
 * @author Andrei_Ramanchuk
 */
@Service
@Scope("prototype")
public class ServerSettingsResourceBuilder extends Builder<ServerSettingsResource> {

	public ServerSettingsResourceBuilder addServerSettings(ServerSettings doc) {
		ServerSettingsResource resource = getObject();
		resource.setProfile(doc.getId());
		resource.setActive(doc.getActive());
		if (null != doc.getServerEmailDetails()) {
			ServerEmailResource output = new ServerEmailResource();
			output.setHost(doc.getServerEmailDetails().getHost());
			output.setPort(doc.getServerEmailDetails().getPort());
			output.setProtocol(doc.getServerEmailDetails().getProtocol());
			output.setAuthEnabled(doc.getServerEmailDetails().getAuthEnabled());
			output.setSslEnabled(doc.getServerEmailDetails().isSslEnabled());
			output.setStarTlsEnabled(doc.getServerEmailDetails().isStarTlsEnabled());
			output.setFrom(doc.getServerEmailDetails().getFrom());
			if (doc.getServerEmailDetails().getAuthEnabled()) {
				output.setUsername(doc.getServerEmailDetails().getUsername());
			/* Password field not provided in response */
			}
			resource.setServerEmailResource(output);
		}

		return this;
	}

	@Override
	protected ServerSettingsResource initObject() {
		return new ServerSettingsResource();
	}
}
