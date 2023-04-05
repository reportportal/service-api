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

import com.epam.ta.reportportal.entity.widget.content.AbstractLaunchStatisticsContent;
import com.epam.ta.reportportal.entity.widget.content.ChartStatisticsContent;
import org.apache.commons.collections.MapUtils;
import org.joda.time.DateTime;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static net.sf.jasperreports.types.date.FixedDate.DATE_PATTERN;

/**
 * @author Andrei_Ramanchuk
 */
public abstract class AbstractStatisticsContentLoader {

	/**
	 * Return lists of objects grouped by specified period
	 *
	 * @param input
	 * @param period
	 * @return
	 */
	protected Map<String, ChartStatisticsContent> groupByDate(List<ChartStatisticsContent> input, Period period) {
		if (input.isEmpty()) {
			return Collections.emptyMap();
		}
		final Map<String, ChartStatisticsContent> chart = new LinkedHashMap<>();

		switch (period) {
			case DAY:
			case WEEK:
				proceedDailyChart(chart, input);
				groupStatistics(DATE_PATTERN, input, chart);
				break;
			case MONTH:
				proceedDailyChart(chart, input);
				groupStatistics("yyyy-MM", input, chart);
				break;
		}

		return chart;
	}

	protected Map<String, ChartStatisticsContent> maxByDate(List<ChartStatisticsContent> statisticsContents, Period period,
			String contentField) {
		final Function<ChartStatisticsContent, String> chartObjectToDate = chartObject -> new DateTime(chartObject.getStartTime().getTime())
				.toString(DATE_PATTERN);
		final BinaryOperator<ChartStatisticsContent> chartObjectReducer = (o1, o2) -> Integer.parseInt(o1.getValues().get(contentField)) > Integer.parseInt(o2.getValues().get(contentField)) ?
				o1 :
				o2;
		final Map<String, Optional<ChartStatisticsContent>> groupByDate = statisticsContents.stream()
				.filter(content -> MapUtils.isNotEmpty(content.getValues()) && ofNullable(content.getValues()
						.get(contentField)).isPresent())
				.sorted(Comparator.comparing(ChartStatisticsContent::getStartTime))
				.collect(Collectors.groupingBy(chartObjectToDate, LinkedHashMap::new, Collectors.reducing(chartObjectReducer)));
		final Map<String, ChartStatisticsContent> range = groupByDate(statisticsContents, period);
		final LinkedHashMap<String, ChartStatisticsContent> result = new LinkedHashMap<>();
		// used forEach cause aspectj compiler can't infer types properly
		range.forEach((key, value) -> result.put(key,
				groupByDate.getOrDefault(key, Optional.of(createChartObject(statisticsContents.get(0)))).get()
		));
		return result;
	}

	private void groupStatistics(String groupingPattern, List<ChartStatisticsContent> statisticsContents,
			Map<String, ChartStatisticsContent> chart) {

		Map<String, List<ChartStatisticsContent>> groupedStatistics = statisticsContents.stream()
				.collect(Collectors.groupingBy(c -> new DateTime(c.getStartTime()).toString(groupingPattern),
						LinkedHashMap::new,
						Collectors.toList()
				));

		groupedStatistics.forEach((key, contents) -> chart.keySet().stream().filter(k -> k.startsWith(key)).findFirst().ifPresent(k -> {
			ChartStatisticsContent content = chart.get(k);
			contents.add(content);
			Map<String, String> values = contents.stream()
					.map(v -> v.getValues().entrySet())
					.flatMap(Collection::stream)
					.collect(Collectors.toMap(Map.Entry::getKey,
							entry -> ofNullable(entry.getValue()).orElse("0"),
							(prev, curr) -> prev = String.valueOf(Double.parseDouble(prev) + Double.parseDouble(curr))
					));

			content.setValues(values);

			chart.put(k, content);
		}));
	}

	private void proceedDailyChart(Map<String, ChartStatisticsContent> chart, List<ChartStatisticsContent> statisticsContents) {
		statisticsContents.stream().sorted(Comparator.comparing(AbstractLaunchStatisticsContent::getStartTime)).forEach(sc -> {
			chart.put(
					sc.getStartTime().toLocalDateTime().format(DateTimeFormatter.ISO_DATE),
					createChartObject(sc)
			);
		});
	}

	private ChartStatisticsContent createChartObject(ChartStatisticsContent input) {
		final ChartStatisticsContent chartObject = new ChartStatisticsContent();
		chartObject.setValues(input.getValues().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> "0")));
		return chartObject;
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
			return findByName(name).isPresent();
		}

		public static Optional<Period> findByName(String name) {
			return Arrays.stream(Period.values()).filter(time -> time.name().equalsIgnoreCase(name)).findAny();
		}
	}
}
