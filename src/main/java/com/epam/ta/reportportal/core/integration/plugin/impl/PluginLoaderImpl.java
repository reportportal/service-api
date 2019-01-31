/*
 * Copyright 2018 EPAM Systems
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
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.integration.plugin.PluginInfo;
import com.epam.ta.reportportal.core.integration.plugin.PluginLoader;
import com.epam.ta.reportportal.core.integration.plugin.PluginUploadingCache;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.entity.plugin.PluginFileExtension;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.io.FilenameUtils;
import org.pf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class PluginLoaderImpl implements PluginLoader {

	private final String pluginsRootPath;

	private final PluginBox pluginBox;

	private final PluginDescriptorFinder pluginDescriptorFinder;

	private final PluginUploadingCache pluginUploadingCache;

	@Autowired
	public PluginLoaderImpl(@Value("${rp.plugins.path}") String pluginsRootPath, PluginBox pluginBox,
			PluginDescriptorFinder pluginDescriptorFinder, PluginUploadingCache pluginUploadingCache) {
		this.pluginsRootPath = pluginsRootPath;
		this.pluginBox = pluginBox;
		this.pluginDescriptorFinder = pluginDescriptorFinder;
		this.pluginUploadingCache = pluginUploadingCache;
	}

	@Override
	@NotNull
	public PluginInfo extractPluginInfo(Path pluginPath) {
		try {

			PluginDescriptor pluginDescriptor = pluginDescriptorFinder.find(pluginPath);

			return new PluginInfo(pluginDescriptor.getPluginId(), pluginDescriptor.getVersion());

		} catch (PluginException e) {

			ofNullable(pluginPath.getFileName()).ifPresent(name -> pluginUploadingCache.finishPluginUploading(name.toString()));

			throw new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, e);
		}
	}

	@Override
	public PluginState reloadPlugin(PluginWrapper plugin) {

		return pluginBox.startUpPlugin(ofNullable(pluginBox.loadPlugin(plugin.getPluginPath())).orElseThrow(() -> new ReportPortalException(
				ErrorType.PLUGIN_UPLOAD_ERROR,
				Suppliers.formattedSupplier("Unable to reload plugin with id = '{}", plugin.getPluginId()).get()
		)));
	}

	@Override
	public boolean validatePluginExtensionClasses(String pluginId) {

		PluginWrapper newPlugin = pluginBox.getPluginById(pluginId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
						Suppliers.formattedSupplier("Plugin with id = {} has not been found.", pluginId).get()
				));

		return newPlugin.getPluginManager()
				.getExtensionClasses(pluginId)
				.stream()
				.map(ExtensionPoint::findByExtension)
				.anyMatch(Optional::isPresent);
	}

	@Override
	public Optional<PluginWrapper> retrieveOldPlugin(String newPluginId, String newPluginFileName) {

		Optional<PluginWrapper> oldPlugin = pluginBox.getPluginById(newPluginId);

		oldPlugin.ifPresent(p -> {

			if (!pluginBox.unloadPlugin(p.getPluginId())) {
				throw new ReportPortalException(ErrorType.PLUGIN_REMOVE_ERROR,
						Suppliers.formattedSupplier("Failed to stop old plugin with id = {}", p.getPluginId()).get()
				);
			}
		});

		validateNewPluginFile(oldPlugin, newPluginFileName);

		return oldPlugin;
	}

	@Override
	public void deleteOldPlugin(PluginWrapper oldPluginWrapper, String newPluginFileName) {

		if (!oldPluginWrapper.getPluginPath().equals(Paths.get(pluginsRootPath, newPluginFileName))) {
			try {
				Files.deleteIfExists(oldPluginWrapper.getPluginPath());
			} catch (IOException e) {

				reloadPlugin(oldPluginWrapper);

				throw new ReportPortalException(ErrorType.PLUGIN_REMOVE_ERROR,
						Suppliers.formattedSupplier("Unable to delete the old plugin file with id = {}", oldPluginWrapper.getPluginId())
								.get()
				);
			}
		}
	}

	@Override
	public void createTempPluginsFolderIfNotExists(String path) {
		if (!Files.isDirectory(Paths.get(path))) {
			try {
				Files.createDirectories(Paths.get(path));
			} catch (IOException e) {

				throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
						Suppliers.formattedSupplier("Unable to create directory = {}", path).get()
				);
			}
		}
	}

	@Override
	public String resolveFileExtensionAndUploadTempPlugin(MultipartFile pluginFile, String pluginsTempPath) {

		String resolvedExtension = FilenameUtils.getExtension(pluginFile.getOriginalFilename());

		PluginFileExtension pluginFileExtension = PluginFileExtension.findByExtension("." + resolvedExtension)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
						Suppliers.formattedSupplier("Unsupported plugin file extension = {}", resolvedExtension).get()
				));

		Path pluginPath = Paths.get(pluginsTempPath, pluginFile.getOriginalFilename());

		try {
			pluginUploadingCache.startPluginUploading(pluginFile.getOriginalFilename(), pluginPath);

			Files.copy(pluginFile.getInputStream(), pluginPath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {

			pluginUploadingCache.finishPluginUploading(pluginFile.getOriginalFilename());

			throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
					Suppliers.formattedSupplier("Unable to copy the new plugin file with name = {} to the temp directory",
							pluginFile.getOriginalFilename()
					).get()
			);
		}

		return pluginFileExtension.getExtension();

	}

	@Override
	public void deleteTempPlugin(String pluginFileDirectory, String pluginFileName) {
		try {

			Files.deleteIfExists(Paths.get(pluginFileDirectory, pluginFileName));

		} catch (IOException e) {
			//error during temp plugin is not crucial, temp files cleaning will be delegated to //TODO impl Quartz job to clean temp files
		} finally {
			pluginUploadingCache.finishPluginUploading(pluginFileName);
		}
	}

	private void validateNewPluginFile(Optional<PluginWrapper> oldPlugin, String newPluginFileName) {

		if (new File(pluginsRootPath, newPluginFileName).exists()) {

			if (!oldPlugin.isPresent() || !Paths.get(pluginsRootPath, newPluginFileName).equals(oldPlugin.get().getPluginPath())) {
				throw new ReportPortalException(ErrorType.PLUGIN_UPLOAD_ERROR,
						Suppliers.formattedSupplier("Unable to rewrite plugin file = '{}' with different plugin type", newPluginFileName)
								.get()
				);
			}
		}
	}
}
