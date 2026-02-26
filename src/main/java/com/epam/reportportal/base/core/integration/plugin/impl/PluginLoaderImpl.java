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

package com.epam.reportportal.base.core.integration.plugin.impl;

import static java.util.Optional.ofNullable;

import com.epam.reportportal.base.core.integration.plugin.PluginLoader;
import com.epam.reportportal.base.core.plugin.PluginInfo;
import com.epam.reportportal.extension.common.ExtensionPoint;
import com.epam.reportportal.extension.common.IntegrationTypeProperties;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationTypeRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.FeatureFlag;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationTypeDetails;
import com.epam.reportportal.base.infrastructure.persistence.filesystem.DataStore;
import com.epam.reportportal.base.infrastructure.persistence.util.FeatureFlagHandler;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.plugin.DetailPluginDescriptor;
import com.epam.reportportal.base.ws.converter.builders.IntegrationTypeBuilder;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.PluginDescriptorFinder;
import org.pf4j.PluginRuntimeException;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service responsible for plugin load.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class PluginLoaderImpl implements PluginLoader {

  private static final String PLUGINS_ROOT_PATH = "plugins";
  private final DataStore dataStore;
  private final IntegrationTypeRepository integrationTypeRepository;
  private final PluginDescriptorFinder pluginDescriptorFinder;

  private final FeatureFlagHandler featureFlagHandler;

  /**
   * Creates instance of {@link PluginLoader}.
   *
   * @param dataStore                 {@link DataStore}
   * @param integrationTypeRepository {@link IntegrationTypeRepository}
   * @param pluginDescriptorFinder    {@link PluginDescriptorFinder}
   * @param featureFlagHandler        {@link FeatureFlagHandler}
   */
  @Autowired
  public PluginLoaderImpl(DataStore dataStore, IntegrationTypeRepository integrationTypeRepository,
      PluginDescriptorFinder pluginDescriptorFinder, FeatureFlagHandler featureFlagHandler) {
    this.dataStore = dataStore;
    this.integrationTypeRepository = integrationTypeRepository;
    this.pluginDescriptorFinder = pluginDescriptorFinder;
    this.featureFlagHandler = featureFlagHandler;
  }

  @Override
  @NotNull
  public PluginInfo extractPluginInfo(Path pluginPath) throws PluginRuntimeException {
    var descriptor = (DetailPluginDescriptor) pluginDescriptorFinder.find(pluginPath);
    return new PluginInfo(
        descriptor.getPluginId(),
        descriptor.getVersion(),
        convertToDetails(descriptor)
    );
  }

  @Override
  public IntegrationTypeDetails resolvePluginDetails(PluginInfo pluginInfo) {

    integrationTypeRepository.findByName(pluginInfo.getId())
        .flatMap(it -> ofNullable(it.getDetails())).flatMap(
            typeDetails -> IntegrationTypeProperties.VERSION.getValue(typeDetails.getDetails())
                .map(String::valueOf)).ifPresent(
            version -> BusinessRule.expect(version, v -> !v.equalsIgnoreCase(pluginInfo.getVersion()))
                .verify(
                    ErrorType.PLUGIN_UPLOAD_ERROR, Suppliers.formattedSupplier(
                        "Plugin with ID = '{}' of the same VERSION = '{}' "
                            + "has already been uploaded.", pluginInfo.getId(),
                        pluginInfo.getVersion()
                    )));

    IntegrationTypeDetails pluginDetails = IntegrationTypeBuilder.createIntegrationTypeDetails();
    IntegrationTypeProperties.VERSION.setValue(pluginDetails, pluginInfo.getVersion());
    return pluginDetails;
  }

  @Override
  public boolean validatePluginExtensionClasses(PluginWrapper plugin) {
    return plugin.getPluginManager().getExtensionClasses(plugin.getPluginId()).stream()
        .map(ExtensionPoint::findByExtension).anyMatch(Optional::isPresent);
  }

  @Override
  public String saveToDataStore(String fileName, InputStream fileStream)
      throws ReportPortalException {
    if (featureFlagHandler.isEnabled(FeatureFlag.SINGLE_BUCKET)) {
      return dataStore.save(Paths.get(PLUGINS_ROOT_PATH, fileName).toString(), fileStream);
    } else {
      return dataStore.save(fileName, fileStream);
    }
  }

  @Override
  public void savePlugin(Path pluginPath, InputStream fileStream) throws IOException {
    Files.copy(fileStream, pluginPath, StandardCopyOption.REPLACE_EXISTING);
  }

  @Override
  public void copyFromDataStore(String fileId, Path pluginPath, Path resourcesPath)
      throws IOException {
    if (Objects.nonNull(pluginPath.getParent())) {
      Files.createDirectories(pluginPath.getParent());
    }
    try (InputStream inputStream = dataStore.load(fileId)) {
      Files.copy(inputStream, pluginPath, StandardCopyOption.REPLACE_EXISTING);
    }
    copyPluginResource(pluginPath, resourcesPath);
  }

  @Override
  public void deleteFromDataStore(String fileId) {
    dataStore.delete(fileId);
  }

  @Override
  public void copyPluginResource(Path pluginPath, Path resourcesTargetPath) throws IOException {
    if (Objects.nonNull(resourcesTargetPath.getParent())) {
      Files.createDirectories(resourcesTargetPath.getParent());
    }
    try (JarFile jar = new JarFile(pluginPath.toFile())) {
      if (!Files.isDirectory(resourcesTargetPath)) {
        Files.createDirectories(resourcesTargetPath);
      }
      copyJarResourcesRecursively(resourcesTargetPath, jar);
    }
  }

  private void copyJarResourcesRecursively(Path destination, JarFile jarFile) {
    jarFile.stream().filter(jarEntry -> jarEntry.getName().startsWith("resources"))
        .forEach(entry -> {
          try {
            copyResources(jarFile, entry, destination);
          } catch (IOException e) {
            throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR, e.getMessage());
          }
        });
  }

  private void copyResources(JarFile jarFile, JarEntry entry, Path destination) throws IOException {
    String fileName = StringUtils.substringAfter(entry.getName(), "resources/");
    validateZipEntry(fileName, destination);

    Path targetPath = destination.resolve(fileName).normalize();
    Path destinationPath = destination.toAbsolutePath().normalize();

    if (!targetPath.startsWith(destinationPath)) {
      throw new IOException(
          "Zip Slip detected: Entry is outside of the target directory: " + fileName);
    }

    if (!entry.isDirectory()) {
      try (InputStream entryInputStream = jarFile.getInputStream(entry)) {
        Files.createDirectories(targetPath.getParent());
        FileUtils.copyToFile(entryInputStream, targetPath.toFile());
      }
    } else {
      Files.createDirectories(targetPath);
    }
  }

  /**
   * Validates a zip entry file name to prevent Zip Slip vulnerability. Ensures the resolved path stays within the
   * destination directory.
   *
   * @param fileName    the file name from the zip entry
   * @param destination the destination directory
   * @throws IOException if the file name is invalid or attempts path traversal
   */
  private void validateZipEntry(String fileName, Path destination) throws IOException {
    Path destinationPath = destination.toAbsolutePath().normalize();
    Path targetPath = destinationPath.resolve(fileName).normalize();

    if (!targetPath.startsWith(destinationPath)) {
      throw new IOException(
          "Invalid archive entry: Entry is outside of the target directory: " + fileName);
    }
  }

  @Override
  public void deleteTempPlugin(String pluginFileDirectory, String pluginFileName)
      throws IOException {
    Files.deleteIfExists(Paths.get(pluginFileDirectory, pluginFileName));
  }

  private Map<String, Object> convertToDetails(DetailPluginDescriptor descriptor) {
    Map<String, Object> details = new HashMap<>();
    details.put("id", descriptor.getPluginId());
    details.put("name", descriptor.getPluginName());
    details.put("version", descriptor.getVersion());
    details.put("license", descriptor.getLicense());
    details.put("description", descriptor.getPluginDescription());
    details.put("documentation", descriptor.getDocumentation());
    details.put("requires", descriptor.getRequires());
    Optional.ofNullable(descriptor.getMetadata())
        .filter(metadata -> !metadata.isEmpty())
        .ifPresent(metadata -> details.put("metadata", metadata));
    Optional.ofNullable(descriptor.getProperties())
        .filter(props -> !props.isEmpty())
        .ifPresent(props -> details.put("properties", props));
    Optional.ofNullable(descriptor.getBinaryData())
        .filter(binaryData -> !binaryData.isEmpty())
        .ifPresent(binaryData -> details.put("binaryData", binaryData));

    var developer = new HashMap<String, Object>();
    developer.put("name", descriptor.getProvider());
    details.put("developer", developer);
    return details;
  }
}
