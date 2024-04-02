/*
 * Copyright 2023 EPAM Systems
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

package com.epam.ta.reportportal.core.analyzer.pattern.service.impl;

import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerStatusCache.PATTERN_ANALYZER_KEY;

import com.epam.ta.reportportal.commons.querygen.ConvertibleCondition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.reportportal.rules.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerStatusCache;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.analyze.AnalyzeItemsMode;
import com.epam.ta.reportportal.core.analyzer.pattern.handler.ItemsPatternsAnalyzer;
import com.epam.ta.reportportal.core.analyzer.pattern.selector.condition.PatternConditionProviderChain;
import com.epam.ta.reportportal.core.analyzer.pattern.service.LaunchPatternAnalyzer;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.reporting.ErrorType;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class LaunchPatternAnalyzerImpl implements LaunchPatternAnalyzer {

  public static final Logger LOGGER = LoggerFactory.getLogger(LaunchPatternAnalyzerImpl.class);

  private final Integer batchSize;

  private final TestItemRepository testItemRepository;
  private final PatternConditionProviderChain patternConditionProviderChain;

  private final AnalyzerStatusCache analyzerStatusCache;

  private final ItemsPatternsAnalyzer itemsPatternsAnalyzer;

  @Autowired
  public LaunchPatternAnalyzerImpl(
      @Value("${rp.environment.variable.pattern-analysis.batch-size}") Integer batchSize,
      TestItemRepository testItemRepository,
      PatternConditionProviderChain patternConditionProviderChain,
      AnalyzerStatusCache analyzerStatusCache, ItemsPatternsAnalyzer itemsPatternsAnalyzer) {
    this.batchSize = batchSize;
    this.testItemRepository = testItemRepository;
    this.patternConditionProviderChain = patternConditionProviderChain;
    this.analyzerStatusCache = analyzerStatusCache;
    this.itemsPatternsAnalyzer = itemsPatternsAnalyzer;
  }

  @Override
  public void analyzeLaunch(Launch launch, Set<AnalyzeItemsMode> analyzeModes) {
    BusinessRule.expect(analyzerStatusCache.getStartedAnalyzers(launch.getId()),
            not(started -> started.contains(PATTERN_ANALYZER_KEY)))
        .verify(ErrorType.PATTERN_ANALYSIS_ERROR, "Pattern analysis is still in progress.");
    analyzerStatusCache.analyzeStarted(PATTERN_ANALYZER_KEY, launch.getId(), launch.getProjectId());
    try {
      analyze(launch, buildItemsCondition(analyzeModes));
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
    } finally {
      analyzerStatusCache.analyzeFinished(PATTERN_ANALYZER_KEY, launch.getId());
    }
  }

  private void analyze(Launch launch, ConvertibleCondition itemCondition) {
    final Filter filter = createItemFilter(itemCondition);
    int offset = 0;
    List<Long> itemIds = testItemRepository.selectIdsByFilter(launch.getId(), filter, batchSize,
        offset);
    while (CollectionUtils.isNotEmpty(itemIds)) {
      itemsPatternsAnalyzer.analyze(launch.getProjectId(), launch.getId(), itemIds);
      offset += itemIds.size();
      itemIds = testItemRepository.selectIdsByFilter(launch.getId(), filter, batchSize, offset);
    }
    notifyAnalysisFinished(launch.getProjectId(), launch.getId());
  }

  private void notifyAnalysisFinished(long projectId, long launchId) {
    itemsPatternsAnalyzer.analyze(projectId, launchId, Collections.emptyList());
  }

  private ConvertibleCondition buildItemsCondition(Set<AnalyzeItemsMode> analyzeModes) {
    return patternConditionProviderChain.provideCondition(analyzeModes)
        .orElseThrow(() -> new ReportPortalException(ErrorType.PATTERN_ANALYSIS_ERROR,
            "Unable to resolve item search condition"));
  }

  private Filter createItemFilter(ConvertibleCondition commonItemCondition) {
    return Filter.builder()
        .withTarget(TestItem.class)
        .withCondition(commonItemCondition)
        .build();
  }
}
