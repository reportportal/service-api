/*
 * Copyright 2021 EPAM Systems
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

package com.epam.ta.reportportal.core.item.impl.filter.updater;

import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.ConvertibleCondition;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.dao.IssueTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.ta.reportportal.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_ISSUE_TYPE;
import static com.epam.ta.reportportal.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_ISSUE_TYPE_ID;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class IssueTypeConditionReplacer implements FilterUpdater {

	private final IssueTypeRepository issueTypeRepository;

	@Autowired
	public IssueTypeConditionReplacer(IssueTypeRepository issueTypeRepository) {
		this.issueTypeRepository = issueTypeRepository;
	}

	@Override
	public void update(Queryable filter) {
		// Added to fix performance issue.
		List<String> issueTypeLocators = filter.getFilterConditions()
				.stream()
				.map(ConvertibleCondition::getAllConditions)
				.flatMap(List::stream)
				.filter(c -> CRITERIA_ISSUE_TYPE.equals(c.getSearchCriteria()) && !c.isNegative() && Condition.IN.equals(c.getCondition()))
				.map(FilterCondition::getValue)
				.flatMap(c -> Stream.of(c.split(",")))
				.collect(Collectors.toList());

		String issueTypeIdsString = issueTypeRepository.getIssueTypeIdsByLocators(issueTypeLocators)
				.stream()
				.map(String::valueOf)
				.collect(Collectors.joining(","));

		FilterCondition oldIssueTypeCondition = new FilterCondition(Condition.IN, false, null, CRITERIA_ISSUE_TYPE);
		FilterCondition issueTypeIdCondition = new FilterCondition(Condition.IN, false, issueTypeIdsString, CRITERIA_ISSUE_TYPE_ID);
		filter.replaceSearchCriteria(oldIssueTypeCondition, issueTypeIdCondition);
	}
}
