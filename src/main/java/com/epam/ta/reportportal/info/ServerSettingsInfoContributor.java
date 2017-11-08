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
package com.epam.ta.reportportal.info;

import com.epam.ta.reportportal.database.dao.ServerSettingsRepository;
import com.epam.ta.reportportal.database.entity.settings.ServerSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

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

		Optional<ServerSettings> serverSettings = ofNullable(settingsRepository.findOne("default"));

		serverSettings.flatMap(settings -> ofNullable(settings.getAnalyticsDetails())).ifPresent(it -> info.put(ANALYTICS_KEY, it));

		serverSettings.flatMap(settings -> ofNullable(settings.getInstanceId()))
				.ifPresent(instanceId -> info.put(INSTANCE_ID_KEY, instanceId));

		return info;

	}
}
