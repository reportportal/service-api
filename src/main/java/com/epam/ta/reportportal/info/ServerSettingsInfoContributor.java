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
package com.epam.ta.reportportal.info;

import com.epam.ta.reportportal.dao.ServerSettingsRepository;
import com.epam.ta.reportportal.entity.ServerSettings;
import com.epam.ta.reportportal.ws.converter.converters.ServerSettingsConverter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Shows list of supported analytics providers and other server settings.
 *
 * @author Pavel Bortnik
 */

@Component
public class ServerSettingsInfoContributor implements ExtensionContributor {

  private final ServerSettingsRepository settingsRepository;

  @Autowired
  @SuppressWarnings("SpringJavaAutowiringInspection")
  public ServerSettingsInfoContributor(ServerSettingsRepository settingsRepository) {
    this.settingsRepository = settingsRepository;
  }

  @Override
  public Map<String, ?> contribute() {
    Map<String, Object> info = new HashMap<>();
    List<ServerSettings> all = settingsRepository.selectServerSettings();
    Map<String, String> result = ServerSettingsConverter.TO_RESOURCES.apply(all);
    info.put("result", result);
    return info;

  }
}
