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

package com.epam.ta.reportportal.core.widget.content.loader;

import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.core.widget.content.LoadContentStrategy;
import com.epam.ta.reportportal.core.widget.util.WidgetOptionUtil;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.enums.TestItemTypeEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.entity.widget.content.LatestLaunchContent;
import com.epam.ta.reportportal.entity.widget.content.MostTimeConsumingTestCasesContent;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_LAUNCH_ID;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_NAME;
import static com.epam.ta.reportportal.commons.querygen.constant.TestItemCriteriaConstant.*;
import static com.epam.ta.reportportal.core.filter.predefined.PredefinedFilters.HAS_METHOD_OR_CLASS;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.*;
import static com.epam.ta.reportportal.core.widget.util.WidgetFilterUtil.GROUP_FILTERS;
import static com.epam.ta.reportportal.jooq.enums.JTestItemTypeEnum.STEP;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class MostTimeConsumingContentLoader implements LoadContentStrategy {

	public static final int MOST_TIME_CONSUMING_CASES_COUNT = 20;

	private final LaunchRepository launchRepository;
	private final WidgetContentRepository widgetContentRepository;

	@Autowired
	public MostTimeConsumingContentLoader(LaunchRepository launchRepository, WidgetContentRepository widgetContentRepository) {
		this.launchRepository = launchRepository;
		this.widgetContentRepository = widgetContentRepository;
	}

	@Override
	public Map<String, ?> loadContent(List<String> contentFields, Map<Filter, Sort> filterSortMap, WidgetOptions widgetOptions, int limit) {

		Filter filter = GROUP_FILTERS.apply(filterSortMap.keySet());
		String launchName = WidgetOptionUtil.getValueByKey(LAUNCH_NAME_FIELD, widgetOptions);

		Launch launch = launchRepository.findLatestByFilter(filter.withCondition(FilterCondition.builder()
				.eq(CRITERIA_NAME, launchName)
				.build())).orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, "No launch with name: " + launchName));

		final List<MostTimeConsumingTestCasesContent> content = widgetContentRepository.mostTimeConsumingTestCasesStatistics(buildItemFilter(
				launch.getId(),
				widgetOptions,
				contentFields
		), MOST_TIME_CONSUMING_CASES_COUNT);

		return content.isEmpty() ?
				emptyMap() :
				ImmutableMap.<String, Object>builder().put(LATEST_LAUNCH, new LatestLaunchContent(launch)).put(RESULT, content).build();
	}

	private Filter buildItemFilter(Long launchId, WidgetOptions widgetOptions, List<String> contentFields) {
		Filter filter = Filter.builder()
				.withTarget(TestItem.class)
				.withCondition(FilterCondition.builder().eq(CRITERIA_LAUNCH_ID, String.valueOf(launchId)).build())
				.build();
		filter = updateFilterWithStatuses(filter, contentFields);
		filter = updateFilterWithTestItemTypes(filter,
				ofNullable(widgetOptions.getOptions().get(INCLUDE_METHODS)).map(v -> BooleanUtils.toBoolean(String.valueOf(v)))
						.orElse(false)
		);
		return filter.withCondition(FilterCondition.builder().eq(CRITERIA_HAS_CHILDREN, Boolean.FALSE.toString()).build())
				.withCondition(FilterCondition.builder().eq(CRITERIA_HAS_STATS, Boolean.TRUE.toString()).build());
	}

	private Filter updateFilterWithStatuses(Filter filter, List<String> contentFields) {
		if (CollectionUtils.isNotEmpty(contentFields)) {
			String statusCriteria = contentFields.stream()
					.filter(StringUtils::isNotBlank)
					.map(it -> it.split("\\$"))
					.map(split -> split[split.length - 1].toUpperCase())
					.filter(cf -> StatusEnum.fromValue(cf).isPresent())
					.collect(Collectors.joining(", "));
			return filter.withCondition(FilterCondition.builder()
					.withSearchCriteria(CRITERIA_STATUS)
					.withCondition(Condition.IN)
					.withValue(statusCriteria)
					.build());
		}
		return filter;
	}

	private Filter updateFilterWithTestItemTypes(Filter filter, boolean includeMethodsFlag) {
		return includeMethodsFlag ? updateFilterWithStepAndBeforeAfterMethods(filter) : updateFilterWithStepTestItem(filter);
	}

	private Filter updateFilterWithStepTestItem(Filter filter) {
		return filter.withCondition(FilterCondition.builder().eq(ITEM_TYPE, STEP.getLiteral()).build());
	}

	private Filter updateFilterWithStepAndBeforeAfterMethods(Filter filter) {
		List<TestItemTypeEnum> itemTypes = Lists.newArrayList(TestItemTypeEnum.STEP);
		itemTypes.addAll(HAS_METHOD_OR_CLASS);

		return filter.withCondition(FilterCondition.builder()
				.withCondition(Condition.IN)
				.withSearchCriteria(ITEM_TYPE)
				.withValue(itemTypes.stream().map(TestItemTypeEnum::name).collect(Collectors.joining(CONTENT_FIELDS_DELIMITER)))
				.build());
	}
}
