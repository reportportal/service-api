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

package com.epam.ta.reportportal.core.integration.plugin;

import com.epam.ta.reportportal.entity.integration.IntegrationType;
import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for uploading plugins.
 */
public interface PluginUploader {

  /**
   * Uploads a plugin file.
   *
   * @param fileName the name of the file to be uploaded
   * @param inputStream the input stream of the file to be uploaded
   * @return the integration type of the uploaded plugin
   * @throws IOException if an I/O error occurs during the upload
   */
  IntegrationType uploadPlugin(String fileName, InputStream inputStream) throws IOException;
}
