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

package com.epam.ta.reportportal.core.integration.plugin.impl;

import com.epam.reportportal.extension.common.ExtensionPoint;
import com.epam.reportportal.extension.common.IntegrationTypeProperties;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.integration.plugin.PluginLoader;
import com.epam.ta.reportportal.core.plugin.PluginInfo;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.entity.integration.IntegrationTypeDetails;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.filesystem.DataStore;
import com.epam.ta.reportportal.ws.converter.builders.IntegrationTypeBuilder;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginDescriptorFinder;
import org.pf4j.PluginException;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class PluginLoaderImpl implements PluginLoader {

	private final String pluginsRootPath;

	private final DataStore dataStore;
	private final IntegrationTypeRepository integrationTypeRepository;
	private final PluginDescriptorFinder pluginDescriptorFinder;

	@Autowired
	public PluginLoaderImpl(@Value("${rp.plugins.path}") String pluginsRootPath, DataStore dataStore,
			IntegrationTypeRepository integrationTypeRepository, PluginDescriptorFinder pluginDescriptorFinder) {
		this.pluginsRootPath = pluginsRootPath;
		this.dataStore = dataStore;
		this.integrationTypeRepository = integrationTypeRepository;
		this.pluginDescriptorFinder = pluginDescriptorFinder;
	}

	@Override
	@NotNull
	public PluginInfo extractPluginInfo(Path pluginPath) throws PluginException {
		PluginDescriptor pluginDescriptor = pluginDescriptorFinder.find(pluginPath);
		return new PluginInfo(pluginDescriptor.getPluginId(), pluginDescriptor.getVersion());
	}

	@Override
	public IntegrationTypeDetails resolvePluginDetails(PluginInfo pluginInfo) {

		integrationTypeRepository.findByName(pluginInfo.getId())
				.flatMap(it -> ofNullable(it.getDetails()))
				.flatMap(typeDetails -> IntegrationTypeProperties.VERSION.getValue(typeDetails.getDetails()).map(String::valueOf))
				.ifPresent(version -> BusinessRule.expect(version, v -> !v.equalsIgnoreCase(pluginInfo.getVersion()))
						.verify(ErrorType.PLUGIN_UPLOAD_ERROR,
								Suppliers.formattedSupplier("Plugin with ID = '{}' of the same VERSION = '{}' has already been uploaded.",
										pluginInfo.getId(),
										pluginInfo.getVersion()
								)
						));

		IntegrationTypeDetails pluginDetails = IntegrationTypeBuilder.createIntegrationTypeDetails();
		IntegrationTypeProperties.VERSION.setValue(pluginDetails, pluginInfo.getVersion());
		return pluginDetails;
	}

	@Override
	public boolean validatePluginExtensionClasses(PluginWrapper plugin) {
		return plugin.getPluginManager()
				.getExtensionClasses(plugin.getPluginId())
				.stream()
				.map(ExtensionPoint::findByExtension)
				.anyMatch(Optional::isPresent);
	}

	@Override
	public String saveToDataStore(String fileName, InputStream fileStream) throws ReportPortalException {
		return dataStore.save(fileName, fileStream);
	}

	@Override
	public void savePlugin(Path pluginPath, InputStream fileStream) throws IOException {
		Files.copy(fileStream, pluginPath, StandardCopyOption.REPLACE_EXISTING);
	}

	@Override
	public void copyFromDataStore(String fileId, Path pluginPath, Path resourcesPath) throws IOException {
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
		jarFile.stream().filter(jarEntry -> jarEntry.getName().startsWith("resources")).forEach(entry -> {
			try {
				copyResources(jarFile, entry, destination);
			} catch (IOException e) {
				throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR, e.getMessage());
			}
		});
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

	@Override
	public void deleteTempPlugin(String pluginFileDirectory, String pluginFileName) throws IOException {
		Files.deleteIfExists(Paths.get(pluginFileDirectory, pluginFileName));
	}

}
