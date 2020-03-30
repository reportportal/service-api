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

package com.epam.ta.reportportal.core.integration.plugin;

import com.epam.ta.reportportal.core.plugin.PluginInfo;
import com.epam.ta.reportportal.entity.integration.IntegrationTypeDetails;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.pf4j.PluginException;
import org.pf4j.PluginWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface PluginLoader {

	/**
	 * Extract info about the plugin from the provided path
	 *
	 * @param pluginPath Plugin's path
	 * @return {@link PluginInfo} with {@link PluginInfo#getId()} and {@link PluginInfo#getVersion()}
	 */
	PluginInfo extractPluginInfo(Path pluginPath) throws PluginException;

	/**
	 * Creates the {@link IntegrationTypeDetails} object based on the params of the plugin
	 *
	 * @param pluginInfo {@link PluginInfo} with {@link PluginInfo#getId()} and {@link PluginInfo#getVersion()}
	 * @return {@link IntegrationTypeDetails}
	 */
	IntegrationTypeDetails resolvePluginDetails(PluginInfo pluginInfo);

	/**
	 * Validate the plugin with {@link com.epam.reportportal.extension.common.ExtensionPoint}
	 * on the presence of the mandatory extension class/classes
	 *
	 * @param plugin {@link PluginWrapper}
	 * @return true if the plugin has mandatory extension class/classes, else false
	 */
	boolean validatePluginExtensionClasses(PluginWrapper plugin);

	/**
	 * Save plugin in the {@link com.epam.ta.reportportal.filesystem.DataStore}
	 *
	 * @param fileName   New plugin file name
	 * @param fileStream {@link InputStream} of the new plugin file
	 * @return File id of the saved file in the file system
	 * @throws ReportPortalException
	 */
	String saveToDataStore(String fileName, InputStream fileStream) throws ReportPortalException;

	/**
	 * Upload plugin file to the directory.
	 *
	 * @param pluginPath Path to save plugin file
	 * @param fileStream {@link InputStream} of the plugin file
	 */
	void savePlugin(Path pluginPath, InputStream fileStream) throws IOException;

	/**
	 * Copy plugin with resources from the {@link com.epam.ta.reportportal.filesystem.DataStore} to the provided path
	 *
	 * @param fileId              {@link com.epam.ta.reportportal.core.integration.util.property.IntegrationDetailsProperties#FILE_ID} value
	 * @param pluginPath          Path where to copy plugin file
	 * @param pluginResourcesPath Path were to copy plugin resources
	 */
	void copyFromDataStore(String fileId, Path pluginPath, Path pluginResourcesPath) throws IOException;

	/**
	 * Delete plugin file from the {@link com.epam.ta.reportportal.filesystem.DataStore}
	 *
	 * @param fileId {@link com.epam.ta.reportportal.core.integration.util.property.IntegrationDetailsProperties#FILE_ID} value
	 */
	void deleteFromDataStore(String fileId);

	/**
	 * Copy plugin resources to the target path
	 *
	 * @param pluginPath          Plugin path in the filesystem
	 * @param resourcesTargetPath Path to copy plugin resources
	 * @throws IOException
	 */
	void copyPluginResource(Path pluginPath, Path resourcesTargetPath) throws IOException, ReportPortalException;

	/**
	 * Remove the plugin file from the temporary directory and file name from the {@link com.epam.ta.reportportal.plugin.Pf4jPluginManager#uploadingPlugins}
	 *
	 * @param pluginFileDirectory Path to the temporary directory with the plugin file
	 * @param pluginFileName      Name of the plugin file
	 */
	void deleteTempPlugin(String pluginFileDirectory, String pluginFileName) throws IOException;
}
