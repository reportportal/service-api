/*
 * Copyright 2018 EPAM Systems
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
import com.epam.ta.reportportal.core.widget.content.WidgetContentUtils;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.widget.ContentField;
import com.epam.ta.reportportal.entity.widget.content.MostFailedContent;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.converters.LaunchConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.notNull;

/**
 * Content loader for {@link com.epam.ta.reportportal.entity.widget.WidgetType#MOST_FAILED_TEST_CASES}
 *
 * @author Pavel Bortnik
 */
@Service
public class MostFailedContentLoader implements LoadContentStrategy {

	private LaunchRepository launchRepository;

	private WidgetContentRepository widgetContentRepository;

	@Autowired
	public void setLaunchRepository(LaunchRepository launchRepository) {
		this.launchRepository = launchRepository;
	}

	@Autowired
	public void setWidgetContentRepository(WidgetContentRepository widgetContentRepository) {
		this.widgetContentRepository = widgetContentRepository;
	}

	@Override
	public Map<String, ?> loadContent(Set<ContentField> contentFields, Filter filter, Map<String, String> widgetOptions, int limit) {
		Map<String, List<String>> fields = WidgetContentUtils.GROUP_CONTENT_FIELDS.apply(contentFields);
		validateContentFields(fields);

		String launchName = widgetOptions.get(LAUNCH_NAME_FIELD);
		Launch latestByName = launchRepository.findLatestByNameAndFilter(launchName, filter)
				.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, "No launch with name: " + launchName));

		List<MostFailedContent> content;
		if (fields.containsKey(EXECUTIONS)) {
			content = widgetContentRepository.mostFailedByExecutionCriteria(widgetOptions.get(LAUNCH_NAME_FIELD),
					fields.get(EXECUTIONS).get(0),
					limit
			);
		} else {
			content = widgetContentRepository.mostFailedByDefectCriteria(widgetOptions.get(LAUNCH_NAME_FIELD),
					fields.get(DEFECTS).get(0),
					limit
			);
		}
		return ImmutableMap.<String, Object>builder().put(LATEST_LAUNCH, LaunchConverter.TO_RESOURCE.apply(latestByName))
				.put(RESULT, content)
				.build();
	}

	/**
	 * Validate provided widget options. For current widget should be specified launch name.
	 *
	 * @param widgetOptions Set of stored widget options.
	 */
	private void validateWidgetOptions(Map<String, List<String>> widgetOptions) {
		BusinessRule.expect(widgetOptions, notNull()).verify(ErrorType.BAD_REQUEST_ERROR, "Widget options should not be null.");
		BusinessRule.expect(widgetOptions.containsKey(LAUNCH_NAME_FIELD), Predicate.isEqual(true))
				.verify(ErrorType.UNABLE_LOAD_WIDGET_CONTENT, LAUNCH_NAME_FIELD + " should be specified for widget.");
	}

	/**
	 * Validate provided content fields. For current widget it should be only one field specified in content fields.
	 * Example is 'executions$failed', so widget would be created by 'failed' criteria.
	 *
	 * @param contentFields List of provided content.
	 * @return Map of grouped content fields by first part. Expected only one value.
	 */
	private void validateContentFields(Map<String, List<String>> contentFields) {
		BusinessRule.expect(MapUtils.isNotEmpty(contentFields), equalTo(true))
				.verify(ErrorType.BAD_REQUEST_ERROR, "Content fields should not be empty");
		BusinessRule.expect(contentFields.size(), Predicate.isEqual(1))
				.verify(ErrorType.BAD_REQUEST_ERROR, "Only one content field could be specified.");
	}
}
