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

package com.epam.ta.reportportal.core.analyzer.pattern.selector.condition.impl;

import com.epam.ta.reportportal.commons.querygen.ConvertibleCondition;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.analyze.AnalyzeItemsMode;
import com.epam.ta.reportportal.core.analyzer.pattern.selector.condition.PatternConditionProvider;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Optional;
import java.util.Set;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public abstract class AbstractPatternConditionProvider implements PatternConditionProvider {

	private final AnalyzeItemsMode analyzeItemsMode;

	public AbstractPatternConditionProvider(AnalyzeItemsMode analyzeItemsMode) {
		this.analyzeItemsMode = analyzeItemsMode;
	}

	@Override
	public Optional<ConvertibleCondition> provideCondition(Set<AnalyzeItemsMode> analyzeItemsModes) {
		if (isModificationRequired(analyzeItemsModes)) {
			return Optional.of(provideCondition());
		}
		return Optional.empty();
	}

	protected boolean isModificationRequired(Set<AnalyzeItemsMode> analyzeItemsModes) {
		return CollectionUtils.isNotEmpty(analyzeItemsModes) && analyzeItemsModes.contains(analyzeItemsMode);
	}

	protected abstract ConvertibleCondition provideCondition();
}
