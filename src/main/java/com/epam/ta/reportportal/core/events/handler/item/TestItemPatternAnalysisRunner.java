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

import com.epam.ta.reportportal.core.analyzer.pattern.handler.ItemsPatternsAnalyzer;
import com.epam.ta.reportportal.core.events.activity.item.TestItemFinishedEvent;
import com.epam.ta.reportportal.core.events.handler.ConfigurableEventHandler;
import com.epam.ta.reportportal.entity.ItemAttribute;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class TestItemPatternAnalysisRunner implements
    ConfigurableEventHandler<TestItemFinishedEvent, Map<String, String>> {

  protected static final String IMMEDIATE_PATTERN_ANALYSIS = "immediatePatternAnalysis";

  private final ItemsPatternsAnalyzer patternsAnalyzer;

  public TestItemPatternAnalysisRunner(ItemsPatternsAnalyzer patternsAnalyzer) {
    this.patternsAnalyzer = patternsAnalyzer;
  }

  @Override
  public void handle(TestItemFinishedEvent event, Map<String, String> config) {
    if (isImmediatePaProvided(event)) {
      patternsAnalyzer.analyze(event.getProjectId(), event.getTestItem().getLaunchId(),
          Collections.singletonList(event.getTestItem().getItemId()));
    }
  }

  private static boolean isImmediatePaProvided(TestItemFinishedEvent event) {
    Optional<ItemAttribute> immediatePa = event.getTestItem().getAttributes().stream()
        .filter(it -> IMMEDIATE_PATTERN_ANALYSIS.equals(it.getKey())).findAny();
    return immediatePa.isPresent() && Boolean.parseBoolean(immediatePa.get().getValue());
  }
}
