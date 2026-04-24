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

/**
 * Property name prefixes for analytics and instance details in server settings.
 *
 * @author Ivan Budaev
 */
public final class ServerSettingsConstants {

  public static final String ANALYTICS_CONFIG_PREFIX = "server.analytics.";
  public static final String SERVER_DETAILS_CONFIG_PREFIX = "server.details.";

  private ServerSettingsConstants() {
    //static only
  }
}
