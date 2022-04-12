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
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.dao.IssueTypeRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.epam.ta.reportportal.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_ISSUE_TYPE;
import static com.epam.ta.reportportal.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_ISSUE_TYPE_ID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class IssueTypeConditionReplacerTest {

	private final IssueTypeRepository issueTypeRepository = mock(IssueTypeRepository.class);
	private final IssueTypeConditionReplacer replacer = new IssueTypeConditionReplacer(issueTypeRepository);

	@Test
	void shouldReplace() {

		final Filter filter = Filter.builder()
				.withTarget(TestItem.class)
				.withCondition(FilterCondition.builder()
						.withCondition(Condition.IN)
						.withSearchCriteria(CRITERIA_ISSUE_TYPE)
						.withValue("ab001,pb001,ti001")
						.build())
				.build();

		final List<Long> ids = List.of(1L, 2L, 3L);
		when(issueTypeRepository.getIssueTypeIdsByLocators(List.of("ab001", "pb001", "ti001"))).thenReturn(ids);

		replacer.update(filter);

		final List<ConvertibleCondition> rootConditions = filter.getFilterConditions();
		final List<FilterCondition> nestedConditions = rootConditions.get(0).getAllConditions();
		Assertions.assertEquals(1, rootConditions.size());
		Assertions.assertEquals(1, nestedConditions.size());

		final FilterCondition filterCondition = nestedConditions.get(0);

		Assertions.assertEquals(CRITERIA_ISSUE_TYPE_ID, filterCondition.getSearchCriteria());
		Assertions.assertEquals(Condition.IN, filterCondition.getCondition());
		Assertions.assertEquals("1,2,3", filterCondition.getValue());

	}

}