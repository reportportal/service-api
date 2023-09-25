package com.epam.ta.reportportal.core.events.handler.item;

import com.epam.ta.reportportal.core.analyzer.pattern.handler.ItemsPatternAnalyzer;
import com.epam.ta.reportportal.core.events.activity.item.TestItemFinishedEvent;
import com.epam.ta.reportportal.core.events.handler.ConfigurableEventHandler;
import com.epam.ta.reportportal.entity.ItemAttribute;
import com.google.common.collect.Lists;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

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
