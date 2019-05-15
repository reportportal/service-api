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

package com.epam.ta.reportportal.core.pattern;

import com.epam.ta.reportportal.core.pattern.impl.PatternAnalyzerImpl;
import com.epam.ta.reportportal.core.pattern.selector.PatternAnalysisSelector;
import com.epam.ta.reportportal.core.pattern.selector.impl.StringPartPatternAnalysisSelector;
import com.epam.ta.reportportal.dao.IssueGroupRepository;
import com.epam.ta.reportportal.dao.PatternTemplateRepository;
import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.item.issue.IssueGroup;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.pattern.PatternTemplate;
import com.epam.ta.reportportal.entity.pattern.PatternTemplateTestItemPojo;
import com.epam.ta.reportportal.entity.pattern.PatternTemplateType;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.TaskExecutor;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class PatternAnalyzerTest {

	private final PatternAnalysisSelector stringSelector = mock(StringPartPatternAnalysisSelector.class);

	private final IssueGroupRepository issueGroupRepository = mock(IssueGroupRepository.class);
	private final PatternTemplateRepository patternTemplateRepository = mock(PatternTemplateRepository.class);

	private final TaskExecutor taskExecutor = mock(TaskExecutor.class);

	private final IssueGroup issueGroup = mock(IssueGroup.class);
	private final Launch launch = mock(Launch.class);

	private final Map<PatternTemplateType, PatternAnalysisSelector> analysisSelectorMapping = mock(Map.class);
	private final PatternAnalyzer patternAnalyzer = new PatternAnalyzerImpl(issueGroupRepository,
			patternTemplateRepository,
			analysisSelectorMapping,
			taskExecutor, messageBus
	);

	@Test
	void analyzeTestItems() {
		when(issueGroupRepository.findByTestItemIssueGroup(any(TestItemIssueGroup.class))).thenReturn(issueGroup);
		when(patternTemplateRepository.findAllByProjectIdAndEnabled(1L, true)).thenReturn(getPatternTemplates());

		when(analysisSelectorMapping.get(PatternTemplateType.STRING)).thenReturn(stringSelector);

		when(stringSelector.selectItemsByPattern(any(Long.class), any(IssueGroup.class), any(PatternTemplate.class))).thenReturn(
				getPatternTemplateTestItemPojos(1L));
		when(stringSelector.selectItemsByPattern(any(Long.class), any(IssueGroup.class), any(PatternTemplate.class))).thenReturn(
				getPatternTemplateTestItemPojos(2L));
		doNothing().when(taskExecutor).execute(any());

		patternAnalyzer.analyzeTestItems(launch);
	}

	private List<PatternTemplate> getPatternTemplates() {

		return Lists.newArrayList(getPatternTemplate(1L, "name", "value", PatternTemplateType.STRING),
				getPatternTemplate(2L, "name1", "value1", PatternTemplateType.REGEX)
		);
	}

	private PatternTemplate getPatternTemplate(Long id, String name, String value, PatternTemplateType type) {
		PatternTemplate patternTemplate = new PatternTemplate();
		patternTemplate.setId(id);
		patternTemplate.setName(name);
		patternTemplate.setValue(value);
		patternTemplate.setEnabled(true);
		patternTemplate.setTemplateType(type);
		patternTemplate.setProjectId(1L);
		return patternTemplate;
	}

	private List<PatternTemplateTestItemPojo> getPatternTemplateTestItemPojos(Long patternId) {

		return Lists.newArrayList(new PatternTemplateTestItemPojo(patternId, 1L), new PatternTemplateTestItemPojo(patternId, 2L));
	}
}