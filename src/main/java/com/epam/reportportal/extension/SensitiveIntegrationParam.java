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

package com.epam.reportportal.extension;

import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Canonical names of integration parameters that are encrypted at rest. service-api encrypts values under these keys
 * before persisting. Plugins must decrypt them using BasicTextEncryptor when reading.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SensitiveIntegrationParam {

  public static final String PASSWORD = "password";
  public static final String OAUTH_ACCESS_KEY = "oauthAccessKey";
  public static final String API_TOKEN = "apiToken";
  public static final String ACCESS_TOKEN = "accessToken";
  public static final String MANAGER_PASSWORD = "managerPassword";
  public static final String CLIENT_SECRET = "clientSecret";
  public static final String PASSWORD_ATTRIBUTE = "passwordAttribute";

  public static final Set<String> ALL = Set.of(PASSWORD, OAUTH_ACCESS_KEY, API_TOKEN, ACCESS_TOKEN, MANAGER_PASSWORD,
      CLIENT_SECRET, PASSWORD_ATTRIBUTE);
}
