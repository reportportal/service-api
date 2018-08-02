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

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.widget.content.LoadContentStrategy;
import com.epam.ta.reportportal.core.widget.content.WidgetContentUtils;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.widget.WidgetOption;
import com.epam.ta.reportportal.entity.widget.content.ActivityContent;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.core.widget.content.WidgetContentUtils.GROUP_CONTENT_FIELDS;
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
	public Map<String, ?> loadContent(List<String> contentFields, Filter filter, Set<WidgetOption> widgetOptions, int limit) {

		Map<String, List<String>> options = WidgetContentUtils.GROUP_WIDGET_OPTIONS.apply(widgetOptions);

		Optional<User> userOptional = userRepository.findByLogin(options.get("login").iterator().next());

		User user = userOptional.orElseThrow(() -> new ReportPortalException(
				"User with login " + options.get("login").iterator().next() + " was not found"));

		List<String> activityTypes = Optional.ofNullable(options.get("activity_type"))
				.orElseThrow(() -> new ReportPortalException("Activity types must not be null"));

		//		String conditionValue = activityTypes.stream().collect(Collectors.joining(", "));
		//
		//		FilterCondition filterCondition = new FilterCondition(Condition.IN, false, conditionValue, "value");
		//
		//		filter.withCondition(filterCondition);

		List<ActivityContent> activityContents = widgetContentRepository.activityStatistics(
				filter,
				GROUP_CONTENT_FIELDS.apply(contentFields.stream().filter(field -> field.contains("$")).collect(Collectors.toList())),
				user.getLogin(),
				activityTypes
		);

		return singletonMap(RESULT, activityContents);
	}
}
