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

import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.item.impl.IssueTypeHandler;
import com.epam.ta.reportportal.core.item.impl.status.*;
import com.epam.ta.reportportal.dao.IssueEntityRepository;
import com.epam.ta.reportportal.dao.ItemAttributeRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Configuration
public class ItemStatusChangingStrategyConfig {

	private final TestItemRepository testItemRepository;

	private final ItemAttributeRepository itemAttributeRepository;

	private final IssueTypeHandler issueTypeHandler;

	private final IssueEntityRepository issueEntityRepository;

	private final LaunchRepository launchRepository;

	private final MessageBus messageBus;

	@Autowired
	public ItemStatusChangingStrategyConfig(TestItemRepository testItemRepository, ItemAttributeRepository itemAttributeRepository,
			IssueTypeHandler issueTypeHandler, IssueEntityRepository issueEntityRepository, LaunchRepository launchRepository,
			MessageBus messageBus) {
		this.testItemRepository = testItemRepository;
		this.itemAttributeRepository = itemAttributeRepository;
		this.issueTypeHandler = issueTypeHandler;
		this.issueEntityRepository = issueEntityRepository;
		this.launchRepository = launchRepository;
		this.messageBus = messageBus;
	}

	@Bean
	public Map<StatusEnum, StatusChangingStrategy> statusChangingStrategyMapping() {
		return ImmutableMap.<StatusEnum, StatusChangingStrategy>builder().put(StatusEnum.PASSED,
				new FromPassedStatusChangingStrategy(testItemRepository,
						itemAttributeRepository,
						issueTypeHandler,
						issueEntityRepository,
						launchRepository,
						messageBus
				)
		)
				.put(
						StatusEnum.FAILED,
						new FromFailedStatusChangingStrategy(testItemRepository,
								itemAttributeRepository,
								issueTypeHandler,
								issueEntityRepository,
								launchRepository,
								messageBus
						)
				)
				.put(
						StatusEnum.SKIPPED,
						new FromSkippedStatusChangingStrategy(testItemRepository,
								itemAttributeRepository,
								issueTypeHandler,
								issueEntityRepository,
								launchRepository,
								messageBus
						)
				)
				.put(
						StatusEnum.INTERRUPTED,
						new FromInterruptedStatusChangingStrategy(testItemRepository,
								itemAttributeRepository,
								issueTypeHandler,
								issueEntityRepository,
								launchRepository,
								messageBus
						)
				)
				.build();
	}
}
