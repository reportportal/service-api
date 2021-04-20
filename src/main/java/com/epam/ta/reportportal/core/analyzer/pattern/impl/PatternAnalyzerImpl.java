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

package com.epam.ta.reportportal.core.analyzer.pattern.impl;

import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.ConvertibleCondition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerStatusCache;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.analyze.AnalyzeItemsMode;
import com.epam.ta.reportportal.core.analyzer.pattern.PatternAnalyzer;
import com.epam.ta.reportportal.core.analyzer.pattern.selector.PatternAnalysisSelector;
import com.epam.ta.reportportal.core.analyzer.pattern.selector.condition.PatternConditionProviderChain;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.PatternMatchedEvent;
import com.epam.ta.reportportal.dao.PatternTemplateRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.pattern.PatternTemplate;
import com.epam.ta.reportportal.entity.pattern.PatternTemplateTestItemPojo;
import com.epam.ta.reportportal.entity.pattern.PatternTemplateType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.converters.PatternTemplateConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.activity.PatternTemplateActivityResource;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_PATTERN_TEMPLATE_NAME;
import static com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerStatusCache.PATTERN_ANALYZER_KEY;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class PatternAnalyzerImpl implements PatternAnalyzer {

	public static final Logger LOGGER = LoggerFactory.getLogger(PatternAnalyzerImpl.class);

	private final Integer batchSize;

	private final TestItemRepository testItemRepository;
	private final PatternTemplateRepository patternTemplateRepository;
	private final PatternConditionProviderChain patternConditionProviderChain;

	private final Map<PatternTemplateType, PatternAnalysisSelector> patternAnalysisSelectorMapping;

	private final TaskExecutor patternAnalysisTaskExecutor;

	private final AnalyzerStatusCache analyzerStatusCache;

	private final MessageBus messageBus;

	@Autowired
	public PatternAnalyzerImpl(@Value("${rp.environment.variable.pattern-analysis.batch-size}") Integer batchSize,
			TestItemRepository testItemRepository, PatternTemplateRepository patternTemplateRepository,
			@Qualifier("patternAnalysisSelectorMapping") Map<PatternTemplateType, PatternAnalysisSelector> patternAnalysisSelectorMapping,
			TaskExecutor patternAnalysisTaskExecutor, PatternConditionProviderChain patternConditionProviderChain,
			AnalyzerStatusCache analyzerStatusCache, MessageBus messageBus) {
		this.batchSize = batchSize;
		this.testItemRepository = testItemRepository;
		this.patternTemplateRepository = patternTemplateRepository;
		this.patternAnalysisSelectorMapping = patternAnalysisSelectorMapping;
		this.patternAnalysisTaskExecutor = patternAnalysisTaskExecutor;
		this.patternConditionProviderChain = patternConditionProviderChain;
		this.analyzerStatusCache = analyzerStatusCache;
		this.messageBus = messageBus;
	}

	@Override
	public void analyzeTestItems(Launch launch, Set<AnalyzeItemsMode> analyzeModes) {
		BusinessRule.expect(analyzerStatusCache.getStartedAnalyzers(launch.getId()), not(started -> started.contains(PATTERN_ANALYZER_KEY)))
				.verify(ErrorType.PATTERN_ANALYSIS_ERROR, "Pattern analysis is still in progress.");

		analyzerStatusCache.analyzeStarted(PATTERN_ANALYZER_KEY, launch.getId(), launch.getProjectId());
		patternAnalysisTaskExecutor.execute(() -> {
			try {
				final ConvertibleCondition itemCondition = getItemCondition(analyzeModes);
				patternTemplateRepository.findAllByProjectIdAndEnabled(launch.getProjectId(), true)
						.forEach(patternTemplate -> analyze(launch, itemCondition, patternTemplate));
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			} finally {
				analyzerStatusCache.analyzeFinished(PATTERN_ANALYZER_KEY, launch.getId());
			}
		});

	}

	private ConvertibleCondition getItemCondition(Set<AnalyzeItemsMode> analyzeModes) {
		return patternConditionProviderChain.provideCondition(analyzeModes)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PATTERN_ANALYSIS_ERROR, "Unable to resolve item search condition"));
	}

	private void analyze(Launch launch, ConvertibleCondition itemCondition, PatternTemplate patternTemplate) {
		final Filter filter = createItemFilter(itemCondition, patternTemplate.getName());
		int offset = 0;
		List<Long> itemIds = testItemRepository.selectIdsByFilter(launch.getId(), filter, batchSize, offset);
		while (CollectionUtils.isNotEmpty(itemIds)) {
			final List<Long> matchedIds = attachToPatternTemplate(launch, patternTemplate, itemIds);
			offset += itemIds.size() - matchedIds.size();
			itemIds = testItemRepository.selectIdsByFilter(launch.getId(), filter, batchSize, offset);
		}

	}

	private List<Long> attachToPatternTemplate(Launch launch, PatternTemplate patternTemplate, List<Long> itemIds) {
		final List<Long> matchedIds = filterItems(launch, patternTemplate, itemIds);
		final List<PatternTemplateTestItemPojo> patternTemplateTestItems = convertToPojo(patternTemplate, matchedIds);
		patternTemplateRepository.saveInBatch(patternTemplateTestItems);
		publishEvents(patternTemplate, patternTemplateTestItems);
		return matchedIds;
	}

	private List<Long> filterItems(Launch launch, PatternTemplate patternTemplate, List<Long> itemIds) {
		final PatternAnalysisSelector patternAnalysisSelector = patternAnalysisSelectorMapping.get(patternTemplate.getTemplateType());
		return patternAnalysisSelector.selectItemsByPattern(launch.getId(), itemIds, patternTemplate.getValue());
	}

	private Filter createItemFilter(ConvertibleCondition commonItemCondition, String patternTemplateName) {
		return Filter.builder()
				.withTarget(TestItem.class)
				.withCondition(commonItemCondition)
				.withCondition(FilterCondition.builder()
						.withCondition(Condition.ANY)
						.withNegative(true)
						.withSearchCriteria(CRITERIA_PATTERN_TEMPLATE_NAME)
						.withValue(patternTemplateName)
						.build())
				.build();
	}

	private List<PatternTemplateTestItemPojo> convertToPojo(PatternTemplate patternTemplate, List<Long> itemIds) {
		return itemIds.stream()
				.map(itemId -> new PatternTemplateTestItemPojo(patternTemplate.getId(), itemId))
				.collect(Collectors.toList());
	}

	private void publishEvents(PatternTemplate patternTemplate, List<PatternTemplateTestItemPojo> patternTemplateTestItems) {
		final PatternTemplateActivityResource patternTemplateActivityResource = PatternTemplateConverter.TO_ACTIVITY_RESOURCE.apply(
				patternTemplate);
		patternTemplateTestItems.forEach(patternItem -> {
			PatternMatchedEvent patternMatchedEvent = new PatternMatchedEvent(patternItem.getPatternTemplateId(),
					patternItem.getTestItemId(),
					patternTemplateActivityResource
			);
			messageBus.publishActivity(patternMatchedEvent);
		});
	}

}
