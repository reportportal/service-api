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

import com.epam.ta.reportportal.core.integration.plugin.file.validator.ExtensionValidator;
import com.epam.ta.reportportal.core.integration.plugin.file.validator.FileValidator;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.entity.plugin.PluginFileExtension;
import com.epam.ta.reportportal.plugin.Pf4jPluginManager;
import com.epam.ta.reportportal.plugin.ReportPortalExtensionFactory;
import org.pf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Configuration
public class PluginConfiguration {

	@Autowired
	private AutowireCapableBeanFactory context;

	@Value("${rp.plugins.path}")
	private String pluginsPath;

	@Value("${rp.plugins.resources.path}")
	private String pluginsResourcesPath;

	@Bean
	public Pf4jPluginBox pf4jPluginBox() throws IOException {
		return new Pf4jPluginManager(pluginManager(), context, applicationEventPublisher);
	}

	@Bean
	public PluginManager pluginManager() {

		return new DefaultPluginManager(Paths.get(pluginsPath)) {
			@Override
			protected PluginDescriptorFinder createPluginDescriptorFinder() {
				return pluginDescriptorFinder();
			}

			@Override
			protected ExtensionFactory createExtensionFactory() {
				return new ReportPortalExtensionFactory(pluginsResourcesPath, this, context);
			}

			@Override
			protected ExtensionFinder createExtensionFinder() {
				RpExtensionFinder extensionFinder = new RpExtensionFinder(this);
				addPluginStateListener(extensionFinder);
				return extensionFinder;
			}

			class RpExtensionFinder extends DefaultExtensionFinder {

				private RpExtensionFinder(PluginManager pluginManager) {
					super(pluginManager);
					finders.clear();
					finders.add(new LegacyExtensionFinder(pluginManager) {
						@Override
						public Set<String> findClassNames(String pluginId) {
							return ofNullable(super.findClassNames(pluginId)).orElseGet(Collections::emptySet);
						}
					});
				}
			}
		};
	}

	@Bean
	public PluginDescriptorFinder pluginDescriptorFinder() {
		return new ManifestPluginDescriptorFinder();
	}

	@Bean
	public FileValidator extensionValidator() {
		final Set<String> allowedExtensions = Arrays.stream(PluginFileExtension.values())
				.map(v -> v.getExtension().split("\\.")[1])
				.collect(Collectors.toSet());
		return new ExtensionValidator(allowedExtensions);
	}

}
