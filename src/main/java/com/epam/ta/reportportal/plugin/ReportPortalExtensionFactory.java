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

package com.epam.ta.reportportal.plugin;

import org.pf4j.DefaultExtensionFactory;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class ReportPortalExtensionFactory extends DefaultExtensionFactory {

	private final PluginManager pluginManager;
	private final AbstractAutowireCapableBeanFactory beanFactory;

	public ReportPortalExtensionFactory(PluginManager pluginManager, AutowireCapableBeanFactory context) {
		this.pluginManager = pluginManager;
		this.beanFactory = (AbstractAutowireCapableBeanFactory) context;
	}

	@Override
	public Object create(Class<?> extensionClass) {
		PluginWrapper pluginWrapper = pluginManager.whichPlugin(extensionClass);
		if (beanFactory.containsSingleton(pluginWrapper.getPluginId())) {
			return beanFactory.getSingleton(pluginWrapper.getPluginId());
		} else {
			return createExtension(extensionClass, pluginWrapper);
		}
	}

	private Object createExtension(Class<?> extensionClass, PluginWrapper pluginWrapper) {
		Object plugin = super.create(extensionClass);
		beanFactory.autowireBean(plugin);
		beanFactory.initializeBean(plugin, pluginWrapper.getDescriptor().getPluginId());
		beanFactory.registerSingleton(pluginWrapper.getDescriptor().getPluginId(), plugin);
		return plugin;
	}
}
