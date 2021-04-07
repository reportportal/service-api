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

package com.epam.ta.reportportal.core.analyzer.pattern.selector.impl;

import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class StringPartPatternAnalysisSelector extends AbstractPatternAnalysisSelector {

	@Autowired
	public StringPartPatternAnalysisSelector(TestItemRepository testItemRepository) {
		super(testItemRepository);
	}

	@Override
	protected List<Long> getItemsWithMatches(String pattern, Set<Long> itemIds) {
		return testItemRepository.selectIdsByStringLogMessage(itemIds,
				LogLevel.ERROR_INT,
				pattern
		);
	}

	@Override
	protected List<Long> getItemsWithNestedStepsMatches(Long launchId, String pattern, List<Long> itemsWithNestedSteps) {
		return testItemRepository.selectIdsUnderByStringLogMessage(launchId,
				itemsWithNestedSteps,
				LogLevel.ERROR_INT,
				pattern
		);
	}
}
