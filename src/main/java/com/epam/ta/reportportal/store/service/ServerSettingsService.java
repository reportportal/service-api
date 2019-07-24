/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.store.service;

import com.epam.ta.reportportal.dao.ServerSettingsRepository;
import com.epam.ta.reportportal.entity.ServerSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Service
public class ServerSettingsService {

	private final ServerSettingsRepository serverSettingsRepository;

	@Autowired
	public ServerSettingsService(ServerSettingsRepository serverSettingsRepository) {
		this.serverSettingsRepository = serverSettingsRepository;
	}

	public Map<String, String> findAllSettings() {
		return serverSettingsRepository.streamAll()
				.collect(Collectors.toMap(ServerSettings::getKey, s -> ofNullable(s.getValue()).orElse("")));
	}

	public void save(Map<String, String> settings) {
		settings.entrySet()
				.stream()
				.map(entry -> new ServerSettings(entry.getKey(), entry.getValue()))
				.collect(Collectors.toList())
				.forEach(ss -> {
					Optional<ServerSettings> prop = serverSettingsRepository.findByKey(ss.getKey());
					if (prop.isPresent()) {
						ServerSettings setting = prop.get();
						setting.setValue(ss.getValue());
						serverSettingsRepository.save(setting);
					} else {
						serverSettingsRepository.save(new ServerSettings(ss.getKey(), ss.getValue()));
					}
				});

	}

	public void save(Object o) {

	}
}
