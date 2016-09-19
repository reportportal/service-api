/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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

import static java.util.stream.Collectors.toMap;

import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.core.item.history.ITestItemsHistoryService;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.entity.widget.ContentOptions;
import com.epam.ta.reportportal.database.search.CriteriaMapFactory;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import com.google.common.collect.Lists;

/**
 * Most failed test-cases widget content loader<br>
 * <b>Slow widget because history based</b>
 * 
 * @author Andrei_Ramanchuk
 */
@Service("MostFailedTestCasesFilterStrategy")
public class MostFailedTestCasesFilterStrategy implements BuildFilterStrategy {
	private static final String ALL_RUNS = "All runs";
	private static final String FAILED = "Failed";
	private static final String AFFECTED_BY = "Affected by";
	private static final String LAST_FAIL_CAPTION = "Last Failure";

	private static final String LAST_FOUND_LAUNCH = "lastLaunch";

	private static final String LAUNCH_NAME_FIELD = "launchNameFilter";

	private static final int ITEMS_COUNT_VALUE = 20;

	@Autowired
	private LaunchRepository launchRepository;

	@Autowired
	private TestItemRepository itemRepository;

	@Autowired
	private ITestItemsHistoryService historyServiceStrategy;

	@Autowired
	private CriteriaMapFactory criteriaMapFactory;

	@Override
	public Map<String, List<ChartObject>> buildFilterAndLoadContent(UserFilter userFilter, ContentOptions contentOptions,
			String projectName) {
		/*
		 * Load content without building filter cause we don't need detailed
		 * information per item here
		 */
		Map<String, List<ChartObject>> result = new HashMap<>();
		/*
		 * Return empty response for absent filtering launch name parameter
		 */
		if (contentOptions.getWidgetOptions() == null || contentOptions.getWidgetOptions().get(LAUNCH_NAME_FIELD) == null)
			return result;
		Optional<Launch> lastLaunchForProject = launchRepository.findLastLaunch(projectName,
				contentOptions.getWidgetOptions().get(LAUNCH_NAME_FIELD).get(0), Mode.DEFAULT.name());
		if (!lastLaunchForProject.isPresent()) {
			return result;
		}

		String criteria = new StatisticBasedContentLoader().getSystemIssueFieldName();
		if (null != contentOptions.getContentFields() && contentOptions.getContentFields().size() >= 1) {
			criteria = WidgetContentProvider.transformToDBStyle(criteriaMapFactory.getCriteriaMap(Launch.class),
					contentOptions.getContentFields()).get(0);
		}

		List<Launch> launchHistory = historyServiceStrategy.loadLaunches(contentOptions.getItemsCount(), lastLaunchForProject.get().getId(),
				projectName, false);
		if (launchHistory.isEmpty()) {
			return result;
		}
		Map<String, String> dbProcessor = itemRepository.getMostFailedTestCases(launchHistory, criteria);

		Map<String, ComplexValue> dbProcessed = this.mapAggregationConvert(dbProcessor);
		// Hack due history method missed launch Name field
		// TODO review launches formation or wait till widgets refactoring
		launchHistory.get(0).setName(contentOptions.getWidgetOptions().get(LAUNCH_NAME_FIELD).get(0));
		return databaseDataConverter(sortByValue(dbProcessed), contentOptions.getItemsCount(), launchHistory.get(0));
	}

	/**
	 * Returned data from database converter in UI style charts
	 * 
	 * @param dbData
	 * @return
	 */
	private static Map<String, List<ChartObject>> databaseDataConverter(Map<String, ComplexValue> dbData, int launches, Launch last) {
		DecimalFormat formatter = new DecimalFormat("###.##");
		Map<String, List<ChartObject>> result = new LinkedHashMap<>();
		if (dbData.keySet().isEmpty())
			return result;

		for (Entry<String, ComplexValue> pair : dbData.entrySet()) {
			ChartObject object = new ChartObject();
			Map<String, String> values = new HashMap<>();
			values.put(ALL_RUNS, String.valueOf(pair.getValue().getTotal()));
			values.put(FAILED, String.valueOf(pair.getValue().getCount()));
			double value = (double) pair.getValue().getCount() / pair.getValue().getTotal() * 100;
			values.put(AFFECTED_BY, String.valueOf(formatter.format(value)));
			values.put(LAST_FAIL_CAPTION, String.valueOf(pair.getValue().getStartTime()));
			object.setValues(values);
			result.put(pair.getKey(), Lists.newArrayList(object));
		}

		// Add last launch attr as last element
		ChartObject lastLaunch = new ChartObject();
		lastLaunch.setName(last.getName());
		lastLaunch.setNumber(last.getNumber().toString());
		lastLaunch.setId(last.getId());
		result.put(LAST_FOUND_LAUNCH, Lists.newArrayList(lastLaunch));
		return result;
	}

	/**
	 * Sorting result map from data by counter<br>
	 * <b>WARNING: do not use method somewhere else except here cause complex
	 * map value and top-20 limit</b>
	 * 
	 * @param map
	 * @return
	 */
	@SuppressWarnings("hiding")
	private static <K, ComplexValue extends Comparable<? super ComplexValue>> LinkedHashMap<K, ComplexValue> sortByValue(
			Map<K, ComplexValue> map) {
		List<Map.Entry<K, ComplexValue>> list = new LinkedList<>(map.entrySet());
		Collections.sort(list, (object1, object2) -> (object1.getValue()).compareTo(object2.getValue()));
		// Splitter for TOP-20 values included
		LinkedHashMap<K, ComplexValue> result = new LinkedHashMap<>();
		int counter = 0;
		for (Map.Entry<K, ComplexValue> entry : list) {
			if (counter < ITEMS_COUNT_VALUE) {
				result.put(entry.getKey(), entry.getValue());
				counter++;
			} else
				break;
		}
		return result;
	}

	/**
	 * Convert Aggregation result into readable map content
	 * 
	 * @return
	 */
	private Map<String, ComplexValue> mapAggregationConvert(Map<String, String> input) {
		return input.entrySet().stream().collect(toMap(Entry::getKey, entry -> valueConstrunt(entry.getValue())));
	}

	private ComplexValue valueConstrunt(String mapValue) {
		String[] split = mapValue.split("#");
		int value = Integer.parseInt(split[0]);
		long date = Long.parseLong(split[1]);
		int total = Integer.parseInt(split[2]);
		return new ComplexValue(value, date, total);
	}

	/**
	 * Representation of complex value of MongoDB Map Aggregation result
	 * 
	 * @author Andrei_Ramanchuk
	 */
	static class ComplexValue implements Comparable<ComplexValue> {
		private Integer count;
		private Long startTime;
		private Integer total;

		public ComplexValue(int counter, long date, int total) {
			this.count = counter;
			this.startTime = date;
			this.total = total;
		}

		public void setCount(int value) {
			this.count = value;
		}

		public Integer getCount() {
			return count;
		}

		public void setStartTime(long value) {
			this.startTime = value;
		}

		public Long getStartTime() {
			return startTime;
		}

		public void setTotal(int value) {
			this.total = value;
		}

		public Integer getTotal() {
			return total;
		}

		@Override
		// Comparator for AffectedBy sorting
		public int compareTo(ComplexValue candidate) {
			double compareCase = (double) candidate.getCount() / candidate.getTotal() * 100;
			// int compareCase = candidate.getCount();
			double thisCase = (double) this.getCount() / this.getTotal() * 100;
			return (int) Math.round(compareCase - thisCase);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			ComplexValue that = (ComplexValue) o;
			return Objects.equals(count, that.count) && Objects.equals(startTime, that.startTime) && Objects.equals(total, that.total);
		}

		@Override
		public int hashCode() {
			return Objects.hash(count, startTime, total);
		}
	}
}