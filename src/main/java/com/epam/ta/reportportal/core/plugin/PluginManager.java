package com.epam.ta.reportportal.core.plugin;

import com.google.common.util.concurrent.AbstractIdleService;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PluginManager extends AbstractIdleService implements PluginBox {

	private List<PluginBox> pluginBoxes;


	public PluginManager(List<PluginBox> pluginBoxes) {
		this.pluginBoxes = pluginBoxes;
	}

	@Override
	public List<Plugin> getPlugins() {
		return this.pluginBoxes.stream().flatMap(pluginBox -> pluginBox.getPlugins().stream()).collect(Collectors.toList());
	}

	@Override
	public Optional<Plugin> getPlugin(String type) {
		return this.pluginBoxes.stream()
				.flatMap(pluginBox -> pluginBox.getPlugins().stream())
				.filter(plugin -> plugin.getType().equals(type))
				.findAny();
	}

	@Override
	public <T> Optional<T> getInstance(String name, Class<T> type) {
		return Optional.empty();
	}

	@Override
	public <T> Optional<T> getInstance(Class<T> type) {
		return Optional.empty();
	}

	@Override
	public PluginState startUpPlugin(String pluginId) {
		return null;
	}

	@Override
	public String loadPlugin(Path path) {
		return null;
	}

	@Override
	public boolean unloadPlugin(String pluginId) {
		return false;
	}

	@Override
	public Optional<PluginWrapper> getPluginById(String id) {
		return Optional.empty();
	}

	@Override
	protected void startUp() throws Exception {

	}

	@Override
	protected void shutDown() throws Exception {

	}
}
