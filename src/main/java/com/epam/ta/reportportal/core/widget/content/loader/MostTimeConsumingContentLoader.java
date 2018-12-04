/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.core.widget.content.loader;

import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.widget.content.LoadContentStrategy;
import com.epam.ta.reportportal.core.widget.content.loader.util.FilterUtils;
import com.epam.ta.reportportal.core.widget.util.WidgetOptionUtil;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_LAUNCH_ID;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.*;
import static com.epam.ta.reportportal.core.widget.content.loader.ActivityContentLoader.CONTENT_FIELDS_DELIMITER;
import static com.epam.ta.reportportal.core.widget.util.WidgetFilterUtil.GROUP_FILTERS;
import static com.epam.ta.reportportal.jooq.enums.JTestItemTypeEnum.*;
import static java.util.Collections.singletonMap;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class MostTimeConsumingContentLoader implements LoadContentStrategy {

	private final LaunchRepository launchRepository;
	private final WidgetContentRepository widgetContentRepository;

	@Autowired
	public MostTimeConsumingContentLoader(LaunchRepository launchRepository, WidgetContentRepository widgetContentRepository) {
		this.launchRepository = launchRepository;
		this.widgetContentRepository = widgetContentRepository;
	}

	@Override
	public Map<String, ?> loadContent(List<String> contentFields, Map<Filter, Sort> filterSortMap, WidgetOptions widgetOptions, int limit) {

		validateFilterSortMapping(filterSortMap);

		validateWidgetOptions(widgetOptions);

		Filter filter = GROUP_FILTERS.apply(filterSortMap.keySet());

		filter = updateFilter(filter, widgetOptions);

		return singletonMap(RESULT, widgetContentRepository.mostTimeConsumingTestCasesStatistics(filter));
	}

	/**
	 * Mapping should not be empty
	 *
	 * @param filterSortMapping Map of ${@link Filter} for query building as key and ${@link Sort} as value for each filter
	 */
	private void validateFilterSortMapping(Map<Filter, Sort> filterSortMapping) {
		BusinessRule.expect(MapUtils.isNotEmpty(filterSortMapping), equalTo(true))
				.verify(ErrorType.BAD_REQUEST_ERROR, "Filter-Sort mapping should not be empty");
	}

	/**
	 * Validate provided widget options. For current widget launch name should be specified.
	 *
	 * @param widgetOptions Map of stored widget options.
	 */
	private void validateWidgetOptions(WidgetOptions widgetOptions) {
		BusinessRule.expect(WidgetOptionUtil.getValueByKey(LAUNCH_NAME_FIELD, widgetOptions), StringUtils::isNotBlank)
				.verify(ErrorType.UNABLE_LOAD_WIDGET_CONTENT, LAUNCH_NAME_FIELD + " should be specified for widget.");
	}

	private Filter updateFilter(Filter filter, WidgetOptions widgetOptions) {
		filter = updateFilterWithLatestLaunchId(filter, WidgetOptionUtil.getValueByKey(LAUNCH_NAME_FIELD, widgetOptions));
		filter = updateFilterWithTestItemTypes(filter, WidgetOptionUtil.containsKey(INCLUDE_METHODS, widgetOptions));
		return filter;
	}

	private Filter updateFilterWithLatestLaunchId(Filter filter, String launchName) {
		return filter.withCondition(new FilterCondition(
				Condition.EQUALS,
				false,
				String.valueOf(launchRepository.findLatestByFilter(FilterUtils.buildLatestLaunchFilter(filter, launchName))
						.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, "No launch with name: " + launchName))
						.getId()),
				CRITERIA_LAUNCH_ID
		));
	}

	private Filter updateFilterWithTestItemTypes(Filter filter, boolean includeMethodsFlag) {
		if (includeMethodsFlag) {
			return updateFilterWithStepAndBeforeAfterMethods(filter);
		} else {
			return updateFilterWithStepTestItem(filter);
		}
	}

	private Filter updateFilterWithStepTestItem(Filter filter) {
		return filter.withCondition(new FilterCondition(Condition.EQUALS, false, STEP.getLiteral(), ITEM_TYPE));
	}

	private Filter updateFilterWithStepAndBeforeAfterMethods(Filter filter) {
		return filter.withCondition(new FilterCondition(
				Condition.IN,
				false,
				String.join(CONTENT_FIELDS_DELIMITER, STEP.getLiteral(), BEFORE_METHOD.getLiteral(), AFTER_METHOD.getLiteral()),
				ITEM_TYPE
		));
	}
}
