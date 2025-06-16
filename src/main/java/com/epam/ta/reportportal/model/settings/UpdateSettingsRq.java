/*
 * Copyright 2019 EPAM Systems
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
package com.epam.ta.reportportal.model.settings;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request object for updating server settings.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Data
public class UpdateSettingsRq {

  @Schema(description = """
      Key of the setting to update.

      The following keys are allowed:
      - `server.analytics.all`- Enables or disables analytics across the server.
      - `server.details.instance` - Provides details about the server instance.
      - `server.users.sso` - Enables Single Sign-On (SSO) only access for users.
      - `server.session.expiration` - Sets the expiration time for user sessions.
      - `server.footer.links` - Configures links in the UI footer.
      - `server.features.important.enabled` - Enables or disables creation personal organization for new users.
      - `server.features.personal-organization.enabled` - Enables or disables personal organization features.
      - `secret.key` - Manages the secret key used for various security purposes.
      """)
  @NotNull
  private SettingsKey key;
  @NotEmpty
  private String value;

  public enum SettingsKey {
    SERVER_ANALYTICS_ALL("server.analytics.all"),
    SERVER_DETAILS_INSTANCE("server.details.instance"),
    SERVER_USERS_SSO("server.users.sso"),
    SERVER_SESSION_EXPIRATION("server.session.expiration"),
    SERVER_FOOTER_LINKS("server.footer.links"),
    SERVER_FEATURES_PERSONAL_ORGANIZATION_ENABLED("server.features.personal-organization.enabled"),
    SECRET_KEY("secret.key");

    private final String name;

    SettingsKey(String name) {
      this.name = name;
    }

    @JsonCreator
    public static SettingsKey fromValue(String value) {
      for (SettingsKey key : values()) {
        if (key.name.equals(value)) {
          return key;
        }
      }
      throw new IllegalArgumentException("Unknown key: " + value);
    }

    @JsonValue
    public String getName() {
      return name;
    }
  }
}
