/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.configs;

import com.epam.ta.reportportal.core.item.impl.merge.strategy.*;
import com.epam.ta.reportportal.core.item.merge.MergeStrategy;
import com.epam.ta.reportportal.core.item.merge.StatisticsCalculationStrategy;
import com.epam.ta.reportportal.dao.TestItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static java.util.Collections.singletonMap;

/**
 * @author Ivan Budaev
 */
@Configuration
public class MergeStrategyConfig {

	private final TestItemRepository testItemRepository;

	@Autowired
	public MergeStrategyConfig(TestItemRepository testItemRepository) {
		this.testItemRepository = testItemRepository;
	}

	@Bean
	public Map<MergeStrategyType, MergeStrategy> mergeStrategyMapping() {
		return singletonMap(MergeStrategyType.DEEP, new DeepMergeStrategy(testItemRepository));
	}

	@Bean
	public Map<MergeStrategyType, StatisticsCalculationStrategy> statisticsCalculationStrategyMaping() {
		return singletonMap(MergeStrategyType.BASIC, new BasicStatisticsCalculationStrategy());
	}

	@Bean
	public MergeStrategyFactory mergeStrategyFactory() {
		return new MergeStrategyFactory(mergeStrategyMapping());
	}

	@Bean
	public StatisticsCalculationFactory statisticsCalculationFactory() {
		return new StatisticsCalculationFactory(statisticsCalculationStrategyMaping());
	}
}
