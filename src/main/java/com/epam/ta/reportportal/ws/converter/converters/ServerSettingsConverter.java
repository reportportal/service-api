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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.ServerSettings;
import com.epam.ta.reportportal.entity.ServerSettingsEnum;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.settings.ServerEmailResource;
import com.epam.ta.reportportal.ws.model.settings.ServerSettingsResource;
import com.mchange.lang.IntegerUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.entity.ServerSettingsConstants.EMAIL_CONFIG_PREFIX;
import static java.util.Optional.ofNullable;
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
		expect(serverSettings, CollectionUtils::isNotEmpty).verify(ErrorType.SERVER_SETTINGS_NOT_FOUND, "default");
		Map<String, String> settings = serverSettings.stream()
				.filter(s -> s.getKey().startsWith(EMAIL_CONFIG_PREFIX))
				.collect(toMap(ServerSettings::getKey, entry -> ofNullable(entry.getValue()).orElse(""), (prev, curr) -> prev));
		ServerSettingsResource resource = new ServerSettingsResource();
		resource.setActive(BooleanUtils.toBoolean(settings.get(ServerSettingsEnum.ENABLED.getAttribute())));

		ServerEmailResource output = new ServerEmailResource();
		output.setHost(settings.get(ServerSettingsEnum.HOST.getAttribute()));
		output.setProtocol(settings.get(ServerSettingsEnum.PROTOCOL.getAttribute()));
		ofNullable(settings.get(ServerSettingsEnum.PORT.getAttribute())).ifPresent(attr -> output.setPort(IntegerUtils.parseInt(attr, -1)));
		ofNullable(settings.get(ServerSettingsEnum.AUTH_ENABLED.getAttribute())).ifPresent(attr -> output.setAuthEnabled(BooleanUtils.toBoolean(
				attr)));
		ofNullable(settings.get(ServerSettingsEnum.SSL_ENABLED.getAttribute())).ifPresent(attr -> output.setSslEnabled(BooleanUtils.toBoolean(
				attr)));
		ofNullable(settings.get(ServerSettingsEnum.STAR_TLS_ENABLED.getAttribute())).ifPresent(attr -> output.setStarTlsEnabled(BooleanUtils
				.toBoolean(attr)));
		output.setFrom(settings.get(ServerSettingsEnum.FROM.getAttribute()));
		if (settings.containsKey(ServerSettingsEnum.AUTH_ENABLED.getAttribute())
				&& BooleanUtils.toBoolean(settings.get(ServerSettingsEnum.AUTH_ENABLED.getAttribute()))) {
			output.setUsername(settings.get(ServerSettingsEnum.USERNAME.getAttribute()));
			/* Password field not provided in response */
		}
		resource.setServerEmailResource(output);

		return resource;
	};

}
