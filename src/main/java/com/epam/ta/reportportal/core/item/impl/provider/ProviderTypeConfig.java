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

package com.epam.ta.reportportal.core.item.impl.provider;

import com.epam.ta.reportportal.core.item.impl.provider.impl.CumulativeTestItemDataProviderImpl;
import com.epam.ta.reportportal.core.item.impl.provider.impl.LaunchDataProviderHandlerImpl;
import com.epam.ta.reportportal.core.item.impl.provider.impl.MaterializedWidgetProviderHandlerImpl;
import com.epam.ta.reportportal.entity.widget.WidgetType;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Configuration
public class ProviderTypeConfig {

	private ApplicationContext applicationContext;

	@Autowired
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Bean("testItemDataProviders")
	public Map<DataProviderType, DataProviderHandler> testItemDataProviders() {
		return ImmutableMap.<DataProviderType, DataProviderHandler>builder().put(DataProviderType.WIDGET_BASED,
				applicationContext.getBean(MaterializedWidgetProviderHandlerImpl.class)
		)
				.put(DataProviderType.LAUNCH_BASED, applicationContext.getBean(LaunchDataProviderHandlerImpl.class))
				.put(DataProviderType.FILTER_BASED, applicationContext.getBean(FilterDataProviderImpl.class))
				.build();
	}

	@Bean("testItemWidgetDataProviders")
	public Map<WidgetType, DataProviderHandler> testItemWidgetDataProviders() {
		return ImmutableMap.<WidgetType, DataProviderHandler>builder().put(WidgetType.CUMULATIVE,
				applicationContext.getBean(CumulativeTestItemDataProviderImpl.class)
		).build();
	}

}
