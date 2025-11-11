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

package com.epam.reportportal.infrastructure.persistence.entity.widget;

import jakarta.annotation.Nullable;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author Pavel Bortnik
 */
public enum WidgetType {

  OLD_LINE_CHART("oldLineChart", false, false),
  INVESTIGATED_TREND("investigatedTrend", false, false),
  LAUNCH_STATISTICS("launchStatistics", false, true),
  STATISTIC_TREND("statisticTrend", false, true),
  CASES_TREND("casesTrend", false, false),
  NOT_PASSED("notPassed", false, false),
  OVERALL_STATISTICS("overallStatistics", false, true),
  UNIQUE_BUG_TABLE("uniqueBugTable", false, false),
  BUG_TREND("bugTrend", false, false),
  ACTIVITY("activityStream", false, false),
  LAUNCHES_COMPARISON_CHART("launchesComparisonChart", false, false),
  LAUNCHES_DURATION_CHART("launchesDurationChart", false, false),
  LAUNCHES_TABLE("launchesTable", false, true),
  TOP_TEST_CASES("topTestCases", false, false),
  FLAKY_TEST_CASES("flakyTestCases", false, false),
  PASSING_RATE_SUMMARY("passingRateSummary", false, false),
  PASSING_RATE_PER_LAUNCH("passingRatePerLaunch", false, false),
  PRODUCT_STATUS("productStatus", false, true),
  MOST_TIME_CONSUMING("mostTimeConsuming", false, false),

  CUMULATIVE("cumulative", true, false),
  TOP_PATTERN_TEMPLATES("topPatternTemplates", true, false),
  COMPONENT_HEALTH_CHECK("componentHealthCheck", true, false),
  COMPONENT_HEALTH_CHECK_TABLE("componentHealthCheckTable", true, false),
  TEST_CASE_SEARCH("testCaseSearch", false, false);

  private final String type;

  private final boolean supportMultilevelStructure;

  private final boolean issueTypeUpdateSupported;

  WidgetType(String type, boolean supportMultilevelStructure, boolean issueTypeUpdateSupported) {
    this.type = type;
    this.supportMultilevelStructure = supportMultilevelStructure;
    this.issueTypeUpdateSupported = issueTypeUpdateSupported;
  }

  public static WidgetType getByName(String type) {
    return WidgetType.valueOf(type);
  }

  public static Optional<WidgetType> findByName(@Nullable String name) {
    return Arrays.stream(WidgetType.values())
        .filter(gadgetTypes -> gadgetTypes.getType().equalsIgnoreCase(name)).findAny();
  }

  public String getType() {
    return this.type;
  }

  public boolean isSupportMultilevelStructure() {
    return supportMultilevelStructure;
  }

  public boolean isIssueTypeUpdateSupported() {
    return issueTypeUpdateSupported;
  }

}
