/*
 * Copyright 2023 EPAM Systems
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

package com.epam.ta.reportportal.core.integration.plugin.info;

import com.epam.ta.reportportal.core.plugin.PluginInfo;
import java.nio.file.Path;

/**
 * Resolver that provides info about plugin.
 *
 * @author <a href="mailto:budaevqwerty@gmail.com">Ivan Budayeu</a>
 */
public interface PluginInfoResolver {

  /**
   * Resolve plugin info by path.
   *
   * @param pluginPath {@link Path} to the plugin file
   * @return {@link PluginInfo} that contains plugin properties
   */
  PluginInfo resolveInfo(Path pluginPath);
}