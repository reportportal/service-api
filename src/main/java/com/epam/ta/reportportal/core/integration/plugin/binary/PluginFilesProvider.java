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

package com.epam.ta.reportportal.core.integration.plugin.binary;

import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.attachment.BinaryData;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.reportportal.rules.exception.ErrorType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import jakarta.activation.FileTypeMap;
import java.util.Objects;
import org.apache.commons.io.FileUtils;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class PluginFilesProvider {

  private final String baseDirectory;
  private final String folderQualifier;

  private final FileTypeMap fileTypeResolver;

  private final IntegrationTypeRepository integrationTypeRepository;

  public PluginFilesProvider(String baseDirectory, String folderQualifier,
      FileTypeMap fileTypeResolver,
      IntegrationTypeRepository integrationTypeRepository) {
    this.baseDirectory = baseDirectory;
    this.folderQualifier = folderQualifier;
    this.fileTypeResolver = fileTypeResolver;
    this.integrationTypeRepository = integrationTypeRepository;
  }

  /**
   * Loads a plugin's file as binary data by name and filename.
   *
   * @param pluginName the name of the plugin
   * @param fileName   the name of the file to load
   * @return BinaryData representation of the file
   */
  public BinaryData load(String pluginName, String fileName) {
    final IntegrationType integrationType = integrationTypeRepository.findByName(pluginName)
        .orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, pluginName));

    validateFileName(fileName);

    final Path basePath = Paths.get(baseDirectory, integrationType.getName(), folderQualifier)
        .toAbsolutePath().normalize();
    final File file = getFile(fileName, basePath);

    return getBinaryData(file);
  }

  private File getFile(String fileName, Path basePath) {
    final Path filePath = basePath.resolve(fileName).normalize();

    if (!filePath.startsWith(basePath)) {
      throw new ReportPortalException(ErrorType.UNABLE_TO_LOAD_BINARY_DATA,
          "Invalid file path: " + fileName);
    }

    final File file = filePath.toFile();

    if (!file.exists() || file.isDirectory()) {
      throw new ReportPortalException(ErrorType.UNABLE_TO_LOAD_BINARY_DATA, fileName);
    }
    return file;
  }

  private void validateFileName(String fileName) {
    if (Objects.isNull(fileName) || fileName.isBlank()) {
      throw new ReportPortalException(ErrorType.UNABLE_TO_LOAD_BINARY_DATA,
          "File name cannot be null or blank");
    }

    if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")
        || fileName.startsWith("/") || fileName.matches("^[a-zA-Z]:.*")) {
      throw new ReportPortalException(ErrorType.UNABLE_TO_LOAD_BINARY_DATA,
          "Invalid or unsafe file name: " + fileName);
    }
  }

  private BinaryData getBinaryData(File file) {
    try {
      final InputStream fileStream = FileUtils.openInputStream(file);
      final String contentType = fileTypeResolver.getContentType(file.getName());
      return new BinaryData(contentType, (long) fileStream.available(), fileStream);
    } catch (IOException e) {
      throw new ReportPortalException(ErrorType.UNABLE_TO_LOAD_BINARY_DATA, e.getMessage());
    }
  }
}
