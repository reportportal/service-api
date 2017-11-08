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

import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import org.joda.time.DateTime;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Base class for content loaders {@link IContentLoadingStrategy}<br>
 * of statistic using charts.
 *
 * @author Andrei_Ramanchuk
 */
public class StatisticBasedContentLoader {

	public static final String RESULT = "result";
	public static final String TIMELINE = "timeline";
	public static final String TOTAL_FIELD = "total";
	public static final String PASSED_FIELD = "passed";

	private static final String PASSED = "statistics.executionCounter.passed";
	private static final String FAILED = "statistics.executionCounter.failed";
	private static final String SKIPPED = "statistics.executionCounter.skipped";
	private static final String TOTAL = "statistics.executionCounter.total";
	private static final List<String> STATZZ = Arrays.asList(PASSED, FAILED, SKIPPED);

	private static final String TO_INVESTIGATE = "statistics.issueCounter.toInvestigate.total";
	private static final String PRODUCT_BUG = "statistics.issueCounter.productBug.total";
	private static final String AUTOMATION_BUG = "statistics.issueCounter.automationBug.total";
	private static final String SYSTEM_ISSUE = "statistics.issueCounter.systemIssue.total";
	private static final String NO_DEFECT = "statistics.issueCounter.noDefect.total";
	private static final List<String> BUGZZ = Arrays.asList(PRODUCT_BUG, AUTOMATION_BUG, SYSTEM_ISSUE, NO_DEFECT);

	private static final String DATE_PATTERN = "yyyy-MM-dd";

	static final String LATEST_MODE = "latest";

	/**
	 * Return collection name using input class object
	 *
	 * @param type
	 * @return
	 */
	public static String getCollectionName(Class<?> type) {
		char[] chars = type.getSimpleName().toCharArray();
		chars[0] = Character.toLowerCase(chars[0]);
		return new String(chars);
	}

	public List<String> getExecutionStatFields() {
		return STATZZ;
	}

	public List<String> getIssueStatFields() {
		return BUGZZ;
	}

	public String getPassedFieldName() {
		return PASSED;
	}

	public String getFailedFieldName() {
		return FAILED;
	}

	public String getSkippedFieldName() {
		return SKIPPED;
	}

	public String getTotalFieldName() {
		return TOTAL;
	}

	public String getToInvestigateFieldName() {
		return TO_INVESTIGATE;
	}

	public String getProductBugFieldName() {
		return PRODUCT_BUG;
	}

	public String getAutomationBugFieldName() {
		return AUTOMATION_BUG;
	}

	public String getSystemIssueFieldName() {
		return SYSTEM_ISSUE;
	}

	public String getNoDefectFieldName() {
		return NO_DEFECT;
	}

	/**
	 * Return lists of objects grouped by specified period
	 *
	 * @param input
	 * @param period
	 * @return
	 */
	public Map<String, List<ChartObject>> groupByDate(List<ChartObject> input, Period period) {
		HashMap<String, List<ChartObject>> result = new LinkedHashMap<>();
		Map<String, ChartObject> range = buildRange(input, period);

		/* Fill ranged items with calculated */
		for (ChartObject anInput : input) {
			String group = new DateTime(Long.valueOf(anInput.getStartTime())).toString(DATE_PATTERN);
			ChartObject axisObject = range.get(group);
			Map<String, String> values = axisObject.getValues();
			Map<String, String> updated = new HashMap<>();
			for (String key : anInput.getValues().keySet()) {
				updated.put(key, String.valueOf(Double.valueOf(anInput.getValues().get(key)) + Double.valueOf(values.get(key))));
			}
			axisObject.setValues(updated);
			range.put(group, axisObject);
		}

		range.keySet().forEach(date -> result.put(date, Collections.singletonList(range.get(date))));
		return result;
	}

	/**
	 * Return grouped objects with MAX element in each group by specified
	 * criteria
	 *
	 * @param input
	 * @param period
	 * @param maxSeries
	 * @return
	 */
	public Map<String, List<ChartObject>> maxByDate(List<ChartObject> input, Period period, String maxSeries) {
		final Function<ChartObject, String> chartObjectToDate = chartObject -> new DateTime(
				Long.valueOf(chartObject.getStartTime())).toString(DATE_PATTERN);
		final BinaryOperator<ChartObject> chartObjectReducer = (o1, o2) -> Integer.valueOf(o1.getValues().get(maxSeries)) > Integer.valueOf(o2.getValues().get(maxSeries)) ?
				o1 :
				o2;
		final Map<String, Optional<ChartObject>> groupByDate = input.stream()
				.sorted(Comparator.comparing(ChartObject::getStartTime))
				.collect(Collectors.groupingBy(chartObjectToDate, LinkedHashMap::new, Collectors.reducing(chartObjectReducer)));
		final Map<String, ChartObject> range = buildRange(input, period);
		final LinkedHashMap<String, List<ChartObject>> result = new LinkedHashMap<>();
		// used forEach cause aspectj compiler can't infer types properly
		range.forEach((key, value) -> result.put(key,
				Collections.singletonList(groupByDate.getOrDefault(key, Optional.of(createChartObject(input.get(0)))).get())
		));
		return result;
	}

	private ChartObject createChartObject(ChartObject input) {
		final ChartObject chartObject = new ChartObject();
		chartObject.setValues(input.getValues().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> "0")));
		return chartObject;
	}

	/**
	 * Create ranged empty timeline billet
	 *
	 * @param base
	 * @param period
	 * @return
	 */
	private Map<String, ChartObject> buildRange(List<ChartObject> base, Period period) {
		final LongSummaryStatistics statistics = base.stream().mapToLong(object -> Long.valueOf(object.getStartTime())).summaryStatistics();
		final DateTime start = new DateTime(statistics.getMin());
		final DateTime end = new DateTime(statistics.getMax());
		DateTime intermediate = start;
		final LinkedHashMap<String, ChartObject> map = new LinkedHashMap<>();
		if (base.isEmpty()) {
			return Collections.emptyMap();
		}
		while (intermediate.isBefore(end)) {
			map.put(intermediate.toString(DATE_PATTERN), createChartObject(base.get(0)));
			switch (period) {
				case DAY:
					intermediate = intermediate.plusDays(1);
					break;
				case WEEK:
					intermediate = intermediate.plusDays(1);
					break;
				case MONTH:
					intermediate = intermediate.plusMonths(1);
					break;
			}
		}
		map.put(end.toString(DATE_PATTERN), createChartObject(base.get(0)));
		return map;
	}

	/**
	 * Timeline periods enumerator
	 *
	 * @author Andrei_Ramanchuk
	 */
	public enum Period {
		// @formatter:off
		DAY(1), 
		WEEK(7), 
		MONTH(30);
		// @formatter:on

		private int value;

		Period(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public static boolean isPresent(String name) {
			return null != findByName(name);
		}

		public static Period findByName(String name) {
			return Arrays.stream(Period.values()).filter(time -> time.name().equalsIgnoreCase(name)).findAny().orElse(null);
		}

		public static Period getByName(String time) {
			return Period.valueOf(time);
		}
	}
}
