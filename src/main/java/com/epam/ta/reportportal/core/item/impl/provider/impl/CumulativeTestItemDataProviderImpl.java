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

package com.epam.ta.reportportal.core.item.impl.provider.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.*;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.item.impl.provider.DataProviderHandler;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.statistics.Statistics;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.collections4.CollectionUtils;
import org.jooq.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_LAUNCH_ID;
import static com.epam.ta.reportportal.commons.querygen.constant.ItemAttributeConstant.CRITERIA_COMPOSITE_ATTRIBUTE;
import static com.epam.ta.reportportal.core.widget.content.loader.materialized.handler.MaterializedWidgetStateHandler.VIEW_NAME;
import static com.epam.ta.reportportal.ws.resolver.FilterCriteriaResolver.DEFAULT_FILTER_PREFIX;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Component
public class CumulativeTestItemDataProviderImpl implements DataProviderHandler {

	@Autowired
	private WidgetContentRepository widgetContentRepository;

	@Autowired
	private TestItemRepository testItemRepository;

	@Override
	public Page<TestItem> getTestItems(Queryable filter, Pageable pageable, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user, Map<String, String> params) {
		filter = updateFilter(filter, params);
		return testItemRepository.findByFilter(filter, pageable);

	}

	@Override
	public Set<Statistics> accumulateStatistics(Queryable filter, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user,
			Map<String, String> params) {
		filter = updateFilter(filter, params);
		return testItemRepository.accumulateStatisticsByFilter(filter);
	}

	public Queryable updateFilter(Queryable filter, Map<String, String> providerParams) {
		String compositeAttribute = providerParams.get(DEFAULT_FILTER_PREFIX + Condition.HAS_FILTER + CRITERIA_COMPOSITE_ATTRIBUTE);
		BusinessRule.expect(compositeAttribute, it -> !StringUtils.isEmpty(it))
				.verify(ErrorType.BAD_REQUEST_ERROR, "Level attributes must be provided for widget based items provider");
		List<Long> redirectLaunchIds = widgetContentRepository.getCumulativeLevelRedirectLaunchIds(providerParams.get(VIEW_NAME),
				compositeAttribute
		);
		if (CollectionUtils.isNotEmpty(redirectLaunchIds)) {
			Queryable launchesBasedFilter = Filter.builder()
					.withTarget(TestItem.class)
					.withCondition(FilterCondition.builder().in(CRITERIA_LAUNCH_ID, redirectLaunchIds).build())
					.build();
			return new CompositeFilter(Operator.AND, filter, launchesBasedFilter);
		}
		return filter;
	}
}