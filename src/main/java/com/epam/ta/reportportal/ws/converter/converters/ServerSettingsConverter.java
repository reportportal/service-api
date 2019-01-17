/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.ServerSettings;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.settings.AnalyticsResource;
import com.epam.ta.reportportal.ws.model.settings.ServerSettingsResource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;

import java.util.List;
import java.util.function.Function;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.entity.ServerSettingsConstants.ANALYTICS_CONFIG_PREFIX;
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

		ServerSettingsResource resource = new ServerSettingsResource();

		resource.setAnalyticsResource(serverSettings.stream()
				.filter(s -> ofNullable(s.getKey()).map(k -> k.startsWith(ANALYTICS_CONFIG_PREFIX)).orElse(false))
				.collect(toMap(ServerSettings::getKey, s -> {
					AnalyticsResource analyticsResource = new AnalyticsResource(BooleanUtils.toBoolean(s.getValue()));
					analyticsResource.setType(s.getKey());

					return analyticsResource;
				}, (prev, curr) -> prev)));

		return resource;
	};

}
