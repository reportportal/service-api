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
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;

/**
 * Converts internal DB model to DTO
 *
 * @author Pavel Bortnik
 */
public final class ServerSettingsConverter {

	private ServerSettingsConverter() {
		//static only
	}

	public static final Function<List<ServerSettings>, Map<String, String>> TO_RESOURCE = serverSettings -> {
		expect(serverSettings, CollectionUtils::isNotEmpty).verify(ErrorType.SERVER_SETTINGS_NOT_FOUND, "default");
		return serverSettings.stream().collect(Collectors.toMap(ServerSettings::getKey, ServerSettings::getValue));
	};

}
