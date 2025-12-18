/*
 * Copyright 2021 EPAM Systems
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

package com.epam.reportportal.extension.util;

import java.util.Optional;
import java.util.jar.Manifest;

public final class PluginManifestUtils {

  public static String PLUGIN_ID_PROPERTY = "Plugin-Id";

  private PluginManifestUtils() {
  }

  public static String readPluginIdFromManifest(Class<?> clazz, String defaultValue) {
    try {
      return Optional.ofNullable(new Manifest(clazz.getResourceAsStream("/META-INF/MANIFEST.MF"))
              .getMainAttributes()
              .getValue(PLUGIN_ID_PROPERTY))
          .orElse(defaultValue);
    } catch (Exception e) {
      return defaultValue;
    }
  }
}
