/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.demo_data;

import com.epam.ta.reportportal.database.entity.StatisticsCalculationStrategy;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DemoDataFacadeFactoryImpl implements DemoDataFacadeFactory, ApplicationContextAware {

	private ApplicationContext applicationContext;

	private static final Map<StatisticsCalculationStrategy, Class<? extends DemoDataFacade>> MAPPING = ImmutableMap.<StatisticsCalculationStrategy, Class<? extends DemoDataFacade>>builder()
			.put(StatisticsCalculationStrategy.STEP_BASED, StepBasedDemoDataFacade.class)
			.put(StatisticsCalculationStrategy.TEST_BASED, TestBasedDemoDataFacade.class)
			.build();

	@Override
	public DemoDataFacade getDemoDataFacade(StatisticsCalculationStrategy strategy) {
		return applicationContext.getBean(MAPPING.getOrDefault(strategy, StepBasedDemoDataFacade.class));
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
