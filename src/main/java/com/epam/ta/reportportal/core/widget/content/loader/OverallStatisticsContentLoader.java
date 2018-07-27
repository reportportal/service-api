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
import com.epam.ta.reportportal.core.widget.content.LoadContentStrategy;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.widget.WidgetOption;
import com.epam.ta.reportportal.entity.widget.content.StatisticsContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.core.widget.content.WidgetContentUtils.GROUP_CONTENT_FIELDS;

/**
 * @author Pavel Bortnik
 */
@Service
public class OverallStatisticsContentLoader implements LoadContentStrategy {

	@Autowired
	private WidgetContentRepository widgetContentRepository;

	@Override
	public Map<String, ?> loadContent(List<String> contentFields, Filter filter, Set<WidgetOption> widgetOptions, int limit) {
		boolean latestMode = widgetOptions.stream().anyMatch(it -> it.getWidgetOption().equalsIgnoreCase(LATEST_OPTION));
		List<StatisticsContent> content = widgetContentRepository.overallStatisticsContent(
				filter,
				GROUP_CONTENT_FIELDS.apply(contentFields),
				latestMode,
				limit
		);
		Map<String, List<StatisticsContent>> result = new HashMap<>();
		result.put(RESULT, content);
		return result;
	}
}
