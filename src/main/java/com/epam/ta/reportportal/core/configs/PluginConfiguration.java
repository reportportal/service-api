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

package com.epam.ta.reportportal.core.configs;

import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.plugin.P4jPluginManager;
import com.google.common.collect.Sets;
import org.pf4j.ManifestPluginDescriptorFinder;
import org.pf4j.PluginDescriptorFinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PluginConfiguration {

	@Autowired
	private AutowireCapableBeanFactory context;

	@Value("${rp.plugins.path}")
	private String pluginsPath;

	@Bean
	public PluginBox p4jPluginBox() {
		P4jPluginManager manager = new P4jPluginManager(pluginsPath, context, Sets.newHashSet(pluginDescriptorFinder()));
		manager.startAsync();
		return manager;
	}

	@Bean
	public PluginDescriptorFinder pluginDescriptorFinder() {
		return new ManifestPluginDescriptorFinder();
	}
}
