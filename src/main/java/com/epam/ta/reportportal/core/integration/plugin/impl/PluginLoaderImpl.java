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

import com.epam.ta.reportportal.core.integration.plugin.PluginLoader;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.filesystem.DataStore;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class PluginLoaderImpl implements PluginLoader {

	private final DataStore dataStore;

	@Autowired
	public PluginLoaderImpl(DataStore dataStore) {
		this.dataStore = dataStore;
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

}
