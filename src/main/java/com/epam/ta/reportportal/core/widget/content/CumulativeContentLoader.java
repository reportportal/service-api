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

package com.epam.ta.reportportal.core.widget.content;

import com.epam.ta.reportportal.database.StatisticsDocumentHandler;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.core.widget.content.StatisticBasedContentLoader.RESULT;
import static com.epam.ta.reportportal.core.widget.content.WidgetContentProvider.TO_UI_STYLE;

/**
 * Content loader for cumulative trend chart widget. Content is based
 * on specified filter and tag prefix in widget options. They are presented
 * as sum by specified fields and tag with number. Filter's sorting is
 * not applied to results. They are fixed in ASC by tag number.
 *
 * @author Pavel Bortnik
 */

@Service
public class CumulativeContentLoader implements IContentLoadingStrategy {

	private static final String TAG_PREFIX = "prefix";

	@Autowired
	private LaunchRepository launchRepository;

	@Override
	public Map<String, List<ChartObject>> loadContent(String projectName, Filter filter, Sort sorting, int quantity,
			List<String> contentFields, List<String> metaDataFields, Map<String, List<String>> widgetsOptions) {
		Map<String, List<ChartObject>> emptyResult = Collections.emptyMap();
		List<String> options = widgetsOptions.get(TAG_PREFIX);
		expect(options, notNull()).verify(ErrorType.BAD_REQUEST_ERROR, "widgetOptions");
		expect(options.isEmpty(), equalTo(false)).verify(ErrorType.BAD_REQUEST_ERROR, TAG_PREFIX);

		List<String> fields = contentFields.stream().map(TO_UI_STYLE).collect(Collectors.toList());
		StatisticsDocumentHandler handler = new StatisticsDocumentHandler(fields, metaDataFields);

		launchRepository.cumulativeStatisticsGroupedByTag(filter, contentFields, quantity, options.get(0), handler);

		List<ChartObject> result = handler.getResult();
		if (null == result) {
			return emptyResult;
		}
		Collections.reverse(result);
		return ImmutableMap.<String, List<ChartObject>>builder().put(RESULT, result).build();
	}
}
