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

import com.epam.ta.reportportal.core.item.merge.LaunchMergeStrategy;

import java.util.Map;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class LaunchMergeFactory {

	private Map<MergeStrategyType, LaunchMergeStrategy> mergeStrategyMapping;

	public LaunchMergeFactory(Map<MergeStrategyType, LaunchMergeStrategy> mergeStrategyMapping) {
		this.mergeStrategyMapping = mergeStrategyMapping;
	}

	public LaunchMergeStrategy getLaunchMergeStrategy(MergeStrategyType type) {
		return mergeStrategyMapping.get(type);
	}
}
