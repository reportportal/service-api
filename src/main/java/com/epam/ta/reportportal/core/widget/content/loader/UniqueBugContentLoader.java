/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.widget.content.loader;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.widget.content.LoadContentStrategy;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.LATEST_OPTION;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.LAUNCH_NAME_FIELD;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.RESULT;
import static com.epam.ta.reportportal.core.widget.util.WidgetFilterUtil.GROUP_FILTERS;
import static com.epam.ta.reportportal.core.widget.util.WidgetFilterUtil.GROUP_SORTS;
import static java.util.Collections.singletonMap;

/**
 * @author Pavel Bortnik
 */
@Service
public class UniqueBugContentLoader implements LoadContentStrategy {

	@Autowired
	private WidgetContentRepository widgetRepository;

	@Override
	public Map<String, ?> loadContent(List<String> contentFields, Map<Filter, Sort> filterSortMapping, Map<String, String> widgetOptions,
			int limit) {

		validateFilterSortMapping(filterSortMapping);

		validateWidgetOptions(widgetOptions);

		Filter filter = GROUP_FILTERS.apply(filterSortMapping.keySet());

		Sort sort = GROUP_SORTS.apply(filterSortMapping.values());

		return singletonMap(RESULT, widgetRepository.uniqueBugStatistics(filter, sort, widgetOptions.containsKey(LATEST_OPTION), limit));
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
	 * Validate provided widget options. For current widget launch name should be specified.
	 *
	 * @param widgetOptions Map of stored widget options.
	 */
	private void validateWidgetOptions(Map<String, String> widgetOptions) {
		BusinessRule.expect(MapUtils.isNotEmpty(widgetOptions), equalTo(true))
				.verify(ErrorType.BAD_REQUEST_ERROR, "Widget options should not be null.");

		String launchName = widgetOptions.get(LAUNCH_NAME_FIELD);
		BusinessRule.expect(launchName, StringUtils::isNotEmpty)
				.verify(ErrorType.UNABLE_LOAD_WIDGET_CONTENT, LAUNCH_NAME_FIELD + " should be specified for widget.");
	}

}
