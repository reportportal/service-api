/*
 * Copyright 2017 EPAM Systems
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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.settings.ServerEmailDetails;
import com.epam.ta.reportportal.database.entity.settings.ServerSettings;
import com.epam.ta.reportportal.ws.model.settings.ServerEmailResource;
import com.epam.ta.reportportal.ws.model.settings.ServerSettingsResource;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.BooleanUtils;

import java.util.function.Function;

/**
 * Converts internal DB model to DTO
 *
 * @author Pavel Bortnik
 */
public final class ServerSettingsConverter {

	private ServerSettingsConverter() {
		//static only
	}

	public static final Function<ServerSettings, ServerSettingsResource> TO_RESOURCE = settings -> {
		Preconditions.checkNotNull(settings);
		ServerSettingsResource resource = new ServerSettingsResource();
		resource.setProfile(settings.getId());
		resource.setActive(settings.getActive());
		ServerEmailDetails serverEmailDetails = settings.getServerEmailDetails();
		if (null != serverEmailDetails) {
			ServerEmailResource output = new ServerEmailResource();
			output.setHost(serverEmailDetails.getHost());
			output.setPort(serverEmailDetails.getPort());
			output.setProtocol(serverEmailDetails.getProtocol());
			output.setAuthEnabled(serverEmailDetails.getAuthEnabled());
			output.setSslEnabled(BooleanUtils.isTrue(serverEmailDetails.getSslEnabled()));
			output.setStarTlsEnabled(BooleanUtils.isTrue(serverEmailDetails.getStarTlsEnabled()));
			output.setFrom(serverEmailDetails.getFrom());
			if (serverEmailDetails.getAuthEnabled()) {
				output.setUsername(serverEmailDetails.getUsername());
				/* Password field not provided in response */
			}
			resource.setServerEmailResource(output);
		}
		return resource;
	};

}
