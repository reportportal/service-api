package com.epam.ta.reportportal.core.analyzer.pattern.selector.condition.impl;

import static com.epam.ta.reportportal.commons.querygen.constant.ItemAttributeConstant.CRITERIA_COMPOSITE_SYSTEM_ATTRIBUTE;
import static com.epam.ta.reportportal.commons.querygen.constant.ItemAttributeConstant.KEY_VALUE_SEPARATOR;

import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.ConvertibleCondition;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.analyze.AnalyzeItemsMode;

public class IgnoreImmediatePatternAnalysisConditionProvider extends
    AbstractPatternConditionProvider {

  private static final String IMMEDIATE_PATTERN_ANALYSIS = "immediatePatternAnalysis";

  public IgnoreImmediatePatternAnalysisConditionProvider(AnalyzeItemsMode analyzeItemsMode) {
    super(analyzeItemsMode);
  }

  @Override
  protected ConvertibleCondition provideCondition() {
    return FilterCondition.builder()
        .withCondition(Condition.HAS)
        .withNegative(true)
        .withSearchCriteria(CRITERIA_COMPOSITE_SYSTEM_ATTRIBUTE)
        .withValue(IMMEDIATE_PATTERN_ANALYSIS + KEY_VALUE_SEPARATOR + Boolean.TRUE)
        .build();
  }
}
