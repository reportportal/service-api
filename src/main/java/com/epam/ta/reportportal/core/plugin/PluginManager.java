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

import com.google.common.util.concurrent.AbstractIdleService;

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
	protected void startUp() throws Exception {

	}

	@Override
	protected void shutDown() throws Exception {

	}
}
