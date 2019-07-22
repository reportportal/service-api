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

import com.epam.ta.reportportal.core.integration.plugin.PluginLoader;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.plugin.Pf4jExtension;
import com.epam.ta.reportportal.plugin.Pf4jPluginManager;
import com.epam.ta.reportportal.plugin.ReportPortalExtensionFactory;
import com.google.common.collect.Sets;
import org.pf4j.ExtensionFactory;
import org.pf4j.ManifestPluginDescriptorFinder;
import org.pf4j.PluginDescriptorFinder;
import org.pf4j.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PluginConfiguration {

	@Autowired
	private AutowireCapableBeanFactory context;

	@Autowired
	private PluginLoader pluginLoader;

	@Autowired
	private IntegrationTypeRepository integrationTypeRepository;

	@Value("${rp.plugins.path}")
	private String pluginsPath;

	@Value("${rp.plugins.temp.path}")
	private String pluginsTempPath;

	@Bean
	public Pf4jPluginBox pf4jPluginBox() {
		Pf4jPluginManager manager = new Pf4jPluginManager(pluginsTempPath, pluginLoader, integrationTypeRepository, pluginManager());
		manager.startAsync();
		return manager;
	}

	@Bean
	public PluginManager pluginManager() {
		return new Pf4jExtension(pluginsPath, extensionFactory(), Sets.newHashSet(pluginDescriptorFinder()));
	}

	@Bean
	public ExtensionFactory extensionFactory() {
		return new ReportPortalExtensionFactory(context);
	}

	@Bean
	public PluginDescriptorFinder pluginDescriptorFinder() {
		return new ManifestPluginDescriptorFinder();
	}

}
