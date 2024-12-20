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

package com.epam.ta.reportportal.ws.converter.converters;

import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.ta.reportportal.entity.ServerSettings;
import com.epam.ta.reportportal.model.settings.ServerSettingsResource;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;

/**
 * Converts internal DB model to DTO
 *
 * @author Pavel Bortnik
 */
public final class ServerSettingsConverter {

  public static final Function<ServerSettings, ServerSettingsResource> TO_RESOURCE = serverSettings -> new ServerSettingsResource(
      serverSettings.getKey(), serverSettings.getValue());

  public static final Function<List<ServerSettings>, Map<String, String>> TO_RESOURCES = serverSettings -> {
    expect(serverSettings, CollectionUtils::isNotEmpty).verify(ErrorType.SERVER_SETTINGS_NOT_FOUND,
        "default");
    return serverSettings.stream()
        .collect(Collectors.toMap(ServerSettings::getKey, ServerSettings::getValue));
  };

  private ServerSettingsConverter() {
    //static only
  }

}
