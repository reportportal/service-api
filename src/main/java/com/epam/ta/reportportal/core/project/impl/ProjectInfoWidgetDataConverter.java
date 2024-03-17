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

package com.epam.ta.reportportal.core.project.impl;

import static com.epam.ta.reportportal.core.project.impl.ProjectInfoWidgetDataConverter.ProjectInfoGroup.BY_DAY;
import static com.epam.ta.reportportal.core.project.impl.ProjectInfoWidgetDataConverter.ProjectInfoGroup.BY_NAME;
import static com.epam.ta.reportportal.core.statistics.StatisticsHelper.extractStatisticsCount;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.DEFECTS_AUTOMATION_BUG_TOTAL;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.DEFECTS_PRODUCT_BUG_TOTAL;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.DEFECTS_SYSTEM_ISSUE_TOTAL;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.DEFECTS_TO_INVESTIGATE_TOTAL;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.EXECUTIONS_TOTAL;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.WEEKS;
import static java.time.temporal.IsoFields.WEEK_BASED_YEAR;

import com.epam.ta.reportportal.entity.enums.InfoInterval;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.statistics.Statistics;
import com.epam.ta.reportportal.model.widget.ChartObject;
import com.google.common.collect.Lists;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Data converter for Report Portal common UI graphics
 *
 * @author Andrei_Ramanchuk
 */
@Service("projectInfoDataConverter")
public class ProjectInfoWidgetDataConverter {

  private Map<InfoInterval, ProjectInfoGroup> grouping;

  @Autowired
  public ProjectInfoWidgetDataConverter(
      @Qualifier("groupingStrategy") Map<InfoInterval, ProjectInfoGroup> grouping) {
    this.grouping = grouping;
  }

  public enum ProjectInfoGroup {
    BY_DAY, BY_WEEK, BY_NAME
  }

  private static DateTimeFormatter formatter = new DateTimeFormatterBuilder()
          .appendValue(WEEK_BASED_YEAR, 4)
          .appendLiteral("-W")
          .appendValue(IsoFields.WEEK_OF_WEEK_BASED_YEAR, 2)
          .toFormatter();

  /**
   * <b>Percentage Of Investigation</b> project info widget content
   *
   * @param initial  A list of {@link Launch} objects to calculate investigation percentages
   * @param interval An {@link InfoInterval} representing the grouping interval (e.g., daily, weekly)
   * @return A {@link Map} with keys representing interval group names and values containing
   * {@link List} of {@link ChartObject} instances with investigated and to-investigate percentage information
   */
  public Map<String, List<ChartObject>> getInvestigatedProjectInfo(List<Launch> initial,
      InfoInterval interval) {
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
      Entry<String, List<Launch>> pair = iterator.next();
      double investigated = 0;
      double toInvestigate = 0;
      List<Launch> group = pair.getValue();
      ChartObject currentGroup = new ChartObject();
      currentGroup.setName(pair.getKey());
      Map<String, String> values = new HashMap<>();
      for (Launch one : group) {
        investigated =
            investigated + extractStatisticsCount(DEFECTS_PRODUCT_BUG_TOTAL, one.getStatistics())
                + extractStatisticsCount(DEFECTS_SYSTEM_ISSUE_TOTAL, one.getStatistics())
                + extractStatisticsCount(DEFECTS_AUTOMATION_BUG_TOTAL, one.getStatistics());
        toInvestigate = toInvestigate + extractStatisticsCount(DEFECTS_TO_INVESTIGATE_TOTAL,
            one.getStatistics()
        );
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
   * @param initial A list of {@link Launch} objects to calculate test-case statistics
   * @return A {@link Map} with keys representing launch names and values containing
   * {@link List} of {@link ChartObject} instances with min, max, and average statistics
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

      DoubleSummaryStatistics statistics = group.stream().mapToDouble(
          launch -> launch.getStatistics().stream()
              .filter(it -> it.getStatisticsField().getName().equalsIgnoreCase(EXECUTIONS_TOTAL))
              .findFirst().orElse(new Statistics()).getCounter()).summaryStatistics();

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
   * @param initial  A list of {@link Launch} objects to calculate the quantity of launches
   * @param interval An {@link InfoInterval} representing the grouping interval (e.g., daily, weekly)
   * @return A {@link Map} with keys representing interval group names and values containing
   * {@link List} of {@link ChartObject} instances with launch quantity information
   */
  public Map<String, List<ChartObject>> getLaunchesQuantity(List<Launch> initial,
      InfoInterval interval) {
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
        var numberOfWeek = formatter.parse(entry.getKey())
            .getLong(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        var startPeriod = parseByDayOfWeek(entry, numberOfWeek, DayOfWeek.MONDAY);

        values.put(START_PERIOD, startPeriod.format(DateTimeFormatter.ISO_DATE));
        var endPeriod = parseByDayOfWeek(entry, numberOfWeek, DayOfWeek.SUNDAY);

        values.put(END_PERIOD, endPeriod.format(DateTimeFormatter.ISO_DATE));
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
   * @param initial  A list of {@link Launch} objects to calculate the issues
   * @param interval An {@link InfoInterval} representing the grouping interval (e.g., daily, weekly)
   * @return A {@link Map} with keys representing interval group names and values containing
   * {@link List} of {@link ChartObject} instances with issue count information
   */
  public Map<String, List<ChartObject>> getLaunchesIssues(List<Launch> initial,
      InfoInterval interval) {
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
        pbCount += extractStatisticsCount(DEFECTS_PRODUCT_BUG_TOTAL, launch.getStatistics());
        abCount += extractStatisticsCount(DEFECTS_AUTOMATION_BUG_TOTAL, launch.getStatistics());
        siCount += extractStatisticsCount(DEFECTS_SYSTEM_ISSUE_TOTAL, launch.getStatistics());
        tiCount += extractStatisticsCount(DEFECTS_TO_INVESTIGATE_TOTAL, launch.getStatistics());
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
   * Utility method for grouping input list of {@link Launch} by {@link ProjectInfoGroup} criteria
   *
   * @param initial  A list of {@link Launch} objects to be grouped
   * @param criteria The {@link ProjectInfoGroup} criteria that determines the grouping key
   * @return A map where the keys represent the grouping parameters (e.g., launch names or formatted dates)
   * and the values are lists of {@link Launch} objects grouped under that key
   */
  private static Map<String, List<Launch>> groupBy(List<Launch> initial,
      ProjectInfoGroup criteria) {
    Map<String, List<Launch>> result = new LinkedHashMap<>();
    LocalDate prevDate = null;
    for (Launch launch : initial) {
      final LocalDate localDate = launch.getStartTime().atZone(ZoneOffset.UTC).toLocalDate();

      String key;
      switch (criteria) {
        case BY_NAME:
          key = launch.getName();
          break;
        default:
          key = formattedDate(criteria, localDate);
          if (prevDate != null) {
            while (prevDate.isBefore(localDate)) {
              if (!result.containsKey(formattedDate(criteria, prevDate))) {
                result.put(formattedDate(criteria, prevDate), new ArrayList<>());
              }
              prevDate = prevDate.plus(1, criteria == BY_DAY ? DAYS : WEEKS);
            }
          }
      }
      if (!result.containsKey(key)) {
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

  private static LocalDate parseByDayOfWeek(Entry<String, List<Launch>> entry, long numberOfWeek,
      DayOfWeek dayOfWeek) {
    int year = (int) formatter.parse(entry.getKey()).getLong(WEEK_BASED_YEAR);
    return LocalDate.of(year, 1, 1)
        .with(TemporalAdjusters.firstInMonth(dayOfWeek))
        .with(WeekFields.ISO.weekOfWeekBasedYear(), numberOfWeek);
  }

}
