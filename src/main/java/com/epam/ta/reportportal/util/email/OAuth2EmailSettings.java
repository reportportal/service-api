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

import java.util.Map;
import java.util.Optional;

/**
 * Additional email server integration parameter keys for OAuth2 authorization, kept outside
 * {@code EmailSettingsEnum} because that enum lives in the external {@code commons-dao}
 * dependency and can't be extended from this repository.
 */
public enum OAuth2EmailSettings {

  AUTH_MODE("authMode"),
  TENANT_ID("tenantId"),
  CLIENT_ID("clientId"),
  CLIENT_SECRET("clientSecret");

  private final String attribute;

  OAuth2EmailSettings(String attribute) {
    this.attribute = attribute;
  }

  public String getAttribute() {
    return attribute;
  }

  public Optional<String> getAttribute(Map<String, Object> params) {
    return Optional.ofNullable(params.get(attribute)).map(String::valueOf);
  }

}
