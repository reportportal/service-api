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

package com.epam.ta.reportportal.core.plugin;

import com.epam.ta.reportportal.entity.integration.IntegrationType;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface Pf4jPluginBox extends PluginBox {

	/**
	 * Start up loaded plugin by id
	 *
	 * @param pluginId {@link PluginWrapper#getPluginId()}
	 * @return {@link PluginState}
	 */
	PluginState startUpPlugin(String pluginId);

	/**
	 * Load plugin to the plugin manager by plugin file path
	 *
	 * @param path Path to the plugin file to load
	 * @return {@link PluginWrapper#getPluginId()}
	 */
	String loadPlugin(Path path);

	/**
	 * Unload plugin from the plugin manager by id
	 *
	 * @param pluginId {@link PluginWrapper#getPluginId()}
	 * @return 'true' if a plugin was successfully unloaded, else 'false'
	 */
	boolean unloadPlugin(String pluginId);

	/**
	 * Delete plugin by id
	 *
	 * @param pluginId {@link PluginWrapper#getPluginId()}
	 * @return 'true' if a plugin was successfully deleted, else 'false'
	 */
	boolean deletePlugin(String pluginId);

	/**
	 * Get plugin from the plugin manager by id
	 *
	 * @param id {@link PluginWrapper#getPluginId()}
	 * @return {@link PluginWrapper} wrapped in the {@link Optional}
	 */
	Optional<PluginWrapper> getPluginById(String id);

	/**
	 * Check if uploading plugins holder contains plugin file name
	 *
	 * @param fileName Name of the plugin file in the {@link com.epam.ta.reportportal.plugin.Pf4jPluginManager#uploadingPlugins}
	 *                 which uploaded state is required
	 * @return 'true' if {@link com.epam.ta.reportportal.plugin.Pf4jPluginManager#uploadingPlugins} contains plugin file name,
	 * else 'false'
	 * @see com.epam.ta.reportportal.plugin.Pf4jPluginManager
	 */
	boolean isInUploadingState(String fileName);

	IntegrationType uploadPlugin(String newPluginFileName, InputStream fileStream);
}
