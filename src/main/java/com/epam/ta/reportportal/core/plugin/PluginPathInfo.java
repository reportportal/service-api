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

package com.epam.ta.reportportal.core.plugin;

import java.nio.file.Path;

/**
 * @author <a href="mailto:budaevqwerty@gmail.com">Ivan Budayeu</a>
 */
public class PluginPathInfo {

  private Path pluginPath;
  private Path resourcesPath;
  private String fileName;
  private String fileId;

  public PluginPathInfo() {
  }

  public PluginPathInfo(Path pluginPath, Path resourcesPath, String fileName, String fileId) {
    this.pluginPath = pluginPath;
    this.resourcesPath = resourcesPath;
    this.fileName = fileName;
    this.fileId = fileId;
  }

  public Path getPluginPath() {
    return pluginPath;
  }

  public void setPluginPath(Path pluginPath) {
    this.pluginPath = pluginPath;
  }

  public Path getResourcesPath() {
    return resourcesPath;
  }

  public void setResourcesPath(Path resourcesPath) {
    this.resourcesPath = resourcesPath;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getFileId() {
    return fileId;
  }

  public void setFileId(String fileId) {
    this.fileId = fileId;
  }
}