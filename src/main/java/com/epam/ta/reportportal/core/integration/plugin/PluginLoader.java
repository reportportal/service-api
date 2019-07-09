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

package com.epam.ta.reportportal.core.integration.plugin;

import com.epam.ta.reportportal.entity.plugin.PluginFileExtension;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Optional;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface PluginLoader {

	/**
	 * Extract info about the plugin from the provided path
	 *
	 * @param pluginPath Plugin's path
	 * @return {@link PluginInfo} with {@link PluginInfo#id} and {@link PluginInfo#version}
	 */
	PluginInfo extractPluginInfo(Path pluginPath);

	/**
	 * Load and start up the plugin
	 *
	 * @param plugin {@link PluginWrapper} with mandatory data for plugin loading: {@link PluginWrapper#pluginPath}
	 * @return {@link PluginState}
	 */
	PluginState loadAndStartUpPlugin(PluginWrapper plugin);

	/**
	 * Validate the plugin with {@link com.epam.reportportal.extension.common.ExtensionPoint}
	 * on the presence of the mandatory extension class/classes
	 *
	 * @param pluginId {@link PluginWrapper#getPluginId()}
	 * @return true if the plugin has mandatory extension class/classes, else false
	 */
	boolean validatePluginExtensionClasses(String pluginId);

	/**
	 * Unload and get plugin with the same 'id' as a new plugin 'id', if they both are of the same type.
	 * This info will be needed to reload previous plugin if something goes wrong with the new one.
	 *
	 * @param newPluginId       Id of the new plugin
	 * @param newPluginFileName New plugin file name
	 * @return {@link Optional} wrapper with the previous unloaded plugin
	 */
	Optional<PluginWrapper> retrievePreviousPlugin(String newPluginId, String newPluginFileName);

	/**
	 * Remove old plugin file, if it wasn't replaced by the new one during the plugin uploading
	 *
	 * @param previousPlugin    {@link PluginWrapper} with info about the previous plugin
	 * @param newPluginFileName New plugin file name
	 */
	void deletePreviousPlugin(PluginWrapper previousPlugin, String newPluginFileName);

	/**
	 * Resolve file type and upload it to the temporary plugins directory.
	 * If successful returns file extension
	 *
	 * @param pluginFile Plugin file to upload
	 * @return {@link PluginFileExtension#extension}
	 */
	String resolveFileExtensionAndUploadTempPlugin(MultipartFile pluginFile, Path pluginsTempPath);

	/**
	 * Remove the plugin file from the temporary directory and file name from the {@link com.epam.ta.reportportal.plugin.Pf4jPluginManager#uploadingPlugins}
	 *
	 * @param pluginFileDirectory Path to the temporary directory with the plugin file
	 * @param pluginFileName      Name of the plugin file
	 */
	void deleteTempPlugin(String pluginFileDirectory, String pluginFileName);
}
