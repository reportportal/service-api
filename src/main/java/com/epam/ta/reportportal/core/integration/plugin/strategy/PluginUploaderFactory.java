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

package com.epam.ta.reportportal.core.integration.plugin.strategy;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.integration.plugin.PluginUploader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Factory class for creating instances of {@link PluginUploader} based on the content type. This
 * class is responsible for managing different types of plugin uploads and providing the appropriate
 * one based on the content type.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 */
@Component
public class PluginUploaderFactory {

  private final Map<String, PluginUploader> uploads = new HashMap<>();

  /**
   * Constructor for PluginUploaderFactory.
   *
   * @param jarPluginUploader  Instance of {@link JarPluginUploader} for handling JAR uploads
   * @param jsonPluginUploader Instance of {@link JsonPluginUploader} for handling JSON uploads
   */
  @Autowired
  public PluginUploaderFactory(
      JarPluginUploader jarPluginUploader,
      JsonPluginUploader jsonPluginUploader
  ) {
    uploads.put("application/java-archive", jarPluginUploader);
    uploads.put("application/json", jsonPluginUploader);
  }

  /**
   * Retrieves the appropriate {@link PluginUploader} based on the provided content type.
   *
   * @param contentType The content type of the plugin to be uploaded
   * @return An instance of {@link PluginUploader} that matches the content type
   * @throws ReportPortalException if no uploader is found for the specified content type
   */
  public PluginUploader getUploader(String contentType) {
    return Optional.ofNullable(uploads.get(contentType))
        .orElseThrow(() -> new ReportPortalException(
            ErrorType.PLUGIN_UPLOAD_ERROR,
            "Unsupported content type: " + contentType
        ));
  }
}