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

package com.epam.reportportal.base.infrastructure.persistence.entity;

import static com.epam.reportportal.base.infrastructure.persistence.entity.ServerSettingsConstants.ANALYTICS_CONFIG_PREFIX;
import static com.epam.reportportal.base.infrastructure.persistence.entity.ServerSettingsConstants.SERVER_DETAILS_CONFIG_PREFIX;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

/**
 * Keys in the global {@code server_settings} table (registration, base URL, analytics, etc.).
 *
 * @author Ivan Budaev
 */
public enum ServerSettingsEnum {

  ANALYTICS(ANALYTICS_CONFIG_PREFIX + "all"),
  INSTANCE(SERVER_DETAILS_CONFIG_PREFIX + "instance");

  private String attribute;

  ServerSettingsEnum(String attribute) {
    this.attribute = attribute;
  }

  public static Optional<ServerSettingsEnum> findByAttribute(String attribute) {
    return Optional.ofNullable(attribute)
        .flatMap(attr -> Arrays.stream(values()).filter(it -> it.attribute.equalsIgnoreCase(attr))
            .findAny());
  }

  public static boolean isPresent(String attribute) {
    return findByAttribute(attribute).isPresent();
  }

  public String getAttribute() {
    return attribute;
  }

  public Optional<String> getAttribute(Map<String, Object> params) {
    return Optional.ofNullable(params.get(this.attribute)).map(o -> (String) o);
  }
}
