/*
 * Copyright 2018 EPAM Systems
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
 *
 */

package com.epam.ta.reportportal.core.widget.content;

import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.database.StatisticsDocumentHandler;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.aggregation.GroupingOperation;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mongodb.DBObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.core.widget.content.StatisticBasedContentLoader.RESULT;
import static com.epam.ta.reportportal.core.widget.content.WidgetContentProvider.TO_UI_STYLE;

/**
 * @author Pavel Bortnik
 */
@Service
public class GroupingContentLoader implements IContentLoadingStrategy {

	private static final String GROUPING_LAUNCHES = "grouping_launches";
	private static final String GROUPING_BY = "grouping_by";

	@Autowired
	private LaunchRepository launchRepository;

	@Override
	public Map<String, List<ChartObject>> loadContent(String projectName, Filter filter, Sort sorting, int quantity,
			List<String> contentFields, List<String> metaDataFields, Map<String, List<String>> widgetOptions) {

		Optional<GroupingOption> groupingOption = GroupingOption.getByValue(widgetOptions.get(GROUPING_LAUNCHES).get(0));
		Optional<GroupingOperation.GroupingPeriod> groupingBy = GroupingOperation.GroupingPeriod.getByValue(
				widgetOptions.get(GROUPING_BY).get(0));

		expect(groupingOption, Predicates.isPresent()).verify(ErrorType.INCORRECT_REQUEST,
				"Incorrect grouping option. Supported are: " + Arrays.stream(GroupingOption.values())
						.map(GroupingOption::getValue)
						.collect(Collectors.joining(","))
		);

		expect(groupingBy, Predicates.isPresent()).verify(ErrorType.INCORRECT_REQUEST, "Incorrect grouping period. Supported are: " + Arrays
				.stream(GroupingOperation.GroupingPeriod.values())
				.map(GroupingOperation.GroupingPeriod::getValue)
				.collect(Collectors.joining(",")));

		List<String> fieldsForHandling = contentFields.stream().map(TO_UI_STYLE).collect(Collectors.toList());
		fieldsForHandling.add("start_time");

		StatisticsDocumentHandler handler = new StatisticsDocumentHandler(fieldsForHandling, metaDataFields);

		List<DBObject> aggregationResults = Lists.newArrayList();
		switch (groupingOption.get()) {
			case ALL:
				aggregationResults = launchRepository.findGroupedBy(filter, contentFields, groupingBy.get(), quantity);
				break;
			case LATEST:
				aggregationResults = launchRepository.findLatestGroupedBy(filter, contentFields, groupingBy.get(), quantity);
				break;
		}
		if (aggregationResults.isEmpty()) {
			return Collections.emptyMap();
		}
		aggregationResults.forEach(handler::processDocument);
		return ImmutableMap.<String, List<ChartObject>>builder().put(RESULT, handler.getResult()).build();
	}

	private enum GroupingOption {
		ALL("all"),
		LATEST("latest");

		private String value;

		GroupingOption(String value) {
			this.value = value;
		}

		private static Optional<GroupingOption> getByValue(String value) {
			return Arrays.stream(values()).filter(it -> it.value.equalsIgnoreCase(value)).findFirst();
		}

		public String getValue() {
			return value;
		}
	}
}
