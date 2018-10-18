package com.epam.ta.reportportal.core.plugin;

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
	 * @param type Type of plugin
	 * @return Optional of plugin by given type
	 */
	<T> Optional<T> getInstance(Class<T> type);

}
