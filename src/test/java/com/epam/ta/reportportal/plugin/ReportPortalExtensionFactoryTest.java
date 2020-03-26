/*
 * Copyright 2020 EPAM Systems
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
import org.junit.jupiter.api.Test;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class ReportPortalExtensionFactoryTest {

	private final String RESOURCES_DIR = "resources";

	private final PluginManager pluginManager = mock(PluginManager.class);
	private final PluginWrapper pluginWrapper = mock(PluginWrapper.class);
	private final PluginDescriptor pluginDescriptor = mock(PluginDescriptor.class);

	private final AbstractAutowireCapableBeanFactory beanFactory = mock(AbstractAutowireCapableBeanFactory.class);

	private final ReportPortalExtensionFactory reportPortalExtensionFactory = new ReportPortalExtensionFactory(RESOURCES_DIR,
			pluginManager,
			beanFactory
	);

	@Test
	public void shouldReturnExistingBean() {

		when(pluginWrapper.getPluginId()).thenReturn("testId");
		when(pluginManager.whichPlugin(any())).thenReturn(pluginWrapper);
		when(beanFactory.containsSingleton(pluginWrapper.getPluginId())).thenReturn(true);
		when(beanFactory.getSingleton("testId")).thenReturn(new DummyPluginBean("testId"));

		DummyPluginBean pluginBean = (DummyPluginBean) reportPortalExtensionFactory.create(DummyPluginBean.class);

		assertEquals(pluginWrapper.getPluginId(), pluginBean.getId());
	}

	@Test
	public void shouldCreateNewBean() {

		when(pluginWrapper.getPluginId()).thenReturn("testId");
		when(pluginDescriptor.getPluginId()).thenReturn("testId");
		when(pluginWrapper.getDescriptor()).thenReturn(pluginDescriptor);
		when(pluginManager.whichPlugin(any())).thenReturn(pluginWrapper);
		when(beanFactory.containsSingleton(pluginWrapper.getPluginId())).thenReturn(false);

		DummyPluginBean pluginBean = (DummyPluginBean) reportPortalExtensionFactory.create(DummyPluginBean.class);

		assertEquals("resources/testId",
				String.valueOf(pluginBean.getInitParams().get(IntegrationTypeProperties.RESOURCES_DIRECTORY.getAttribute()))
		);

		verify(beanFactory, times(1)).autowireBean(pluginBean);
		verify(beanFactory, times(1)).initializeBean(pluginBean, pluginWrapper.getDescriptor().getPluginId());
		verify(beanFactory, times(1)).registerSingleton(pluginWrapper.getDescriptor().getPluginId(), pluginBean);
		verify(beanFactory, times(1)).registerDisposableBean(pluginWrapper.getDescriptor().getPluginId(), (DisposableBean) pluginBean);

	}

	private static class DummyPluginBean implements DisposableBean {
		private String id;

		private Map<String, Object> initParams;

		public DummyPluginBean(String id) {
			this.id = id;
		}

		public DummyPluginBean(Map<String, Object> initParams) {
			this.initParams = initParams;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public Map<String, Object> getInitParams() {
			return initParams;
		}

		public void setInitParams(Map<String, Object> initParams) {
			this.initParams = initParams;
		}

		@Override
		public void destroy() throws Exception {

		}
	}

}