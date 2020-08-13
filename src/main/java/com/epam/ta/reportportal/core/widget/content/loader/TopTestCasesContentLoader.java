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
import com.epam.ta.reportportal.core.widget.content.LoadContentStrategy;
import com.epam.ta.reportportal.core.widget.util.WidgetOptionUtil;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.ws.converter.converters.LaunchConverter;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_NAME;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.*;
import static com.epam.ta.reportportal.core.widget.util.WidgetFilterUtil.GROUP_FILTERS;
import static java.util.Optional.ofNullable;

/**
 * Content loader for {@link com.epam.ta.reportportal.entity.widget.WidgetType#TOP_TEST_CASES}
 *
 * @author Pavel Bortnik
 */
@Service
public class TopTestCasesContentLoader implements LoadContentStrategy {

	private final LaunchRepository launchRepository;

	private final LaunchConverter launchConverter;

	private final WidgetContentRepository widgetContentRepository;

	@Autowired
	public TopTestCasesContentLoader(LaunchRepository launchRepository, LaunchConverter launchConverter,
			WidgetContentRepository widgetContentRepository) {
		this.launchRepository = launchRepository;
		this.launchConverter = launchConverter;
		this.widgetContentRepository = widgetContentRepository;
	}

	@Override
	public Map<String, ?> loadContent(List<String> contentFields, Map<Filter, Sort> filterSortMapping, WidgetOptions widgetOptions,
			int limit) {
		String criteria = contentFields.get(0);
		Filter filter = GROUP_FILTERS.apply(filterSortMapping.keySet())
				.withCondition(new FilterCondition(Condition.EQUALS,
						false,
						WidgetOptionUtil.getValueByKey(LAUNCH_NAME_FIELD, widgetOptions),
						CRITERIA_NAME
				));

		return launchRepository.findLatestByFilter(filter)
				.map(it -> Pair.of(it,
						widgetContentRepository.topItemsByCriteria(filter,
								criteria,
								limit,
								ofNullable(widgetOptions.getOptions()
										.get(INCLUDE_METHODS)).map(v -> BooleanUtils.toBoolean(String.valueOf(v))).orElse(false)
						)
				))
				.filter(it -> !it.getRight().isEmpty())
				.map(it -> (Map<String, ?>) ImmutableMap.<String, Object>builder().put(LATEST_LAUNCH,
						launchConverter.TO_RESOURCE.apply(it.getLeft())
				).put(RESULT, it.getRight()).build())
				.orElse(Collections.emptyMap());
	}

}
