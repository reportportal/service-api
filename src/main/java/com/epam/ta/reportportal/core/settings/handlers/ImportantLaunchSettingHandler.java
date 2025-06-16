/*
 * Copyright 2025 EPAM Systems
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
package com.epam.ta.reportportal.core.settings.handlers;


import static com.epam.ta.reportportal.core.launch.attribute.AttributeHandler.RETENTION_POLICY_KEY;

import com.epam.ta.reportportal.core.settings.ServerSettingHandler;
import com.epam.ta.reportportal.dao.ItemAttributeRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.entity.enums.RetentionPolicyEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Handler for the "important launch" server setting.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImportantLaunchSettingHandler implements ServerSettingHandler {

  public static final String IMPORTANT_SETTINGS_KEY = "server.features.important.enabled";

  private final LaunchRepository launchRepository;

  private final ItemAttributeRepository itemAttributeRepository;

  @Override
  public void handle(String value) {
    if (Boolean.FALSE.equals(Boolean.parseBoolean(value))) {
      var updatedCount = launchRepository.updateLaunchesRetentionPolicy(
          RetentionPolicyEnum.REGULAR);
      itemAttributeRepository.deleteAllByKeyAndSystem(RETENTION_POLICY_KEY, true);
      log.info("Retention policy for {} launches changed to 'REGULAR'", updatedCount);
    }
  }

  @Override
  public String getKey() {
    return IMPORTANT_SETTINGS_KEY;
  }
}
