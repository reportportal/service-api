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

package com.epam.ta.reportportal.core.item.impl.merge.strategy;

import com.epam.ta.reportportal.core.item.merge.StatisticsCalculationStrategy;

import java.util.Map;

/**
 * @author Ivan Budaev
 */
public class StatisticsCalculationFactory {

	private final Map<MergeStrategyType, StatisticsCalculationStrategy> calculationStrategyMapping;

	public StatisticsCalculationFactory(Map<MergeStrategyType, StatisticsCalculationStrategy> calculationStrategyMapping) {
		this.calculationStrategyMapping = calculationStrategyMapping;
	}

	public StatisticsCalculationStrategy getStrategy(MergeStrategyType type) {
		return this.calculationStrategyMapping.get(type);
	}
}
