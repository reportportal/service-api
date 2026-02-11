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

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_ISSUE_GROUP_ID;

import com.epam.reportportal.base.core.analyzer.auto.strategy.analyze.AnalyzeItemsMode;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.CompositeFilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.ConvertibleCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.issue.IssueGroup;
import com.google.common.collect.Lists;
import java.util.function.Supplier;
import org.jooq.Operator;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class ToInvestigatePatternConditionProvider extends AbstractPatternConditionProvider {

  //TODO ADD OPTIONAL<ISSUEGROUP>
  private final Supplier<IssueGroup> issueGroupSupplier;

  public ToInvestigatePatternConditionProvider(AnalyzeItemsMode analyzeItemsMode,
      Supplier<IssueGroup> issueGroupSupplier) {
    super(analyzeItemsMode);
    this.issueGroupSupplier = issueGroupSupplier;
  }

  @Override
  protected ConvertibleCondition provideCondition() {
    return new CompositeFilterCondition(Lists.newArrayList(FilterCondition.builder()
        .eq(CRITERIA_ISSUE_GROUP_ID, String.valueOf(issueGroupSupplier.get().getId()))
        .build()), Operator.OR);

  }

}
