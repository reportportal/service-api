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

import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.analyzer.pattern.selector.PatternAnalysisSelector;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.pattern.PatternTemplate;
import com.epam.ta.reportportal.entity.pattern.PatternTemplateTestItemPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class StringPartPatternAnalysisSelector implements PatternAnalysisSelector {

	private final TestItemRepository testItemRepository;

	@Autowired
	public StringPartPatternAnalysisSelector(TestItemRepository testItemRepository) {
		this.testItemRepository = testItemRepository;
	}

	@Override
	public List<PatternTemplateTestItemPojo> selectItemsByPattern(Queryable filter, PatternTemplate patternTemplate) {
		return testItemRepository.selectIdsByStringPatternMatchedLogMessage(filter, LogLevel.ERROR.toInt(), patternTemplate.getValue())
				.stream()
				.map(itemId -> new PatternTemplateTestItemPojo(patternTemplate.getId(), itemId))
				.collect(Collectors.toList());
	}
}
