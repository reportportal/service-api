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
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.entity.widget.WidgetOption;
import com.epam.ta.reportportal.entity.widget.content.LaunchesTableContent;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.core.widget.content.WidgetContentUtils.GROUP_CONTENT_FIELDS;
import static java.util.Collections.singletonMap;

/**
 * @author Pavel Bortnik
 */
@Service
public class LaunchesTableContentLoader implements LoadContentStrategy {

	@Autowired
	private WidgetContentRepository widgetContentRepository;

	@Override
	public Map<String, ?> loadContent(List<String> contentFields, Filter filter, Set<WidgetOption> widgetOptions, int limit) {

		List<String> tableColumns = contentFields.stream().filter(field -> !field.contains("$")).collect(Collectors.toList());

		Map<String, List<String>> fields = GROUP_CONTENT_FIELDS.apply(contentFields.stream()
				.filter(field -> field.contains("$"))
				.collect(Collectors.toList()));
		validateContentFields(fields);

		List<LaunchesTableContent> result = widgetContentRepository.launchesTableStatistics(filter, fields, tableColumns, limit);
		return singletonMap(RESULT, result);
	}

	/**
	 * Validate provided content fields.
	 *
	 * @param contentFields Map of provided content.
	 */
	private void validateContentFields(Map<String, List<String>> contentFields) {
		BusinessRule.expect(contentFields, notNull()).verify(ErrorType.BAD_REQUEST_ERROR, "Content fields should not be null.");
	}
}
