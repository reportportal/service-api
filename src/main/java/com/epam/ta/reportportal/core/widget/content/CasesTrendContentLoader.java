/*
 * Copyright 2016 EPAM Systems
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
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.UNABLE_LOAD_WIDGET_CONTENT;

/**
 * ContentLoader implementation for <b>Cases Trend Gadget</b>.<br> Content represents <b>total</b> values of cases at time-period with<br>
 * difference delta between current and previous periods.
 *
 * @author Andrei_Ramanchuk
 */
@Service("CasesTrendContentLoader")
public class CasesTrendContentLoader extends StatisticBasedContentLoader implements IContentLoadingStrategy {

	// Works for launch filter target only
	private static final String COLLECTION = "launch";
	private static final String SORT_FIELD = "start_time";
	private static final String DELTA = "delta";

	@Autowired
	private LaunchRepository launchRepository;

	@Override
	public Map<String, List<ChartObject>> loadContent(String projectName, Filter filter, Sort sorting, int quantity,
			List<String> contentFields, List<String> metaDataFields, Map<String, List<String>> options) {
		expect(metaDataFields == null || metaDataFields.isEmpty(), equalTo(false)).verify(UNABLE_LOAD_WIDGET_CONTENT,
				"Metadata fields should exist for providing content."
		);

		/*
		 * Return empty map if filter target TestItem. Chart cannot show trend
		 * for those filters so its unnecessary to proceed it.
		 */
		if (filter.getTarget().getCanonicalName().equalsIgnoreCase(TestItem.class.getName())) {
			return Collections.emptyMap();
		}

		List<String> allFields = ImmutableList.<String>builder().addAll(contentFields).addAll(metaDataFields).build();
		StatisticsDocumentHandler handler = new StatisticsDocumentHandler(contentFields, metaDataFields);

		// start_time is required sorting for trend
		// charts then setup it before loading chart data
		Sort.Order order = sorting.iterator().next();
		if (order.getProperty().equalsIgnoreCase(SORT_FIELD)) {
			sorting = order.isAscending() ? new Sort(Sort.Direction.ASC, SORT_FIELD) : new Sort(Sort.Direction.DESC, SORT_FIELD);
		}
		launchRepository.loadWithCallback(filter, sorting, quantity, allFields, handler, COLLECTION);
		List<ChartObject> rawData = handler.getResult();

		Map<String, List<ChartObject>> result = new LinkedHashMap<>();
		if ((options.get(TIMELINE) != null) && (Period.findByName(options.get(TIMELINE).get(0)) != null)) {
			Map<String, List<ChartObject>> timeline = maxByDate(rawData, Period.findByName(options.get(TIMELINE).get(0)),
					getTotalFieldName()
			);
			result.putAll(calculateGroupedDiffs(timeline, sorting));
		} else {
			result = calculateDiffs(rawData, sorting);
		}
		return mapRevert(result, sorting);
	}

	/**
	 * Calculation of group differences between TOTAL parameter of items
	 *
	 * @param initial
	 * @param sorting
	 * @return
	 */
	private Map<String, List<ChartObject>> calculateGroupedDiffs(Map<String, List<ChartObject>> initial, Sort sorting) {
		if (initial.keySet().isEmpty()) {
			return Collections.emptyMap();
		}

		if (sorting.toString().contains(Sort.Direction.ASC.name())) {
			ArrayList<String> keys = new ArrayList<>(initial.keySet());
			/* Last element in map */
			Integer previous = Integer.valueOf(initial.get(keys.get(keys.size() - 1)).get(0).getValues().get(getTotalFieldName()));
			/* Iteration in reverse order */
			for (int i = keys.size() - 1; i >= 0; i--) {
				Integer current = Integer.valueOf(initial.get(keys.get(i)).get(0).getValues().get(getTotalFieldName()));
				initial.get(keys.get(i)).get(0).getValues().put(DELTA, String.valueOf(current - previous));
				previous = current;
			}
		} else {
			Integer previous = Integer.valueOf(initial.get(initial.keySet().iterator().next()).get(0).getValues().get(getTotalFieldName()));
			for (Map.Entry<String, List<ChartObject>> entry : initial.entrySet()) {
				Integer current = Integer.valueOf(entry.getValue().get(0).getValues().get(getTotalFieldName()));
				entry.getValue().get(0).getValues().put(DELTA, String.valueOf(current - previous));
				previous = current;
			}
		}
		return initial;
	}

	/**
	 * Calculation of differences in one group or in overall array of items
	 *
	 * @param initial
	 * @param sorting
	 * @return
	 */
	private Map<String, List<ChartObject>> calculateDiffs(List<ChartObject> initial, Sort sorting) {
		if (initial.isEmpty()) {
			return new HashMap<>();
		}

		if (sorting.toString().contains(Sort.Direction.DESC.name())) {
			Integer previous = Integer.valueOf(initial.get(initial.size() - 1).getValues().get(getTotalFieldName()));
			for (int i = initial.size() - 1; i >= 0; i--) {
				Integer current = Integer.valueOf(initial.get(i).getValues().get(getTotalFieldName()));
				initial.get(i).getValues().put(DELTA, String.valueOf(current - previous));
				previous = current;
			}
		} else {
			Integer previous = Integer.valueOf(initial.get(0).getValues().get(getTotalFieldName()));
			for (ChartObject anInitial : initial) {
				Integer current = Integer.valueOf(anInitial.getValues().get(getTotalFieldName()));
				anInitial.getValues().put(DELTA, String.valueOf(current - previous));
				previous = current;
			}
		}
		Map<String, List<ChartObject>> result = new HashMap<>();
		result.put(RESULT, initial);
		return result;
	}

	/**
	 * Revert results if in descending order as it is a trend chart.
	 *
	 * @param input - callback output result
	 * @return - transformed Map with reverse ordered elements
	 */
	private Map<String, List<ChartObject>> mapRevert(Map<String, List<ChartObject>> input, Sort sorting) {
		if (null != sorting) {
			Sort.Order order = sorting.iterator().next();
			if (order.getProperty().equalsIgnoreCase(SORT_FIELD) && order.isAscending()) {
				return input;
			}
		}
		input.forEach((key, value) -> Collections.reverse(value));
		return input;
	}
}