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

package com.epam.ta.reportportal.core.launch.export;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * Utility class for determining file extensions based on content type.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class FileExtensionUtils {

  private static final Map<String, String> CONTENT_TYPE_TO_EXTENSION = new HashMap<>();

  static {
    // Text files
    CONTENT_TYPE_TO_EXTENSION.put("text/plain", ".txt");
    CONTENT_TYPE_TO_EXTENSION.put("text/html", ".html");
    CONTENT_TYPE_TO_EXTENSION.put("text/css", ".css");
    CONTENT_TYPE_TO_EXTENSION.put("text/javascript", ".js");
    CONTENT_TYPE_TO_EXTENSION.put("text/xml", ".xml");
    CONTENT_TYPE_TO_EXTENSION.put("text/csv", ".csv");
    CONTENT_TYPE_TO_EXTENSION.put("text/x-php", ".php");
    
    // Image files
    CONTENT_TYPE_TO_EXTENSION.put("image/png", ".png");
    CONTENT_TYPE_TO_EXTENSION.put("image/jpeg", ".jpg");
    CONTENT_TYPE_TO_EXTENSION.put("image/gif", ".gif");
    CONTENT_TYPE_TO_EXTENSION.put("image/svg+xml", ".svg");
    CONTENT_TYPE_TO_EXTENSION.put("image/webp", ".webp");
    
    // Application files
    CONTENT_TYPE_TO_EXTENSION.put("application/pdf", ".pdf");
    CONTENT_TYPE_TO_EXTENSION.put("application/json", ".json");
    CONTENT_TYPE_TO_EXTENSION.put("application/xml", ".xml");
    CONTENT_TYPE_TO_EXTENSION.put("application/zip", ".zip");
    CONTENT_TYPE_TO_EXTENSION.put("application/x-rar-compressed", ".rar");
    CONTENT_TYPE_TO_EXTENSION.put("application/tar", ".tar");
    CONTENT_TYPE_TO_EXTENSION.put("application/gzip", ".gz");
    CONTENT_TYPE_TO_EXTENSION.put("application/har+json", ".har");
    CONTENT_TYPE_TO_EXTENSION.put("application/javascript", ".js");
  }

  /**
   * Determines the appropriate file extension based on content type.
   * If the filename already has an extension, it returns the filename as is.
   * If no extension is found for the content type, it returns the filename as is.
   *
   * @param fileName    the original filename
   * @param contentType the content type of the file
   * @return filename with appropriate extension
   */
  public static String getFileNameWithExtension(String fileName, String contentType) {
    if (StringUtils.isBlank(fileName)) {
      return fileName;
    }
    
    // Check if filename already has an extension
    if (fileName.contains(".")) {
      return fileName;
    }
    
    // Get extension from content type
    String extension = CONTENT_TYPE_TO_EXTENSION.get(contentType);
    if (StringUtils.isNotBlank(extension)) {
      return fileName + extension;
    }
    
    // Return original filename if no extension found
    return fileName;
  }
}

