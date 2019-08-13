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

import com.epam.ta.reportportal.commons.querygen.*;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerStatusCache;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.AnalyzeItemsMode;
import com.epam.ta.reportportal.core.analyzer.pattern.PatternAnalyzer;
import com.epam.ta.reportportal.core.analyzer.pattern.selector.PatternAnalysisSelector;
import com.epam.ta.reportportal.core.analyzer.pattern.selector.condition.PatternConditionProviderChain;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.PatternMatchedEvent;
import com.epam.ta.reportportal.dao.PatternTemplateRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.pattern.PatternTemplateTestItemPojo;
import com.epam.ta.reportportal.entity.pattern.PatternTemplateType;
import com.epam.ta.reportportal.ws.converter.converters.PatternTemplateConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.activity.PatternTemplateActivityResource;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_LAUNCH_ID;
import static com.epam.ta.reportportal.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_PATTERN_TEMPLATE_NAME;
import static com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerStatusCache.PATTERN_ANALYZER_KEY;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class PatternAnalyzerImpl implements PatternAnalyzer {

	public static final Logger LOGGER = LoggerFactory.getLogger(PatternAnalyzerImpl.class);

	private final PatternTemplateRepository patternTemplateRepository;
	private final PatternConditionProviderChain patternConditionProviderChain;

	private final Map<PatternTemplateType, PatternAnalysisSelector> patternAnalysisSelectorMapping;

	private final TaskExecutor patternAnalysisTaskExecutor;

	private final AnalyzerStatusCache analyzerStatusCache;

	private final MessageBus messageBus;

	@Autowired
	public PatternAnalyzerImpl(PatternTemplateRepository patternTemplateRepository,
			@Qualifier("patternAnalysisSelectorMapping") Map<PatternTemplateType, PatternAnalysisSelector> patternAnalysisSelectorMapping,
			TaskExecutor patternAnalysisTaskExecutor, PatternConditionProviderChain patternConditionProviderChain,
			AnalyzerStatusCache analyzerStatusCache, MessageBus messageBus) {
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

		try {
			analyzerStatusCache.analyzeStarted(PATTERN_ANALYZER_KEY, launch.getId(), launch.getProjectId());

			ConvertibleCondition commonItemCondition = createCommonItemCondition(launch.getId(), analyzeModes);
			patternTemplateRepository.findAllByProjectIdAndEnabled(launch.getProjectId(), true)
					.forEach(patternTemplate -> patternAnalysisTaskExecutor.execute(() -> {
						Filter filter = createItemFilter(commonItemCondition, patternTemplate.getName());
						List<PatternTemplateTestItemPojo> patternTemplateTestItems = patternAnalysisSelectorMapping.get(patternTemplate.getTemplateType())
								.selectItemsByPattern(filter, patternTemplate);
						patternTemplateRepository.saveInBatch(patternTemplateTestItems);

						PatternTemplateActivityResource patternTemplateActivityResource = PatternTemplateConverter.TO_ACTIVITY_RESOURCE.apply(
								patternTemplate);
						patternTemplateTestItems.forEach(patternItem -> {
							PatternMatchedEvent patternMatchedEvent = new PatternMatchedEvent(patternItem.getPatternTemplateId(),
									patternItem.getTestItemId(),
									patternTemplateActivityResource
							);
							messageBus.publishActivity(patternMatchedEvent);
						});

					}));
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			analyzerStatusCache.analyzeFinished(PATTERN_ANALYZER_KEY, launch.getId());
		}

	}

	private ConvertibleCondition createCommonItemCondition(Long launchId, Set<AnalyzeItemsMode> analyzeModes) {
		CompositeFilterCondition testItemCondition = new CompositeFilterCondition(Lists.newArrayList(FilterCondition.builder()
				.eq(CRITERIA_LAUNCH_ID, String.valueOf(launchId))
				.build()));
		patternConditionProviderChain.provideCondition(analyzeModes)
				.ifPresent(condition -> testItemCondition.getConditions().add(condition));

		return testItemCondition;
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

}
