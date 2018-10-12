package com.epam.ta.reportportal.core.plugin;

import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.AbstractScheduledService;
import org.pf4j.DefaultPluginManager;

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
	protected void startUp() throws Exception {

	}

	@Override
	protected void shutDown() throws Exception {

	}
}
