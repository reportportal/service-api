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

package com.epam.ta.reportportal.core.plugin;

import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * PluginBox holds information about all loaded/available plugins
 *
 * @author Andrei Varabyeu
 */
public interface PluginBox {

	/**
	 * @return All available plugins
	 */
	List<Plugin> getPlugins();

	/**
	 * @param type Type of plugin
	 * @return Optional of plugin by given type
	 */
	Optional<Plugin> getPlugin(String type);

	/**
	 * Creates (or takes from cache) instance of given plugin
	 *
	 * @param name Plugin name / ID
	 * @param type Type of plugin
	 * @return Optional of plugin by given type
	 */
	<T> Optional<T> getInstance(String name, Class<T> type);

	/**
	 * Creates (or takes from cache) instance of given plugin
	 *
	 * @param type Type of plugin
	 * @return Optional of plugin by given type
	 */
	<T> Optional<T> getInstance(Class<T> type);

	PluginState startUpPlugin(String pluginId);

	String loadPlugin(Path path);

	boolean unloadPlugin(String pluginId);

	Optional<PluginWrapper> getPluginById(String id);

}
