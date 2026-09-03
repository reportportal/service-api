/*
 * Copyright 2026 EPAM Systems
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

package com.epam.ta.reportportal.util.email;

import com.epam.ta.reportportal.entity.EmailSettingsEnum;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

/**
 * Authorization mode for the email server integration ({@link OAuth2EmailSettings#AUTH_MODE}).
 *
 * <p>{@code EmailSettingsEnum} itself lives in the external {@code commons-dao} dependency and
 * can't be extended with new constants here, so the new OAuth2 fields are plain string keys
 * ({@link OAuth2EmailSettings}) instead of enum values. For backward compatibility with
 * integrations persisted before this existed, {@link #resolve(Map)} falls back to the legacy
 * {@link EmailSettingsEnum#AUTH_ENABLED} boolean flag when {@code authMode} is absent:
 * {@code authEnabled=true} resolves to {@link #BASIC}, anything else resolves to {@link #OFF}.
 */
public enum EmailAuthMode {

  OFF,
  BASIC,
  OAUTH2;

  public static Optional<EmailAuthMode> findByName(String name) {
    return Optional.ofNullable(name)
        .flatMap(n -> Arrays.stream(values()).filter(it -> it.name().equalsIgnoreCase(n)).findAny());
  }

  public static EmailAuthMode resolve(Map<String, Object> params) {
    return OAuth2EmailSettings.AUTH_MODE.getAttribute(params)
        .flatMap(EmailAuthMode::findByName)
        .orElseGet(() -> EmailSettingsEnum.AUTH_ENABLED.getAttribute(params)
            .map(Boolean::parseBoolean)
            .filter(Boolean::booleanValue)
            .map(legacy -> BASIC)
            .orElse(OFF));
  }

}
