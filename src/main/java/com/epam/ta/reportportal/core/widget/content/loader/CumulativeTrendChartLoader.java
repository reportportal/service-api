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

import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.core.widget.content.MultilevelLoadContentStrategy;
import com.epam.ta.reportportal.core.widget.util.ContentFieldMatcherUtil;
import com.epam.ta.reportportal.core.widget.util.WidgetOptionUtil;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.entity.widget.content.CumulativeTrendChartEntry;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.querygen.constant.ItemAttributeConstant.CRITERIA_ITEM_ATTRIBUTE_KEY;
import static com.epam.ta.reportportal.commons.querygen.constant.ItemAttributeConstant.CRITERIA_ITEM_ATTRIBUTE_VALUE;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.ATTRIBUTES;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.RESULT;
import static com.epam.ta.reportportal.core.widget.util.ContentFieldPatternConstants.COMBINED_CONTENT_FIELDS_REGEX;
import static com.epam.ta.reportportal.core.widget.util.WidgetFilterUtil.GROUP_FILTERS;
import static com.epam.ta.reportportal.core.widget.util.WidgetFilterUtil.GROUP_SORTS;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class CumulativeTrendChartLoader implements MultilevelLoadContentStrategy {

	@Autowired
	private WidgetContentRepository widgetContentRepository;

	@Override
	public Map<String, Object> loadContent(List<String> contentFields, Map<Filter, Sort> filterSortMapping, WidgetOptions widgetOptions,
			String[] attributes, Map<String, String> params, int limit) {

		validateFilterSortMapping(filterSortMapping);

		validateContentFields(contentFields);

		Filter filter = GROUP_FILTERS.apply(filterSortMapping.keySet());

		Sort sort = GROUP_SORTS.apply(filterSortMapping.values());

		List<CumulativeTrendChartEntry> content;

		List<String> storedAttributes = WidgetOptionUtil.getListByKey(ATTRIBUTES, widgetOptions);

		if (ArrayUtils.isEmpty(attributes)) {
			content = widgetContentRepository.cumulativeTrendStatistics(filter,
					contentFields,
					sort,
					storedAttributes.size() > 0 ? storedAttributes.get(0) : null,
					storedAttributes.size() > 1 ? storedAttributes.get(1) : null,
					limit
			);
		} else {
			Map<String, String> requestAttributesMapping = getAttributesMapping(attributes);

			expect(storedAttributes.containsAll(requestAttributesMapping.keySet()),
					Predicate.isEqual(true)
			).verify(ErrorType.BAD_REQUEST_ERROR, ATTRIBUTES);

			requestAttributesMapping.forEach((key, value) -> filter.withCondition(FilterCondition.builder()
					.withSearchCriteria(CRITERIA_ITEM_ATTRIBUTE_KEY)
					.withCondition(Condition.HAS)
					.withValue(key)
					.build())
					.withCondition(FilterCondition.builder()
							.withSearchCriteria(CRITERIA_ITEM_ATTRIBUTE_VALUE)
							.withCondition(Condition.HAS)
							.withValue(value)
							.build()));
			content = widgetContentRepository.cumulativeTrendStatistics(filter, contentFields, sort, storedAttributes.get(1), null, limit);
		}
		return content.isEmpty() ? emptyMap() : singletonMap(RESULT, content);
	}

	/**
	 * Mapping should not be empty
	 *
	 * @param filterSortMapping Map of ${@link Filter} for query building as key and ${@link Sort} as value for each filter
	 */
	private void validateFilterSortMapping(Map<Filter, Sort> filterSortMapping) {
		expect(MapUtils.isNotEmpty(filterSortMapping), equalTo(true)).verify(ErrorType.BAD_REQUEST_ERROR,
				"Filter-Sort mapping should not be empty"
		);
	}

	/**
	 * Validate provided content fields.
	 * The value of content field should not be empty
	 * All content fields should match the pattern {@link com.epam.ta.reportportal.core.widget.util.ContentFieldPatternConstants#COMBINED_CONTENT_FIELDS_REGEX}
	 *
	 * @param contentFields List of provided content.
	 */
	private void validateContentFields(List<String> contentFields) {
		expect(CollectionUtils.isNotEmpty(contentFields), equalTo(true)).verify(ErrorType.BAD_REQUEST_ERROR,
				"Content fields should not be empty"
		);
		expect(ContentFieldMatcherUtil.match(COMBINED_CONTENT_FIELDS_REGEX, contentFields),
				equalTo(true)
		).verify(ErrorType.BAD_REQUEST_ERROR, "Bad content fields format");
	}

	private Map<String, String> getAttributesMapping(String[] attributes) {
		return Arrays.stream(attributes)
				.collect(Collectors.toMap(it -> StringUtils.substringBefore(it, ":"), it -> StringUtils.substringAfter(it, ":")));
	}
}
