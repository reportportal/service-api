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

package com.epam.ta.reportportal.core.plugin;

import java.io.Serializable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This class represents information about a plugin, including its ID, version, file ID, file name,
 * enabled status, and additional details.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PluginInfo implements Serializable {

  private String id;
  private String version;
  private String fileId;
  private String fileName;
  private boolean isEnabled;
  private Map<String, Object> details;

  /**
   * Constructs a {@link PluginInfo} object with the specified ID and version.
   *
   * @param id      The ID of the plugin.
   * @param version The version of the plugin.
   */
  public PluginInfo(String id, String version) {
    this.id = id;
    this.version = version;
  }

  /**
   * Constructs a {@link PluginInfo} object with the specified ID, version, and details.
   *
   * @param id      The ID of the plugin.
   * @param version The version of the plugin.
   * @param details The details of the plugin.
   */
  public PluginInfo(String id, String version, Map<String, Object> details) {
    this.id = id;
    this.version = version;
    this.details = details;
  }

  /**
   * Constructs a {@link PluginInfo} object with the specified ID, version, file ID, file name, and
   * enabled status.
   *
   * @param id        The ID of the plugin.
   * @param version   The version of the plugin.
   * @param fileId    The file ID of the plugin.
   * @param fileName  The file name of the plugin.
   * @param isEnabled The enabled status of the plugin.
   */
  public PluginInfo(String id, String version, String fileId, String fileName, boolean isEnabled) {
    this.id = id;
    this.version = version;
    this.fileId = fileId;
    this.fileName = fileName;
    this.isEnabled = isEnabled;
  }
}
