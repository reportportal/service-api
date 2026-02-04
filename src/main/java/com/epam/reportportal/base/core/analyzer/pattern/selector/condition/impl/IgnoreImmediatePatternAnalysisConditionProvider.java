/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.core.analyzer.pattern.selector.condition.impl;

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ItemAttributeConstant.CRITERIA_COMPOSITE_SYSTEM_ATTRIBUTE;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ItemAttributeConstant.KEY_VALUE_SEPARATOR;

import com.epam.reportportal.base.core.analyzer.auto.strategy.analyze.AnalyzeItemsMode;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Condition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.ConvertibleCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
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
