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

package com.epam.ta.reportportal.core.analyzer.pattern.handler.proxy;

import static com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerStatusCache.PATTERN_ANALYZER_KEY;
import static com.epam.ta.reportportal.core.analyzer.config.PatternAnalysisRabbitConfiguration.PATTERN_ANALYSIS_QUEUE;

import com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerStatusCache;
import com.epam.ta.reportportal.core.analyzer.pattern.handler.ItemsPatternsAnalyzer;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Consumes items for pattern analysis from the queue
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Component
public class ItemsPatternAnalyzeConsumer {

  private final ItemsPatternsAnalyzer itemsPatternsAnalyzer;

  private final AnalyzerStatusCache analyzerStatusCache;

  public ItemsPatternAnalyzeConsumer(
      @Qualifier("itemsPatternAnalyzerImpl") ItemsPatternsAnalyzer itemsPatternsAnalyzer,
      AnalyzerStatusCache analyzerStatusCache) {
    this.itemsPatternsAnalyzer = itemsPatternsAnalyzer;
    this.analyzerStatusCache = analyzerStatusCache;
  }

  @RabbitListener(queues = PATTERN_ANALYSIS_QUEUE, containerFactory = "patternAnalysisContainerFactory")
  public void handleEvent(ItemsPatternAnalyzeDto event) {
    if (event.isLastItem()) {
      analyzerStatusCache.analyzeFinished(PATTERN_ANALYZER_KEY, event.getLaunchId());
    } else {
      itemsPatternsAnalyzer.analyze(event.getProjectId(), event.getLaunchId(), event.getItemIds());
    }
  }


}
