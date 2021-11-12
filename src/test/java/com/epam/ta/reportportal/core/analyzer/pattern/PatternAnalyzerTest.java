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

package com.epam.ta.reportportal.core.analyzer.pattern;

import com.epam.ta.reportportal.commons.querygen.ConvertibleCondition;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerStatusCache;
import com.epam.ta.reportportal.core.analyzer.pattern.impl.PatternAnalyzerImpl;
import com.epam.ta.reportportal.core.analyzer.pattern.selector.PatternAnalysisSelector;
import com.epam.ta.reportportal.core.analyzer.pattern.selector.condition.PatternConditionProviderChain;
import com.epam.ta.reportportal.core.analyzer.pattern.selector.impl.RegexPatternAnalysisSelector;
import com.epam.ta.reportportal.core.analyzer.pattern.selector.impl.StringPartPatternAnalysisSelector;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.dao.IssueGroupRepository;
import com.epam.ta.reportportal.dao.PatternTemplateRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.item.issue.IssueGroup;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.pattern.PatternTemplate;
import com.epam.ta.reportportal.entity.pattern.PatternTemplateTestItemPojo;
import com.epam.ta.reportportal.entity.pattern.PatternTemplateType;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class PatternAnalyzerTest {

	private final PatternAnalysisSelector stringSelector = mock(StringPartPatternAnalysisSelector.class);
	private final PatternAnalysisSelector regexSelector = mock(RegexPatternAnalysisSelector.class);
	private final MessageBus messageBus = mock(MessageBus.class);

	private final TestItemRepository testItemRepository = mock(TestItemRepository.class);
	private final IssueGroupRepository issueGroupRepository = mock(IssueGroupRepository.class);
	private final PatternTemplateRepository patternTemplateRepository = mock(PatternTemplateRepository.class);
	private final AnalyzerStatusCache analyzerStatusCache = mock(AnalyzerStatusCache.class);
	private final PatternConditionProviderChain patternConditionProviderChain = mock(PatternConditionProviderChain.class);

	private final TaskExecutor taskExecutor = new ThreadPoolTaskExecutor() {
		@Override
		public void execute(Runnable task) {
			task.run();
		}
	};

	private final IssueGroup issueGroup = mock(IssueGroup.class);
	private final Launch launch = mock(Launch.class);

	private final Map<PatternTemplateType, PatternAnalysisSelector> analysisSelectorMapping = mock(Map.class);

	private final int batchSize = 100;

	private final PatternAnalyzer patternAnalyzer = new PatternAnalyzerImpl(batchSize,
			testItemRepository,
			patternTemplateRepository,
			analysisSelectorMapping,
			taskExecutor,
			patternConditionProviderChain,
			analyzerStatusCache,
			messageBus
	);

	@Test
	void analyzeTestItems() {
		when(issueGroupRepository.findByTestItemIssueGroup(any(TestItemIssueGroup.class))).thenReturn(issueGroup);
		when(patternTemplateRepository.findAllByProjectIdAndEnabled(launch.getProjectId(), true)).thenReturn(getPatternTemplates());

		when(launch.getId()).thenReturn(1L);

		final List<Long> itemIds = List.of(10L, 11L, 12L);

		when(patternConditionProviderChain.provideCondition(anySet())).thenReturn(Optional.of(getConvertibleCondition()));
		when(testItemRepository.selectIdsByFilter(eq(launch.getId()), any(Queryable.class), eq(batchSize), eq(0))).thenReturn(itemIds);
		when(analysisSelectorMapping.get(PatternTemplateType.STRING)).thenReturn(stringSelector);
		when(analysisSelectorMapping.get(PatternTemplateType.REGEX)).thenReturn(regexSelector);

		final List<Long> firstPatternMatch = List.of(10L, 11L);
		final List<Long> secondPatternMatch = List.of(11L, 12L);
		when(stringSelector.selectItemsByPattern(eq(launch.getId()), eq(itemIds), anyString())).thenReturn(firstPatternMatch);
		when(regexSelector.selectItemsByPattern(eq(launch.getId()), eq(itemIds), anyString())).thenReturn(secondPatternMatch);

		patternAnalyzer.analyzeTestItems(launch, Sets.newHashSet());

		final ArgumentCaptor<List<PatternTemplateTestItemPojo>> pojoCaptor = ArgumentCaptor.forClass(List.class);
		verify(patternTemplateRepository, times(2)).saveInBatch(pojoCaptor.capture());

		final List<PatternTemplateTestItemPojo> stringPatternPojos = pojoCaptor.getAllValues().get(0);
		final List<PatternTemplateTestItemPojo> regexPatternPojos = pojoCaptor.getAllValues().get(1);

		Assertions.assertEquals(firstPatternMatch,
				stringPatternPojos.stream().map(PatternTemplateTestItemPojo::getTestItemId).collect(Collectors.toList())
		);
		Assertions.assertEquals(secondPatternMatch,
				regexPatternPojos.stream().map(PatternTemplateTestItemPojo::getTestItemId).collect(Collectors.toList())
		);
	}

	private ConvertibleCondition getConvertibleCondition() {
		return FilterCondition.builder().eq(CRITERIA_ID, String.valueOf(1L)).build();
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