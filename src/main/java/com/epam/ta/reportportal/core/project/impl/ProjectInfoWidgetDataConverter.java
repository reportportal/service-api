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

package com.epam.ta.reportportal.core.project.impl;

import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.project.info.InfoInterval;
import com.epam.ta.reportportal.database.entity.project.info.ProjectInfoGroup;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.IsoFields;
import java.util.*;
import java.util.Map.Entry;

import static com.epam.ta.reportportal.database.entity.project.info.ProjectInfoGroup.BY_DAY;
import static com.epam.ta.reportportal.database.entity.project.info.ProjectInfoGroup.BY_NAME;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.WEEKS;

/**
 * Data converter for Report Portal common UI graphics
 *
 * @author Andrei_Ramanchuk
 */
@Service("projectInfoDataConverter")
public class ProjectInfoWidgetDataConverter {

	@Autowired
	@Qualifier("groupingStrategy")
	private Map<InfoInterval, ProjectInfoGroup> grouping;

	private static DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendValue(IsoFields.WEEK_BASED_YEAR, 4)
			.appendLiteral("-W")
			.appendValue(IsoFields.WEEK_OF_WEEK_BASED_YEAR, 2)
			.toFormatter();

	/**
	 * <b>Percentage Of Investigation</b> project info widget content
	 *
	 * @param initial
	 * @param interval
	 * @return
	 */
	public Map<String, List<ChartObject>> getInvestigatedProjectInfo(List<Launch> initial, InfoInterval interval) {
		if (initial.isEmpty()) {
			return new HashMap<>();
		}
		final DecimalFormat formatter = new DecimalFormat("###.##");
		final String INV = "investigated";
		final String TI = "toInvestigate";
		Map<String, List<ChartObject>> result = new HashMap<>();
		Map<String, List<Launch>> grouped = groupBy(initial, grouping.get(interval));
		Iterator<Entry<String, List<Launch>>> iterator = grouped.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, List<Launch>> pair = iterator.next();
			double investigated = 0;
			double toInvestigate = 0;
			List<Launch> group = pair.getValue();
			ChartObject currentGroup = new ChartObject();
			currentGroup.setName(pair.getKey());
			Map<String, String> values = new HashMap<>();
			for (Launch one : group) {
				investigated = investigated + one.getStatistics().getIssueCounter().getProductBugTotal() + one.getStatistics()
						.getIssueCounter()
						.getSystemIssueTotal() + one.getStatistics().getIssueCounter().getAutomationBugTotal();
				toInvestigate = toInvestigate + one.getStatistics().getIssueCounter().getToInvestigateTotal();
			}
			if ((investigated + toInvestigate) > 0) {
				double investigatedPercent = (investigated / (investigated + toInvestigate)) * 100;
				double toInvestigatePercent = 100 - investigatedPercent;
				values.put(INV, formatter.format(investigatedPercent));
				values.put(TI, formatter.format(toInvestigatePercent));
			} else {
				values.put(INV, "0");
				values.put(TI, "0");
			}
			currentGroup.setValues(values);
			result.put(pair.getKey(), Collections.singletonList(currentGroup));
			iterator.remove();
		}
		return result;
	}

	/**
	 * <b>Test-cases statistics in unique launches</b> project info widget
	 * content data-source
	 *
	 * @param initial
	 * @return
	 */
	public Map<String, List<ChartObject>> getTestCasesStatisticsProjectInfo(List<Launch> initial) {
		DecimalFormat formatter = new DecimalFormat("#####.##");
		final String MIN = "min";
		final String MAX = "max";
		final String AVG = "avg";
		String globalAverageSeria = "Median value in all unique launches";

		if (initial.isEmpty()) {
			return new HashMap<>();
		}

		Map<String, List<ChartObject>> result = new HashMap<>();
		Map<String, List<Launch>> grouped = groupBy(initial, BY_NAME);
		for (Entry<String, List<Launch>> pair : grouped.entrySet()) {
			ChartObject singleStat = new ChartObject();
			singleStat.setName(pair.getKey());
			Map<String, String> values = new HashMap<>();
			List<Launch> group = pair.getValue();

			DoubleSummaryStatistics statistics = group.stream()
					.mapToDouble(it -> it.getStatistics().getExecutionCounter().getTotal())
					.summaryStatistics();
			values.put(MIN, String.valueOf(statistics.getMin()));
			values.put(MAX, String.valueOf(statistics.getMax()));
			values.put(AVG, formatter.format(statistics.getAverage()));
			singleStat.setValues(values);

			result.put(pair.getKey(), Collections.singletonList(singleStat));
		}

		/*
		 * Separate label for 'Median value in all unique launches' on the table
		 */
		// TODO Implement new MEDIAN calculation!
		result.put(globalAverageSeria, Collections.singletonList(new ChartObject()));
		return result;
	}

	/**
	 * <b>Quantity of Launches</b> project info widget content
	 *
	 * @param initial
	 * @param interval
	 * @return
	 */
	public Map<String, List<ChartObject>> getLaunchesQuantity(List<Launch> initial, InfoInterval interval) {
		final String START_PERIOD = "start";
		final String END_PERIOD = "end";
		final String COUNT = "count";
		final String INTERVAL = "interval";
		HashMap<String, List<ChartObject>> result = new HashMap<>();
		if (initial.isEmpty()) {
			return result;
		}
		ProjectInfoGroup criteria = grouping.get(interval);
		Map<String, List<Launch>> grouped = groupBy(initial, criteria);
		for (Entry<String, List<Launch>> entry : grouped.entrySet()) {
			List<Launch> launches = entry.getValue();
			Integer count = null != launches ? launches.size() : 0;
			ChartObject group = new ChartObject();
			Map<String, String> values = new HashMap<>();
			values.put(COUNT, String.valueOf(count));
			values.put(INTERVAL, interval.getInterval());
			if (criteria != BY_DAY) {
				DateTime parse = DateTime.parse(entry.getKey());
				// TODO remove Yoda time. replace with JDK8
				values.put(START_PERIOD, parse.withDayOfWeek(DateTimeConstants.MONDAY).toString("yyy-MM-dd"));
				values.put(END_PERIOD, parse.withDayOfWeek(DateTimeConstants.SUNDAY).toString("yyy-MM-dd"));
			} else {
				values.put(START_PERIOD, entry.getKey());
			}
			group.setName("Number of launches");
			group.setValues(values);
			result.put(entry.getKey(), Collections.singletonList(group));
		}
		return result;
	}

	/**
	 * <b>Launch statistics line chart</b> project info widget content
	 *
	 * @param initial
	 * @param interval
	 * @return
	 */
	public Map<String, List<ChartObject>> getLaunchesIssues(List<Launch> initial, InfoInterval interval) {
		HashMap<String, List<ChartObject>> result = new HashMap<>();
		if (initial.isEmpty()) {
			return result;
		}
		final String PB = "productBug";
		final String SI = "systemIssue";
		final String AB = "automationBug";
		final String TI = "toInvestigate";

		ProjectInfoGroup criteria = grouping.get(interval);
		Map<String, List<Launch>> grouped = groupBy(initial, criteria);
		for (Entry<String, List<Launch>> entry : grouped.entrySet()) {
			List<Launch> launches = entry.getValue();
			Integer pbCount = 0;
			Integer abCount = 0;
			Integer siCount = 0;
			Integer tiCount = 0;
			for (Launch launch : launches) {
				pbCount += launch.getStatistics().getIssueCounter().getProductBugTotal();
				abCount += launch.getStatistics().getIssueCounter().getAutomationBugTotal();
				siCount += launch.getStatistics().getIssueCounter().getSystemIssueTotal();
				tiCount += launch.getStatistics().getIssueCounter().getToInvestigateTotal();
			}
			ChartObject object = new ChartObject();
			Map<String, String> values = new HashMap<>();
			values.put(PB, String.valueOf(pbCount));
			values.put(SI, String.valueOf(siCount));
			values.put(AB, String.valueOf(abCount));
			values.put(TI, String.valueOf(tiCount));
			object.setValues(values);
			result.put(entry.getKey(), Collections.singletonList(object));
		}
		return result;
	}

	/**
	 * Utility method for grouping input list of {@link Launch} by
	 * {@link ProjectInfoGroup} criteria
	 *
	 * @param initial
	 * @param criteria
	 * @return
	 */
	private static Map<String, List<Launch>> groupBy(List<Launch> initial, ProjectInfoGroup criteria) {
		Map<String, List<Launch>> result = new LinkedHashMap<>();
		LocalDate prevDate = null;
		for (Launch launch : initial) {
			final LocalDate localDate = launch.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			String key;
			switch (criteria) {
				case BY_NAME:
					key = launch.getName();
					break;
				default:
					key = formattedDate(criteria, localDate);
					if (prevDate != null) {
						while (!formattedDate(criteria, prevDate).equals(formattedDate(criteria, localDate))) {
							if (!result.containsKey(formattedDate(criteria, prevDate))) {
								result.put(formattedDate(criteria, prevDate), new ArrayList<>());
							}
							prevDate = prevDate.plus(1, criteria == BY_DAY ? DAYS : WEEKS);
						}
					}
			}
			if (!result.keySet().contains(key)) {
				result.put(key, Lists.newArrayList(launch));
			} else {
				List<Launch> prev = result.get(key);
				prev.add(launch);
				result.put(key, prev);
			}
			prevDate = localDate;
		}
		return result;
	}

	private static String formattedDate(ProjectInfoGroup criteria, LocalDate localDate) {
		return criteria == BY_DAY ? localDate.toString() : formatter.format(localDate);
	}

}