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

package com.epam.ta.reportportal.core.configs;

import com.epam.ta.reportportal.core.integration.plugin.PluginLoader;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.plugin.Pf4jPluginManager;
import com.epam.ta.reportportal.plugin.ReportPortalExtensionFactory;
import com.google.common.collect.Sets;
import org.pf4j.*;
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
		Pf4jPluginManager manager = new Pf4jPluginManager(pluginsPath,
				pluginsTempPath,
				pluginLoader,
				integrationTypeRepository,
				Sets.newHashSet(pluginDescriptorFinder()),
				extensionFactory()
		);
		manager.startAsync();
		return manager;
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
