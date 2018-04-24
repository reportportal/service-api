package com.epam.ta.reportportal.store.service;

import com.epam.ta.reportportal.store.database.dao.ServerSettingRepository;
import com.epam.ta.reportportal.store.database.entity.ServerSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ServerSettingsService {

	private final ServerSettingRepository serverSettingRepository;

	@Autowired
	public ServerSettingsService(ServerSettingRepository serverSettingRepository) {
		this.serverSettingRepository = serverSettingRepository;
	}

	public Map<String, String> findAllSettings() {
		return serverSettingRepository.streamAll().collect(Collectors.toMap(ServerSettings::getKey, ServerSettings::getValue));
	}

	public void save(Map<String, String> settings) {
		settings.entrySet()
				.stream()
				.map(entry -> new ServerSettings(entry.getKey(), entry.getValue()))
				.collect(Collectors.toList())
				.forEach(ss -> {
					Optional<ServerSettings> prop = serverSettingRepository.findByKey(ss.getKey());
					if (prop.isPresent()) {
						ServerSettings setting = prop.get();
						setting.setValue(ss.getValue());
						serverSettingRepository.save(setting);
					} else {
						serverSettingRepository.save(new ServerSettings(ss.getKey(), ss.getValue()));
					}
				});

	}
}
