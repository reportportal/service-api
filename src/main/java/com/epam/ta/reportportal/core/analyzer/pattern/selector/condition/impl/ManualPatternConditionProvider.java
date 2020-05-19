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

package com.epam.ta.reportportal.core.analyzer.pattern.selector.condition.impl;

import com.epam.ta.reportportal.commons.querygen.CompositeFilterCondition;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.ConvertibleCondition;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.analyze.AnalyzeItemsMode;
import com.epam.ta.reportportal.entity.item.issue.IssueGroup;
import com.google.common.collect.Lists;
import org.jooq.Operator;

import java.util.function.Supplier;

import static com.epam.ta.reportportal.commons.querygen.constant.IssueCriteriaConstant.CRITERIA_ISSUE_AUTO_ANALYZED;
import static com.epam.ta.reportportal.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_ISSUE_GROUP_ID;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class ManualPatternConditionProvider extends AbstractPatternConditionProvider {

	private final Supplier<IssueGroup> issueGroupSupplier;

	public ManualPatternConditionProvider(AnalyzeItemsMode analyzeItemsMode, Supplier<IssueGroup> issueGroupSupplier) {
		super(analyzeItemsMode);
		this.issueGroupSupplier = issueGroupSupplier;
	}

	@Override
	protected ConvertibleCondition provideCondition() {

		return new CompositeFilterCondition(Lists.newArrayList(FilterCondition.builder()
				.withCondition(Condition.NOT_EQUALS)
				.withSearchCriteria(CRITERIA_ISSUE_GROUP_ID)
				.withValue(String.valueOf(issueGroupSupplier.get().getId()))
				.build(), FilterCondition.builder().eq(CRITERIA_ISSUE_AUTO_ANALYZED, Boolean.FALSE.toString()).build()), Operator.OR);
	}

}
