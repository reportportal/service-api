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

package com.epam.ta.reportportal.core.integration.plugin.file;

import com.epam.ta.reportportal.core.plugin.PluginInfo;
import com.epam.ta.reportportal.core.plugin.PluginPathInfo;
import java.nio.file.Path;
import org.springframework.web.multipart.MultipartFile;

/**
 * Plugin file manager that provides operations with plugin binaries (save, get, delete, etc.).
 *
 * @author <a href="mailto:budaevqwerty@gmail.com">Ivan Budayeu</a>
 */
public interface PluginFileManager {

  /**
   * Temporary upload file.
   *
   * @param pluginFile {@link MultipartFile} source file.
   * @return {@link Path} to uploaded plugin file.
   */
  Path uploadTemp(MultipartFile pluginFile);

  /**
   * Upload plugin binaries.
   *
   * @param pluginInfo {@link PluginInfo} that contains plugin properties
   * @return {@link PluginPathInfo} that contains binaries' store properties
   */
  PluginPathInfo upload(PluginInfo pluginInfo);

  /**
   * Download plugin binaries.
   *
   * @param pluginPathInfo {@link PluginPathInfo} that contains download source and target.
   */
  void download(PluginPathInfo pluginPathInfo);

  /**
   * Delete plugin binaries.
   *
   * @param pluginPathInfo {@link PluginPathInfo} that contains binaries' store properties
   */
  void delete(PluginPathInfo pluginPathInfo);

  /**
   * Delete plugin binaries by path.
   *
   * @param path {@link Path} to plugin local binaries.
   */
  void delete(Path path);

  /**
   * Delete plugin binaries by plugin file id.
   *
   * @param fileId plugin file id
   */
  void delete(String fileId);
}