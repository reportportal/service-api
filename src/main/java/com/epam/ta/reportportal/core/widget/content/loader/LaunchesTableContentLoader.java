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
import com.epam.ta.reportportal.entity.widget.ContentField;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.core.widget.content.WidgetContentUtils.GROUP_CONTENT_FIELDS;
import static com.epam.ta.reportportal.dao.WidgetContentRepositoryConstants.DEFECTS_KEY;
import static com.epam.ta.reportportal.dao.WidgetContentRepositoryConstants.EXECUTIONS_KEY;
import static java.util.Collections.singletonMap;

/**
 * @author Pavel Bortnik
 */
@Service
public class LaunchesTableContentLoader implements LoadContentStrategy {

	@Autowired
	private WidgetContentRepository widgetContentRepository;

	@Override
	public Map<String, ?> loadContent(Set<ContentField> contentFields, Filter filter, Map<String, String> widgetOptions, int limit) {

		Map<String, List<String>> fields = GROUP_CONTENT_FIELDS.apply(contentFields);
		validateContentFields(fields);

		return singletonMap(RESULT, widgetContentRepository.launchesTableStatistics(filter, fields, limit));
	}

	/**
	 * Validate provided content fields.
	 * For this widget content fields only with {@link com.epam.ta.reportportal.dao.WidgetContentRepositoryConstants#EXECUTIONS_KEY},
	 * {@link com.epam.ta.reportportal.dao.WidgetContentRepositoryConstants#DEFECTS_KEY},
	 * {@link com.epam.ta.reportportal.dao.WidgetContentRepositoryConstants#TABLE_COLUMN_KEY} (optional)
	 * keys should be specified
	 * <p>
	 * The value of at least one of the non-optional content fields should not be empty
	 *
	 * @param contentFields Map of provided content.
	 */
	private void validateContentFields(Map<String, List<String>> contentFields) {
		BusinessRule.expect(MapUtils.isNotEmpty(contentFields), equalTo(true))
				.verify(ErrorType.BAD_REQUEST_ERROR, "Content fields should not be empty");
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
