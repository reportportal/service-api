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
package com.epam.ta.reportportal.core.events.handler.item;

import com.epam.ta.reportportal.core.analyzer.pattern.handler.ItemsPatternAnalyzer;
import com.epam.ta.reportportal.core.events.activity.item.TestItemFinishedEvent;
import com.epam.ta.reportportal.core.events.handler.ConfigurableEventHandler;
import com.epam.ta.reportportal.entity.ItemAttribute;
import com.google.common.collect.Lists;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class TestItemPatternAnalysisRunner implements
    ConfigurableEventHandler<TestItemFinishedEvent, Map<String, String>> {

  public static final String IMMEDIATE_PATTERN_ANALYSIS = "immediatePatternAnalysis";

  private final ItemsPatternAnalyzer patternAnalyzer;

  public TestItemPatternAnalysisRunner(ItemsPatternAnalyzer patternAnalyzer) {
    this.patternAnalyzer = patternAnalyzer;
  }

  @Override
  public void handle(TestItemFinishedEvent event, Map<String, String> config) {
    Optional<ItemAttribute> first = event.getTestItem().getAttributes().stream()
        .filter(it -> IMMEDIATE_PATTERN_ANALYSIS.equals(it.getKey())).findFirst();

    if (first.isPresent() && Boolean.parseBoolean(first.get().getValue())) {
      patternAnalyzer.analyzeItems(event.getProjectId(), event.getTestItem().getLaunchId(),
          Lists.newArrayList(event.getTestItem().getItemId()));
    }
  }
}
