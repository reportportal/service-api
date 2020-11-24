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
import com.epam.ta.reportportal.core.widget.util.WidgetOptionUtil;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.entity.widget.content.ChartStatisticsContent;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.RESULT;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.TIMELINE;
import static com.epam.ta.reportportal.core.widget.util.WidgetFilterUtil.GROUP_FILTERS;
import static com.epam.ta.reportportal.core.widget.util.WidgetFilterUtil.GROUP_SORTS;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.*;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;

/**
 * @author Pavel Bortnik
 */
@Service
public class ChartInvestigatedContentLoader extends AbstractStatisticsContentLoader implements LoadContentStrategy {

	@Autowired
	private WidgetContentRepository widgetContentRepository;

	@Override
	public Map<String, ?> loadContent(List<String> contentFields, Map<Filter, Sort> filterSortMapping, WidgetOptions widgetOptions,
			int limit) {

		Filter filter = GROUP_FILTERS.apply(filterSortMapping.keySet());

		Sort sort = GROUP_SORTS.apply(filterSortMapping.values());

		String timeLineOption = WidgetOptionUtil.getValueByKey(TIMELINE, widgetOptions);

		if (StringUtils.isNotBlank(timeLineOption)) {
			Optional<Period> period = Period.findByName(timeLineOption);
			if (period.isPresent()) {
				Map<String, ChartStatisticsContent> statistics = groupByDate(widgetContentRepository.timelineInvestigatedStatistics(filter,
						sort,
						limit
				), period.get());
				return MapUtils.isEmpty(statistics) ? emptyMap() : calculateInvestigatedPercentage(statistics);
			}

		}

		List<ChartStatisticsContent> content = widgetContentRepository.investigatedStatistics(filter, sort, limit);

		return CollectionUtils.isEmpty(content) ? emptyMap() : singletonMap(RESULT, content);
	}

	private Map<String, ?> calculateInvestigatedPercentage(Map<String, ChartStatisticsContent> investigatedStatistics) {

		investigatedStatistics.values().forEach(c -> {
			Map<String, String> values = c.getValues();
			BigDecimal divisor = BigDecimal.valueOf(Double.parseDouble(values.get(INVESTIGATED)));
			if (0 != divisor.intValue()) {
				values.put(TO_INVESTIGATE,
						String.valueOf(BigDecimal.valueOf(PERCENTAGE_MULTIPLIER * Double.parseDouble(values.get(TO_INVESTIGATE)))
								.divide(divisor, 2, RoundingMode.FLOOR)
								.doubleValue())
				);
				values.put(INVESTIGATED, String.valueOf(PERCENTAGE_MULTIPLIER - Double.parseDouble(values.get(TO_INVESTIGATE))));
			} else {
				values.put(INVESTIGATED, "0");
				values.put(TO_INVESTIGATE, "0");
			}
		});

		return singletonMap(RESULT, investigatedStatistics);
	}

}
