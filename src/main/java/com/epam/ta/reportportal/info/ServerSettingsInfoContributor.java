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
package com.epam.ta.reportportal.info;

import com.epam.ta.reportportal.dao.ServerSettingsRepository;
import com.epam.ta.reportportal.entity.ServerSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Shows list of supported analytics providers and other server settings.
 *
 * @author Pavel Bortnik
 */

@Component
public class ServerSettingsInfoContributor implements ExtensionContributor {

	private static final String ANALYTICS_KEY = "analytics";
	private static final String INSTANCE_ID_KEY = "instanceId";

	private final ServerSettingsRepository settingsRepository;

	@Autowired
	@SuppressWarnings("SpringJavaAutowiringInspection")
	public ServerSettingsInfoContributor(ServerSettingsRepository settingsRepository) {
		this.settingsRepository = settingsRepository;
	}

	@Override
	public Map<String, ?> contribute() {
		Map<String, Object> info = new HashMap<>();
		Optional<ServerSettings> analyticsDetails = settingsRepository.findByKey("server.analytics.all");
		analyticsDetails.ifPresent(it -> info.put(ANALYTICS_KEY, it));
		return info;

	}
}
