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

import com.epam.reportportal.extension.common.IntegrationTypeProperties;
import org.pf4j.DefaultExtensionFactory;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class ReportPortalExtensionFactory extends DefaultExtensionFactory {

	private final String resourcesDir;
	private final PluginManager pluginManager;
	private final AbstractAutowireCapableBeanFactory beanFactory;

	public ReportPortalExtensionFactory(String resourcesDir, PluginManager pluginManager, AutowireCapableBeanFactory context) {
		this.resourcesDir = resourcesDir;
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
		Map<String, Object> initParams = getInitParams(pluginWrapper);
		Object plugin = createPlugin(extensionClass, initParams);
		beanFactory.autowireBean(plugin);
		beanFactory.initializeBean(plugin, pluginWrapper.getDescriptor().getPluginId());
		beanFactory.registerSingleton(pluginWrapper.getDescriptor().getPluginId(), plugin);
		if (DisposableBean.class.isAssignableFrom(extensionClass)) {
			beanFactory.registerDisposableBean(pluginWrapper.getDescriptor().getPluginId(), (DisposableBean) plugin);
		}
		return plugin;
	}

	private Object createPlugin(Class<?> extensionClass, Map<String, Object> initParams) {
		try {
			return extensionClass.getDeclaredConstructor(Map.class).newInstance(initParams);
		} catch (Exception ex) {
			return super.create(extensionClass);
		}
	}

	private Map<String, Object> getInitParams(PluginWrapper pluginWrapper) {
		Map<String, Object> initParams = new HashMap<>();
		initParams.put(IntegrationTypeProperties.RESOURCES_DIRECTORY.getAttribute(),
				Paths.get(resourcesDir, pluginWrapper.getPluginId())
		);
		return initParams;
	}
}
