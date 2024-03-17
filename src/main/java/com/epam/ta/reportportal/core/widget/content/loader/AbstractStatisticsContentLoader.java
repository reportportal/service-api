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

import static java.util.Optional.ofNullable;
import static net.sf.jasperreports.types.date.FixedDate.DATE_PATTERN;

import com.epam.ta.reportportal.entity.widget.content.ChartStatisticsContent;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections.MapUtils;

/**
 * @author Andrei_Ramanchuk
 */
public abstract class AbstractStatisticsContentLoader {

  /**
   * Return lists of objects grouped by specified period
   *
   * @param input   A list of {@link ChartStatisticsContent} objects to be grouped
   * @param period  The {@link Period} that defines the desired time grouping (e.g., DAY, WEEK, MONTH)
   * @return A map where keys represent the grouped time periods, and values are instances of
   *         {@link ChartStatisticsContent} containing aggregated statistical data for each group
   */
  protected Map<String, ChartStatisticsContent> groupByDate(List<ChartStatisticsContent> input,
      Period period) {

    final LongSummaryStatistics statistics = input.stream()
        .mapToLong(object -> object.getStartTime().toEpochMilli())
        .summaryStatistics();
    final Instant start = Instant.ofEpochMilli(statistics.getMin());
    final Instant end = Instant.ofEpochMilli(statistics.getMax());
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

  protected Map<String, ChartStatisticsContent> maxByDate(
      List<ChartStatisticsContent> statisticsContents, Period period,
      String contentField) {
    final Function<ChartStatisticsContent, String> chartObjectToDate = chartObject ->
        instantToFormattedString(chartObject.getStartTime(), DATE_PATTERN);

    final BinaryOperator<ChartStatisticsContent> chartObjectReducer = (o1, o2) ->
        Integer.parseInt(o1.getValues().get(contentField)) > Integer.parseInt(
            o2.getValues().get(contentField)) ?
            o1 :
            o2;
    final Map<String, Optional<ChartStatisticsContent>> groupByDate = statisticsContents.stream()
        .filter(
            content -> MapUtils.isNotEmpty(content.getValues()) && ofNullable(content.getValues()
                .get(contentField)).isPresent())
        .sorted(Comparator.comparing(ChartStatisticsContent::getStartTime))
        .collect(Collectors.groupingBy(chartObjectToDate, LinkedHashMap::new,
            Collectors.reducing(chartObjectReducer)));
    final Map<String, ChartStatisticsContent> range = groupByDate(statisticsContents, period);
    final LinkedHashMap<String, ChartStatisticsContent> result = new LinkedHashMap<>();
    // used forEach cause aspectj compiler can't infer types properly
    range.forEach((key, value) -> result.put(key,
        groupByDate.getOrDefault(key, Optional.of(createChartObject(statisticsContents.get(0))))
            .get()
    ));
    return result;
  }

  private void groupStatistics(String groupingPattern,
      List<ChartStatisticsContent> statisticsContents,
      Map<String, ChartStatisticsContent> chart) {

    Map<String, List<ChartStatisticsContent>> groupedStatistics = statisticsContents.stream()
        .collect(Collectors.groupingBy(c ->
                instantToFormattedString(c.getStartTime(), groupingPattern),
            LinkedHashMap::new,
            Collectors.toList()));

    groupedStatistics.forEach(
        (key, contents) -> chart.keySet().stream().filter(k -> k.startsWith(key)).findFirst()
            .ifPresent(k -> {
              ChartStatisticsContent content = chart.get(k);
              contents.add(content);
              Map<String, String> values = contents.stream()
                  .map(v -> v.getValues().entrySet())
                  .flatMap(Collection::stream)
                  .collect(Collectors.toMap(Map.Entry::getKey,
                      entry -> ofNullable(entry.getValue()).orElse("0"),
                      (prev, curr) -> prev = String.valueOf(
                          Double.parseDouble(prev) + Double.parseDouble(curr))
                  ));

              content.setValues(values);

              chart.put(k, content);
            }));
  }

  private void proceedDailyChart(Map<String, ChartStatisticsContent> chart, Instant intermediate,
      Instant end,
      List<ChartStatisticsContent> statisticsContents) {

    while (intermediate.isBefore(end)) {
      var interDate = instantToFormattedString(intermediate, DATE_PATTERN);
      chart.put(interDate, createChartObject(statisticsContents.getFirst()));
      intermediate = intermediate.plus(1, ChronoUnit.DAYS);
    }
    var endDate = instantToFormattedString(end, DATE_PATTERN);

    chart.put(endDate, createChartObject(statisticsContents.getFirst()));

  }

  private static String instantToFormattedString(Instant date, String pattern) {
    return date.atOffset(ZoneOffset.UTC).toLocalDateTime()
        .format(DateTimeFormatter.ofPattern(pattern));
  }

  private void proceedMonthlyChart(Map<String, ChartStatisticsContent> chart, Instant intermediate,
      Instant end,
      List<ChartStatisticsContent> statisticsContents) {
    while (intermediate.isBefore(end)) {
      if (intermediate.get(ChronoField.YEAR) == end.get(ChronoField.YEAR)) {
        if (intermediate.get(ChronoField.MONTH_OF_YEAR) != end.get(ChronoField.MONTH_OF_YEAR)) {
          chart.put(instantToFormattedString(intermediate, DATE_PATTERN),
              createChartObject(statisticsContents.getFirst()));
        }
      } else {
        chart.put(instantToFormattedString(intermediate, DATE_PATTERN),
            createChartObject(statisticsContents.getFirst()));
      }

      intermediate = intermediate.plus(1, ChronoUnit.MONTHS);
    }

    chart.put(instantToFormattedString(end, DATE_PATTERN),
        createChartObject(statisticsContents.getFirst()));

  }

  private ChartStatisticsContent createChartObject(ChartStatisticsContent input) {
    final ChartStatisticsContent chartObject = new ChartStatisticsContent();
    chartObject.setValues(input.getValues().entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> "0")));
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
      return Arrays.stream(Period.values()).filter(time -> time.name().equalsIgnoreCase(name))
          .findAny();
    }
  }
}
