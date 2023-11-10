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

import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.integration.plugin.file.validator.FileValidator;
import com.epam.ta.reportportal.core.plugin.PluginInfo;
import com.epam.ta.reportportal.core.plugin.PluginPathInfo;
import com.epam.ta.reportportal.entity.enums.FeatureFlag;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.filesystem.DataStore;
import com.epam.ta.reportportal.util.FeatureFlagHandler;
import com.epam.ta.reportportal.ws.model.ErrorType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Plugin file manager is built over {@link DataStore} that provides operations with plugin binaries
 * (save, get, delete, etc.).
 *
 * @author <a href="mailto:budaevqwerty@gmail.com">Ivan Budayeu</a>
 */
@Service
public class DataStorePluginFileManager implements PluginFileManager {

  public static final Logger LOGGER = LoggerFactory.getLogger(DataStorePluginFileManager.class);

  private static final String PLUGINS_ROOT_PATH = "plugins";

  private final String pluginsTempDir;
  private final String pluginsDir;
  private final String resourcesDir;

  private final List<FileValidator> fileValidators;
  private final DataStore dataStore;

  private final FeatureFlagHandler featureFlagHandler;

  @Autowired
  public DataStorePluginFileManager(@Value("${rp.plugins.temp.path}") String pluginsTempPath,
      @Value("${rp.plugins.path}") String pluginsDir,
      @Value("${rp.plugins.resources.path}") String resourcesDir,
      List<FileValidator> fileValidators, DataStore dataStore,
      FeatureFlagHandler featureFlagHandler) throws IOException {
    this.pluginsTempDir = pluginsTempPath;
    this.pluginsDir = pluginsDir;
    this.resourcesDir = resourcesDir;
    this.fileValidators = fileValidators;
    this.dataStore = dataStore;
    this.featureFlagHandler = featureFlagHandler;

    Files.createDirectories(Paths.get(this.pluginsTempDir));
    Files.createDirectories(Paths.get(this.pluginsDir));
    Files.createDirectories(Paths.get(this.resourcesDir));
  }

  /**
   * Upload file to {@link DataStorePluginFileManager#pluginsTempDir}.
   *
   * @param pluginFile {@link MultipartFile} source file.
   * @return {@link Path} to uploaded plugin file.
   */
  @Override
  public Path uploadTemp(MultipartFile pluginFile) {
    fileValidators.forEach(v -> v.validate(pluginFile));
    final String fileName = pluginFile.getOriginalFilename();
    final Path targetPath = Paths.get(pluginsTempDir, fileName);

    try (InputStream inputStream = pluginFile.getInputStream()) {
      Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
      return targetPath;
    } catch (IOException e) {
      throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
          Suppliers.formattedSupplier(
              "Unable to copy the new plugin file with name = '{}' to the temp directory",
              pluginFile.getName()
          ).get()
      );
    }
  }

  /**
   * Save file in the {@link DataStore} and make a local copy.
   *
   * @param pluginInfo {@link PluginInfo} that contains plugin properties
   * @return {@link PluginPathInfo} that contains binaries' store properties
   */
  @Override
  public PluginPathInfo upload(PluginInfo pluginInfo) {
    final String newPluginFileName = generatePluginFileName(pluginInfo);
    final String fileId = saveToDataStore(pluginInfo.getOriginalFilePath(), pluginInfo,
        newPluginFileName);

    final Path targetPluginPath = Paths.get(pluginsDir, newPluginFileName);
    final Path targetResourcesPath = Paths.get(resourcesDir, pluginInfo.getId());
    final PluginPathInfo pluginPathInfo = new PluginPathInfo(targetPluginPath, targetResourcesPath,
        newPluginFileName, fileId);

    download(pluginPathInfo);
    return pluginPathInfo;
  }

  /**
   * Download plugin binaries.
   *
   * @param pluginPathInfo {@link PluginPathInfo} that contains download source and target.
   */
  @Override
  public void download(PluginPathInfo pluginPathInfo) {
    try {
      try (InputStream inputStream = dataStore.load(pluginPathInfo.getFileId())) {
        Files.copy(inputStream, pluginPathInfo.getPluginPath(),
            StandardCopyOption.REPLACE_EXISTING);
      }
      copyPluginResource(pluginPathInfo.getPluginPath(), pluginPathInfo.getResourcesPath());
    } catch (IOException e) {
      throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
          Suppliers.formattedSupplier(
              "Unable to copy new plugin file = '{}' from the data store to the root directory: {}",
              pluginPathInfo.getFileName(),
              e.getMessage()
          ).get()
      );
    }
  }

  /**
   * Delete plugin binaries.
   *
   * @param pluginPathInfo {@link PluginPathInfo} that contains binaries' store properties
   */
  @Override
  public void delete(PluginPathInfo pluginPathInfo) {
    delete(pluginPathInfo.getFileId());
    delete(pluginPathInfo.getPluginPath());
    delete(pluginPathInfo.getResourcesPath());
  }

  /**
   * Delete plugin local binaries.
   *
   * @param path {@link Path} to plugin local binaries.
   */
  @Override
  public void delete(Path path) {
    try {
      FileUtils.deleteDirectory(path.toFile());
    } catch (IOException e) {
      //error during temp plugin is not crucial, temp files cleaning will be delegated to the plugins cleaning job
      LOGGER.error("Error during plugin file removing: '{}'", e.getMessage());
    }
  }

  /**
   * Delete plugin binaries form {@link DataStore}.
   *
   * @param fileId {@link} plugin file id
   */
  @Override
  public void delete(String fileId) {
    try {
      dataStore.delete(fileId);
    } catch (Exception ex) {
      LOGGER.error("Error during removing plugin file from the Data store: {}", ex.getMessage());
    }
  }

  private String saveToDataStore(Path sourcePath, PluginInfo pluginInfo, String newPluginFileName) {
    try (InputStream fileStream = FileUtils.openInputStream(sourcePath.toFile())) {
      if (featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)) {
        return dataStore.save(Paths.get(PLUGINS_ROOT_PATH, newPluginFileName).toString(),
            fileStream);
      } else {
        return dataStore.save(newPluginFileName, fileStream);
      }
    } catch (Exception e) {
      throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
          Suppliers.formattedSupplier("Unable to upload new plugin file = '{}' to the data store",
              pluginInfo.getOriginalFilePath().getFileName().toString()
          ).get()
      );
    }
  }

  private String generatePluginFileName(PluginInfo pluginInfo) {
    return pluginInfo.getId() + "-" + pluginInfo.getVersion() + "." + FilenameUtils.getExtension(
        pluginInfo.getOriginalFilePath()
            .getFileName()
            .toString());
  }

  private void copyPluginResource(Path pluginPath, Path resourcesTargetPath) throws IOException {
    try (JarFile jar = new JarFile(pluginPath.toFile())) {
      if (!Files.isDirectory(resourcesTargetPath)) {
        Files.createDirectories(resourcesTargetPath);
      }
      copyJarResourcesRecursively(resourcesTargetPath, jar);
    }
  }

  private void copyJarResourcesRecursively(Path destination, JarFile jarFile) throws IOException {
    final List<JarEntry> resources = jarFile.stream()
        .filter(jarEntry -> jarEntry.getName().startsWith("resources"))
        .collect(Collectors.toList());

    for (JarEntry entry : resources) {
      copyResources(jarFile, entry, destination);
    }
  }

  private void copyResources(JarFile jarFile, JarEntry entry, Path destination) throws IOException {
    String fileName = StringUtils.substringAfter(entry.getName(), "resources/");
    if (!entry.isDirectory()) {
      try (InputStream entryInputStream = jarFile.getInputStream(entry)) {
        FileUtils.copyToFile(entryInputStream, new File(destination.toFile(), fileName));
      }
    } else {
      Files.createDirectories(Paths.get(destination.toString(), fileName));
    }
  }
}