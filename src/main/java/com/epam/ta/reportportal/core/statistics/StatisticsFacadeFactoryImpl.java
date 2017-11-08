/*
 * Copyright 2016 EPAM Systems
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

package com.epam.ta.reportportal.core.statistics;

import com.epam.ta.reportportal.database.entity.StatisticsCalculationStrategy;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Default implementation of factory for statistics facades
 *
 * @author Andrei Varabyeu
 */
@Service
public class StatisticsFacadeFactoryImpl implements StatisticsFacadeFactory, ApplicationContextAware {

	private ApplicationContext applicationContext;

	private static final Map<StatisticsCalculationStrategy, Class<? extends StatisticsFacade>> MAPPING = ImmutableMap.<StatisticsCalculationStrategy, Class<? extends StatisticsFacade>>builder()
			.put(StatisticsCalculationStrategy.STEP_BASED, StepBasedStatisticsFacade.class)
			.put(StatisticsCalculationStrategy.TEST_BASED, TestBasedStatisticsFacade.class)
			.put(StatisticsCalculationStrategy.ALL_ITEMS_BASED, StatisticsFacadeImpl.class)
			.build();

	@Override
	public StatisticsFacade getStatisticsFacade(StatisticsCalculationStrategy strategy) {
		return applicationContext.getBean(MAPPING.getOrDefault(strategy, StepBasedStatisticsFacade.class));
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;

	}

}
