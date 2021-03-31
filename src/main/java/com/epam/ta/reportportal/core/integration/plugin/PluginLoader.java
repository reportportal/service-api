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

import com.epam.ta.reportportal.exception.ReportPortalException;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface PluginLoader {

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
}
