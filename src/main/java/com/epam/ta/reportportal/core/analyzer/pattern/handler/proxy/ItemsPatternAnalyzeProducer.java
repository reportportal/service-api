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

import static com.epam.ta.reportportal.core.analyzer.config.PatternAnalysisConfig.PATTERN_ANALYSIS_QUEUE;

import com.epam.ta.reportportal.core.analyzer.pattern.handler.ItemsPatternsAnalyzer;
import com.epam.ta.reportportal.core.events.MessageBus;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Sends items for pattern analysis queue
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Primary
@Component
public class ItemsPatternAnalyzeProducer implements ItemsPatternsAnalyzer {

  private final boolean isSingleItem;
  private final MessageBus messageBus;

  public ItemsPatternAnalyzeProducer(
      @Value("${rp.environment.variable.pattern-analysis.single-item:true}") boolean isSingleItem,
      MessageBus messageBus) {
    this.isSingleItem = isSingleItem;
    this.messageBus = messageBus;
  }

  @Override
  public void analyze(long projectId, long launchId, List<Long> itemIds) {
    if (CollectionUtils.isEmpty(itemIds)) {
      sendFinishedEvent(projectId, launchId);
    }
    if (isSingleItem) {
      itemIds.forEach(id -> messageBus.publish(PATTERN_ANALYSIS_QUEUE,
          new ItemsPatternAnalyzeDto(projectId, launchId, Collections.singletonList(id))));
    } else {
      messageBus.publish(PATTERN_ANALYSIS_QUEUE,
          new ItemsPatternAnalyzeDto(projectId, launchId, itemIds));
    }
  }

  public void sendFinishedEvent(long projectId, long launchId) {
    messageBus.publish(PATTERN_ANALYSIS_QUEUE,
        new ItemsPatternAnalyzeDto(projectId, launchId, Collections.emptyList(), true));
  }
}
