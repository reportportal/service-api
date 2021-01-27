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

import com.epam.ta.reportportal.core.item.identity.TestItemUniqueIdGenerator;
import com.epam.ta.reportportal.core.item.impl.merge.strategy.*;
import com.epam.ta.reportportal.core.item.merge.LaunchMergeStrategy;
import com.epam.ta.reportportal.core.item.merge.StatisticsCalculationStrategy;
import com.epam.ta.reportportal.dao.AttachmentRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static java.util.Collections.singletonMap;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Configuration
public class MergeStrategyConfig {

	private final TestItemRepository testItemRepository;

	private final LaunchRepository launchRepository;

	private final TestItemUniqueIdGenerator testItemUniqueIdGenerator;

	private final LogRepository logRepository;

	private final AttachmentRepository attachmentRepository;

	@Autowired
	public MergeStrategyConfig(TestItemRepository testItemRepository, LaunchRepository launchRepository,
			TestItemUniqueIdGenerator testItemUniqueIdGenerator, LogRepository logRepository, AttachmentRepository attachmentRepository) {
		this.testItemRepository = testItemRepository;
		this.launchRepository = launchRepository;
		this.testItemUniqueIdGenerator = testItemUniqueIdGenerator;
		this.logRepository = logRepository;
		this.attachmentRepository = attachmentRepository;
	}

	@Bean
	public Map<MergeStrategyType, StatisticsCalculationStrategy> statisticsCalculationStrategyMaping() {
		return singletonMap(MergeStrategyType.BASIC, new BasicStatisticsCalculationStrategy());
	}

	@Bean
	public StatisticsCalculationFactory statisticsCalculationFactory() {
		return new StatisticsCalculationFactory(statisticsCalculationStrategyMaping());
	}

	@Bean
	public Map<MergeStrategyType, LaunchMergeStrategy> launchMergeStrategyMapping() {
		return ImmutableMap.<MergeStrategyType, LaunchMergeStrategy>builder().put(MergeStrategyType.BASIC,
				new BasicLaunchMergeStrategy(launchRepository,
						testItemRepository,
						logRepository,
						attachmentRepository,
						testItemUniqueIdGenerator,
						statisticsCalculationFactory()
				)
		)
				.put(MergeStrategyType.DEEP,
						new DeepLaunchMergeStrategy(launchRepository,
								testItemRepository,
								logRepository,
								attachmentRepository,
								testItemUniqueIdGenerator
						)
				)
				.build();
	}

	@Bean
	public LaunchMergeFactory launchMergeFactory() {
		return new LaunchMergeFactory(launchMergeStrategyMapping());
	}
}
