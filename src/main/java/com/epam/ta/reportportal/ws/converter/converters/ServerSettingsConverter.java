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

import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.entity.ServerSettings;
import com.epam.ta.reportportal.entity.ServerSettingsEnum;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.settings.ServerEmailResource;
import com.epam.ta.reportportal.ws.model.settings.ServerSettingsResource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static java.util.stream.Collectors.toMap;

/**
 * Converts internal DB model to DTO
 *
 * @author Pavel Bortnik
 */
public final class ServerSettingsConverter {

	private ServerSettingsConverter() {
		//static only
	}

	public static final Function<List<ServerSettings>, ServerSettingsResource> TO_RESOURCE = serverSettings -> {
		BusinessRule.expect(CollectionUtils.isNotEmpty(serverSettings), equalTo(true))
				.verify(ErrorType.SERVER_SETTINGS_NOT_FOUND, "Server settings should not be empty");
		Map<ServerSettingsEnum, String> settings = serverSettings.stream()
				.collect(toMap(s -> ServerSettingsEnum.valueOf(s.getKey()), ServerSettings::getValue, (prev, curr) -> prev));
		ServerSettingsResource resource = new ServerSettingsResource();
		//TODO check profile
		//resource.setProfile(settings.getId());
		resource.setActive(BooleanUtils.toBoolean(settings.get(ServerSettingsEnum.ENABLED)));

		ServerEmailResource output = new ServerEmailResource();
		output.setHost(settings.get(ServerSettingsEnum.HOST));
		output.setPort(Integer.valueOf(settings.get(ServerSettingsEnum.PORT)));
		output.setProtocol(settings.get(ServerSettingsEnum.PROTOCOL));
		output.setAuthEnabled(BooleanUtils.toBoolean(settings.get(ServerSettingsEnum.AUTH_ENABLED)));
		output.setSslEnabled(BooleanUtils.toBoolean(settings.get(ServerSettingsEnum.SSL_ENABLED)));
		output.setStarTlsEnabled(BooleanUtils.toBoolean(settings.get(ServerSettingsEnum.START_TLS_ENABLED)));
		output.setFrom(settings.get(ServerSettingsEnum.FROM));
		if (BooleanUtils.toBoolean(settings.get(ServerSettingsEnum.AUTH_ENABLED))) {
			output.setUsername(settings.get(ServerSettingsEnum.USERNAME));
			/* Password field not provided in response */
		}
		resource.setServerEmailResource(output);

		return resource;
	};

}
