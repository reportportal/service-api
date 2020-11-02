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
import com.epam.ta.reportportal.core.widget.content.LoadContentStrategy;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.entity.widget.content.ChartStatisticsContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_START_TIME;
import static com.epam.ta.reportportal.commons.querygen.constant.LaunchCriteriaConstant.CRITERIA_LAUNCH_NUMBER;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.RESULT;
import static com.epam.ta.reportportal.core.widget.util.WidgetFilterUtil.GROUP_FILTERS;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;

/**
 * @author Pavel Bortnik
 */
@Service
public class LaunchesComparisonContentLoader implements LoadContentStrategy {

	@Autowired
	private WidgetContentRepository widgetContentRepository;

	@Override
	public Map<String, ?> loadContent(List<String> contentFields, Map<Filter, Sort> filterSortMapping, WidgetOptions widgetOptions,
			int limit) {

		Filter filter = GROUP_FILTERS.apply(filterSortMapping.keySet());
		Sort sort = Sort.by(Sort.Order.desc(CRITERIA_START_TIME), Sort.Order.desc(CRITERIA_LAUNCH_NUMBER));

		List<ChartStatisticsContent> result = widgetContentRepository.launchesComparisonStatistics(filter, contentFields, sort, limit);

		if (result.isEmpty()) {
			return emptyMap();
		} else {
			Collections.reverse(result);
			return singletonMap(RESULT, result);
		}
	}

}
