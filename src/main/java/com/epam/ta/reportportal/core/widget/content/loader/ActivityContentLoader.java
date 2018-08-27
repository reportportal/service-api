/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.core.widget.content.loader;

import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.widget.content.LoadContentStrategy;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.widget.content.ActivityContent;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static java.util.Collections.singletonMap;

/**
 * @author Pavel Bortnik
 */
@Service
public class ActivityContentLoader implements LoadContentStrategy {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private WidgetContentRepository widgetContentRepository;

	@Override
	public Map<String, ?> loadContent(List<String> contentFields, Filter filter, Sort sort, Map<String, String> widgetOptions, int limit) {

		validateWidgetOptions(widgetOptions);

		validateContentFields(contentFields);

		String login = widgetOptions.get(LOGIN);
		User user = userRepository.findByLogin(login)
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, "User with login " + login + " was not found"));

		filter.withCondition(new FilterCondition(Condition.EQUALS, false, user.getLogin(), LOGIN))
				.withCondition(new FilterCondition(Condition.IN, false, String.join(",", contentFields), "action"));

		List<ActivityContent> activityContents = widgetContentRepository.activityStatistics(filter, contentFields, sort, limit);

		return singletonMap(RESULT, activityContents);
	}

	/**
	 * Validate provided content fields.
	 *
	 * <p>
	 * The value of content field should not be empty
	 *
	 * @param contentFields Map of provided content.
	 */
	private void validateContentFields(List<String> contentFields) {
		BusinessRule.expect(CollectionUtils.isNotEmpty(contentFields), equalTo(true))
				.verify(ErrorType.BAD_REQUEST_ERROR, "Content fields should not be empty");
	}

	/**
	 * Validate provided widget options. For current widget user login should be specified for activity tracking.
	 *
	 * @param widgetOptions Set of stored widget options.
	 */
	private void validateWidgetOptions(Map<String, String> widgetOptions) {
		BusinessRule.expect(MapUtils.isNotEmpty(widgetOptions), equalTo(true))
				.verify(ErrorType.BAD_REQUEST_ERROR, "Widget options should not be null.");
		BusinessRule.expect(widgetOptions.get(LOGIN), StringUtils::isNotEmpty)
				.verify(ErrorType.UNABLE_LOAD_WIDGET_CONTENT, LOGIN + " should be specified for widget.");
	}

}
