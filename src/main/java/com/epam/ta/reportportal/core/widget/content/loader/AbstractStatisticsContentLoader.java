package com.epam.ta.reportportal.core.widget.content.loader;

import com.epam.ta.reportportal.entity.widget.content.ChartStatisticsContent;
import org.apache.commons.collections.MapUtils;
import org.joda.time.DateTime;

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

		final LongSummaryStatistics statistics = input.stream().mapToLong(object -> object.getStartTime().getTime()).summaryStatistics();
		final DateTime start = new DateTime(statistics.getMin());
		final DateTime end = new DateTime(statistics.getMax());
		if (input.isEmpty()) {
			return Collections.emptyMap();
		}
		final Map<String, ChartStatisticsContent> chart = new LinkedHashMap<>();

		switch (period) {
			case DAY:
				proceedDailyChart(chart, start, end, input);
				groupStatistics(DATE_PATTERN, input, chart);
				break;
			case WEEK:
				proceedDailyChart(chart, start, end, input);
				groupStatistics(DATE_PATTERN, input, chart);
				break;
			case MONTH:
				proceedMonthlyChart(chart, start, end, input);
				groupStatistics("yyyy-MM", input, chart);
				break;
		}

		return chart;
	}

	protected Map<String, ChartStatisticsContent> maxByDate(List<ChartStatisticsContent> statisticsContents, Period period,
			String contentField) {
		final Function<ChartStatisticsContent, String> chartObjectToDate = chartObject -> new DateTime(chartObject.getStartTime().getTime())
				.toString(DATE_PATTERN);
		final BinaryOperator<ChartStatisticsContent> chartObjectReducer = (o1, o2) -> Integer.valueOf(o1.getValues().get(contentField)) > Integer.valueOf(o2.getValues().get(contentField)) ?
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
							(prev, curr) -> prev = String.valueOf(Double.valueOf(prev) + Double.valueOf(curr))
					));

			content.setValues(values);

			chart.put(k, content);
		}));
	}

	private void proceedDailyChart(Map<String, ChartStatisticsContent> chart, DateTime intermediate, DateTime end,
			List<ChartStatisticsContent> statisticsContents) {

		while (intermediate.isBefore(end)) {
			chart.put(intermediate.toString(DATE_PATTERN), createChartObject(statisticsContents.get(0)));
			intermediate = intermediate.plusDays(1);
		}

		chart.put(end.toString(DATE_PATTERN), createChartObject(statisticsContents.get(0)));

	}

	private void proceedMonthlyChart(Map<String, ChartStatisticsContent> chart, DateTime intermediate, DateTime end,
			List<ChartStatisticsContent> statisticsContents) {
		while (intermediate.isBefore(end)) {
			if (intermediate.getYear() == end.getYear()) {
				if (intermediate.getMonthOfYear() != end.getMonthOfYear()) {
					chart.put(intermediate.toString(DATE_PATTERN), createChartObject(statisticsContents.get(0)));
				}
			} else {
				chart.put(intermediate.toString(DATE_PATTERN), createChartObject(statisticsContents.get(0)));
			}

			intermediate = intermediate.plusMonths(1);
		}

		chart.put(end.toString(DATE_PATTERN), createChartObject(statisticsContents.get(0)));

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
