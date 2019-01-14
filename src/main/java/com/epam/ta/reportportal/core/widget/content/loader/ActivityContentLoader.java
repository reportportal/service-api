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
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.entity.widget.content.ActivityContent;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.querygen.constant.ActivityCriteriaConstant.CRITERIA_ACTION;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.*;
import static com.epam.ta.reportportal.core.widget.util.WidgetFilterUtil.GROUP_FILTERS;
import static com.epam.ta.reportportal.core.widget.util.WidgetFilterUtil.GROUP_SORTS;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.Optional.ofNullable;

/**
 * @author Pavel Bortnik
 */
@Service
public class ActivityContentLoader implements LoadContentStrategy {

	public static final String CONTENT_FIELDS_DELIMITER = ",";

	private final UserRepository userRepository;

	private final WidgetContentRepository widgetContentRepository;

	@Autowired
	public ActivityContentLoader(UserRepository userRepository, WidgetContentRepository widgetContentRepository) {
		this.userRepository = userRepository;
		this.widgetContentRepository = widgetContentRepository;
	}

	@Override
	public Map<String, ?> loadContent(List<String> contentFields, Map<Filter, Sort> filterSortMapping, WidgetOptions widgetOptions,
			int limit) {

		validateFilterSortMapping(filterSortMapping);

		validateContentFields(contentFields);

		Filter filter = GROUP_FILTERS.apply(filterSortMapping.keySet());

		Sort sort = GROUP_SORTS.apply(filterSortMapping.values());

		ofNullable(widgetOptions).ifPresent(wo -> modifyFilterWithUserCriteria(filter, wo));

		final List<String> actionTypes = (List<String>) widgetOptions.getOptions().get(ACTION_TYPE);

		filter.withCondition(new FilterCondition(Condition.IN, false, String.join(CONTENT_FIELDS_DELIMITER, actionTypes), CRITERIA_ACTION));

		List<ActivityContent> activityContents = widgetContentRepository.activityStatistics(filter, sort, limit);

		return activityContents.isEmpty() ? emptyMap() : singletonMap(RESULT, activityContents);
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
	 * Validate provided content fields.
	 *
	 * <p>
	 * The value of content field should not be empty
	 *
	 * @param contentFields List of provided content.
	 */
	private void validateContentFields(List<String> contentFields) {
		BusinessRule.expect(CollectionUtils.isNotEmpty(contentFields), equalTo(true))
				.verify(ErrorType.BAD_REQUEST_ERROR, "Content fields should not be empty");
	}

	/**
	 * Add username criteria for the filter if there are any username options in the {@link WidgetOptions#options}
	 *
	 * @param filter        {@link Filter}
	 * @param widgetOptions {@link WidgetOptions}
	 */
	private void modifyFilterWithUserCriteria(Filter filter, WidgetOptions widgetOptions) {

		ofNullable(widgetOptions.getOptions()).ifPresent(wo -> ofNullable(wo.get(USER)).ifPresent(users -> {
			Set<String> usernameCriteria = Arrays.stream(String.valueOf(users).split(CONTENT_FIELDS_DELIMITER))
					.map(String::trim)
					.collect(Collectors.toSet());

			usernameCriteria.forEach(username -> userRepository.findByLogin(username)
					.orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND,
							"User with login " + username + " was not found"
					)));

			filter.withCondition(new FilterCondition(Condition.IN, false, String.join(CONTENT_FIELDS_DELIMITER, usernameCriteria), USER));

		}));
	}

}
