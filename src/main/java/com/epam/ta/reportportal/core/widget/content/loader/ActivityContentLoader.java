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

import static com.epam.ta.reportportal.commons.querygen.constant.ActivityCriteriaConstant.CRITERIA_CREATED_AT;
import static com.epam.ta.reportportal.commons.querygen.constant.ActivityCriteriaConstant.CRITERIA_EVENT_NAME;
import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.ACTION_TYPE;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.CONTENT_FIELDS_DELIMITER;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.RESULT;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.USER;
import static com.epam.ta.reportportal.core.widget.util.WidgetFilterUtil.GROUP_FILTERS;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.Optional.ofNullable;

import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.core.widget.content.LoadContentStrategy;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.reportportal.model.ActivityResource;
import com.epam.reportportal.rules.exception.ErrorType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * @author Pavel Bortnik
 */
@Service
public class ActivityContentLoader implements LoadContentStrategy {

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

		Filter filter = GROUP_FILTERS.apply(filterSortMapping.keySet());

		Sort sort = Sort.by(Sort.Direction.DESC, CRITERIA_CREATED_AT);

		ofNullable(widgetOptions).ifPresent(wo -> modifyFilterWithUserCriteria(filter, wo));

		final List<String> actionTypes = (List<String>) widgetOptions.getOptions().get(ACTION_TYPE);

		expect(actionTypes, CollectionUtils::isNotEmpty).verify(ErrorType.BAD_REQUEST_ERROR,
				"At least 1 action type should be provided.");

		filter.withCondition(
				new FilterCondition(Condition.IN, false, String.join(CONTENT_FIELDS_DELIMITER, actionTypes),
						CRITERIA_EVENT_NAME));

		List<ActivityResource> activityContents = widgetContentRepository.activityStatistics(filter,
				sort, limit);

		return activityContents.isEmpty() ? emptyMap() : singletonMap(RESULT, activityContents);
	}

	/**
	 * Add username criteria for the filter if there are any username options in the {@link WidgetOptions#getOptions()}
	 *
	 * @param filter        {@link Filter}
	 * @param widgetOptions {@link WidgetOptions}
	 */
	private void modifyFilterWithUserCriteria(Filter filter, WidgetOptions widgetOptions) {

		ofNullable(widgetOptions.getOptions()).ifPresent(wo -> ofNullable(wo.get(USER)).ifPresent(users -> {

			if (StringUtils.isNotBlank(String.valueOf(users))) {
				Set<String> usernameCriteria = Arrays.stream(String.valueOf(users).split(CONTENT_FIELDS_DELIMITER))
						.map(String::trim)
						.collect(Collectors.toSet());

				usernameCriteria.forEach(username -> userRepository.findByLogin(username)
						.orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND,
								"User with login " + username + " was not found"
						)));

				filter.withCondition(new FilterCondition(Condition.IN,
						false,
						String.join(CONTENT_FIELDS_DELIMITER, usernameCriteria),
						USER
				));
			}

		}));
	}

}
