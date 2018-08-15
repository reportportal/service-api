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
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.widget.content.LoadContentStrategy;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.entity.widget.ContentField;
import com.epam.ta.reportportal.entity.widget.content.ComparisonStatisticsContent;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.core.widget.content.WidgetContentUtils.GROUP_CONTENT_FIELDS;
import static com.epam.ta.reportportal.dao.WidgetContentRepositoryConstants.DEFECTS_KEY;
import static com.epam.ta.reportportal.dao.WidgetContentRepositoryConstants.EXECUTIONS_KEY;
import static java.util.Collections.singletonMap;

/**
 * @author Pavel Bortnik
 */
@Service
public class LaunchesComparisonContentLoader implements LoadContentStrategy {

	@Autowired
	private LaunchRepository launchRepository;

	@Autowired
	private WidgetContentRepository widgetContentRepository;

	@Override
	public Map<String, ?> loadContent(Set<ContentField> contentFields, Filter filter, Map<String, String> widgetOptions, int limit) {

		validateWidgetOptions(widgetOptions, filter);

		Map<String, List<String>> fields = GROUP_CONTENT_FIELDS.apply(contentFields);
		validateContentFields(fields);

		List<ComparisonStatisticsContent> result = widgetContentRepository.launchesComparisonStatistics(filter,
				fields,
				widgetOptions.get(LAUNCH_NAME_FIELD),
				limit
		);
		return singletonMap(RESULT, result);
	}

	/**
	 * Validate provided widget options. For current widget launch name should be specified.
	 *
	 * @param widgetOptions Set of stored widget options.
	 * @param filter		Filter for launch search
	 */
	private void validateWidgetOptions(Map<String, String> widgetOptions, Filter filter) {
		BusinessRule.expect(MapUtils.isNotEmpty(widgetOptions), equalTo(true))
				.verify(ErrorType.BAD_REQUEST_ERROR, "Widget options should not be null.");

		String launchName = widgetOptions.get(LAUNCH_NAME_FIELD);
		BusinessRule.expect(launchName, StringUtils::isNotEmpty)
				.verify(ErrorType.UNABLE_LOAD_WIDGET_CONTENT, LAUNCH_NAME_FIELD + " should be specified for widget.");
		BusinessRule.expect(launchRepository.findLatestByNameAndFilter(launchName, filter), notNull())
				.verify(ErrorType.LAUNCH_NOT_FOUND, "Launch with name: " + launchName + " - was not found");
	}

	/**
	 * Validate provided content fields.
	 * For this widget content fields only with {@link com.epam.ta.reportportal.dao.WidgetContentRepositoryConstants#EXECUTIONS_KEY},
	 * 											{@link com.epam.ta.reportportal.dao.WidgetContentRepositoryConstants#DEFECTS_KEY}
	 * 										keys should be specified
	 *
	 * The value of at least one of the content fields should not be empty
	 *
	 * @param contentFields Map of provided content.
	 */
	private void validateContentFields(Map<String, List<String>> contentFields) {
		BusinessRule.expect(MapUtils.isNotEmpty(contentFields), equalTo(true))
				.verify(ErrorType.BAD_REQUEST_ERROR, "Content fields should not be empty");
		BusinessRule.expect(contentFields.size(), size -> !(size > 2)).verify(
				ErrorType.BAD_REQUEST_ERROR,
				"Launch statistics' content fields should contain either " + DEFECTS_KEY + " or " + EXECUTIONS_KEY + " keys or both of them"
		);
		BusinessRule.expect(
				CollectionUtils.isNotEmpty(contentFields.get(EXECUTIONS_KEY)) || CollectionUtils.isNotEmpty(contentFields.get(DEFECTS_KEY)),
				equalTo(true)
		).verify(
				ErrorType.BAD_REQUEST_ERROR,
				"The value of at least one of the content fields with keys: " + EXECUTIONS_KEY + ", " + DEFECTS_KEY
						+ " - should not be empty"
		);
	}

}
