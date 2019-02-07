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
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.entity.widget.content.CriteriaHistoryItem;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.converters.LaunchConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_ID;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.*;
import static com.epam.ta.reportportal.core.widget.util.WidgetFilterUtil.GROUP_FILTERS;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

/**
 * Content loader for {@link com.epam.ta.reportportal.entity.widget.WidgetType#TOP_TEST_CASES}
 *
 * @author Pavel Bortnik
 */
@Service
public class TopTestCasesContentLoader implements LoadContentStrategy {

	private final LaunchRepository launchRepository;

	private final LaunchConverter launchConverter;

	private final WidgetContentRepository widgetContentRepository;

	@Autowired
	public TopTestCasesContentLoader(LaunchRepository launchRepository, LaunchConverter launchConverter,
			WidgetContentRepository widgetContentRepository) {
		this.launchRepository = launchRepository;
		this.launchConverter = launchConverter;
		this.widgetContentRepository = widgetContentRepository;
	}

	@Override
	public Map<String, ?> loadContent(List<String> contentFields, Map<Filter, Sort> filterSortMapping, WidgetOptions widgetOptions,
			int limit) {
		String contentField = validateContentFields(contentFields);
		Filter filter = GROUP_FILTERS.apply(filterSortMapping.keySet());
		Launch latestByName = launchRepository.findLatestByFilter(FilterUtils.buildLatestLaunchFilter(filter,
				WidgetOptionUtil.getValueByKey(LAUNCH_NAME_FIELD, widgetOptions)
		))
				.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND,
						WidgetOptionUtil.getValueByKey(LAUNCH_NAME_FIELD, widgetOptions)
				));

		filter.withCondition(new FilterCondition(Condition.EQUALS, false, String.valueOf(latestByName.getId()), CRITERIA_ID));

		List<CriteriaHistoryItem> content = widgetContentRepository.topItemsByCriteria(filter,
				contentField,
				limit,
				ofNullable(widgetOptions.getOptions().get(INCLUDE_METHODS)).map(v -> BooleanUtils.toBoolean(String.valueOf(v)))
						.orElse(false)
		);

		return CollectionUtils.isEmpty(content) ?
				emptyMap() :
				ImmutableMap.<String, Object>builder().put(LATEST_LAUNCH, launchConverter.TO_RESOURCE.apply(latestByName))
						.put(RESULT, content)
						.build();
	}

	/**
	 * Validate provided content fields. For current widget it should be only one field specified in content fields.
	 * Example is 'executions$failed', so widget would be created by 'failed' criteria.
	 *
	 * @param contentFields List of provided content.
	 */
	private String validateContentFields(List<String> contentFields) {
		BusinessRule.expect(CollectionUtils.isNotEmpty(contentFields), equalTo(true))
				.verify(ErrorType.BAD_REQUEST_ERROR, "Content fields should not be empty");
		BusinessRule.expect(contentFields.size(), Predicate.isEqual(1))
				.verify(ErrorType.BAD_REQUEST_ERROR, "Only one content field could be specified.");
		return contentFields.get(0);
	}
}
