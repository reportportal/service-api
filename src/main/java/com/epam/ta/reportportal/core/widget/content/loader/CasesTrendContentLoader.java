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

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.widget.content.LoadContentStrategy;
import com.epam.ta.reportportal.core.widget.util.ContentFieldMatcherUtil;
import com.epam.ta.reportportal.core.widget.util.WidgetOptionUtil;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.entity.widget.content.ChartStatisticsContent;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.RESULT;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.TIMELINE;
import static com.epam.ta.reportportal.core.widget.util.ContentFieldPatternConstants.EXECUTIONS_TOTAL_REGEX;
import static com.epam.ta.reportportal.core.widget.util.WidgetFilterUtil.GROUP_FILTERS;
import static com.epam.ta.reportportal.core.widget.util.WidgetFilterUtil.GROUP_SORTS;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.DELTA;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;

/**
 * @author Pavel Bortnik
 */
@Service
public class CasesTrendContentLoader extends AbstractStatisticsContentLoader implements LoadContentStrategy {

	@Autowired
	private WidgetContentRepository widgetContentRepository;

	@Override
	public Map<String, ?> loadContent(List<String> contentFields, Map<Filter, Sort> filterSortMapping, WidgetOptions widgetOptions, int limit) {

		validateFilterSortMapping(filterSortMapping);

		validateContentFields(contentFields);

		Filter filter = GROUP_FILTERS.apply(filterSortMapping.keySet());

		Sort sort = GROUP_SORTS.apply(filterSortMapping.values());

		String contentField = contentFields.get(0);
		List<ChartStatisticsContent> content = widgetContentRepository.casesTrendStatistics(filter, contentField, sort, limit);

		return CollectionUtils.isEmpty(content) ? emptyMap() : calculateStatistics(widgetOptions, content, contentField, sort);
	}

	private Map<String, ?> calculateStatistics(WidgetOptions widgetOptions, List<ChartStatisticsContent> content, String contentField,
			Sort sort) {
		String timeLineOption = WidgetOptionUtil.getValueByKey(TIMELINE, widgetOptions);

		if (StringUtils.isNotBlank(timeLineOption)) {
			Optional<Period> period = Period.findByName(timeLineOption);
			if (period.isPresent()) {
				Map<String, ChartStatisticsContent> statistics = maxByDate(content, period.get(), contentField);
				calculateDelta(statistics, sort, contentField);
				return singletonMap(RESULT, statistics);
			}

		}

		return singletonMap(RESULT, content);
	}

	private void calculateDelta(Map<String, ChartStatisticsContent> statistics, Sort sort, String contentField) {

		if (sort.get().anyMatch(Sort.Order::isAscending)) {
			ArrayList<String> keys = new ArrayList<>(statistics.keySet());
			/* Last element in map */
			int previous = Integer.parseInt(statistics.get(keys.get(keys.size() - 1)).getValues().get(contentField));
			/* Iteration in reverse order */
			for (int i = keys.size() - 1; i >= 0; i--) {
				int current = Integer.parseInt(statistics.get(keys.get(i)).getValues().get(contentField));
				statistics.get(keys.get(i)).getValues().put(DELTA, String.valueOf(current - previous));
				previous = current;
			}
		} else {
			int previousValue = Integer.parseInt(new ArrayList<>(statistics.values()).get(0).getValues().get(contentField));
			for (ChartStatisticsContent content : statistics.values()) {
				Map<String, String> values = content.getValues();
				int currentValue = Integer.parseInt(values.get(contentField));
				values.put(DELTA, String.valueOf(currentValue - previousValue));
				previousValue = currentValue;
			}
		}

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
	 * <p>
	 * The value of content field should not be empty
	 * Content fields should contain only 1 value
	 * Content field value should match the pattern {@link com.epam.ta.reportportal.core.widget.util.ContentFieldPatternConstants#EXECUTIONS_TOTAL_REGEX}
	 *
	 * @param contentFields List of provided content.
	 */
	private void validateContentFields(List<String> contentFields) {
		BusinessRule.expect(CollectionUtils.isNotEmpty(contentFields), equalTo(true))
				.verify(ErrorType.BAD_REQUEST_ERROR, "Content fields should not be empty");
		BusinessRule.expect(contentFields.size(), equalTo(1))
				.verify(ErrorType.BAD_REQUEST_ERROR, "Test cases growth widget content fields should contain only 1 value");
		BusinessRule.expect(ContentFieldMatcherUtil.match(EXECUTIONS_TOTAL_REGEX, contentFields), equalTo(true))
				.verify(ErrorType.BAD_REQUEST_ERROR, "Bad content fields format");
	}

}
