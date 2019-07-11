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

import com.epam.ta.reportportal.core.item.impl.status.*;
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

	private final FromFailedStatusChangingStrategy fromFailedStrategy;

	private final FromPassedStatusChangingStrategy fromPassedStrategy;

	private final FromInterruptedStatusChangingStrategy fromInterruptedStrategy;

	private final FromSkippedStatusChangingStrategy fromSkippedStrategy;

	@Autowired
	public ItemStatusChangingStrategyConfig(FromFailedStatusChangingStrategy fromFailedStrategy,
			FromPassedStatusChangingStrategy fromPassedStrategy, FromInterruptedStatusChangingStrategy fromInterruptedStrategy,
			FromSkippedStatusChangingStrategy fromSkippedStrategy) {
		this.fromFailedStrategy = fromFailedStrategy;
		this.fromPassedStrategy = fromPassedStrategy;
		this.fromInterruptedStrategy = fromInterruptedStrategy;
		this.fromSkippedStrategy = fromSkippedStrategy;
	}

	@Bean
	public Map<StatusEnum, StatusChangingStrategy> statusChangingStrategyMapping() {
		return ImmutableMap.<StatusEnum, StatusChangingStrategy>builder().put(StatusEnum.PASSED, fromPassedStrategy)
				.put(StatusEnum.FAILED, fromFailedStrategy)
				.put(StatusEnum.SKIPPED, fromSkippedStrategy)
				.put(StatusEnum.INTERRUPTED, fromInterruptedStrategy)
				.build();
	}
}
