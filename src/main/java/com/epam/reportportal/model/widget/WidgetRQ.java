/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.model.widget;

import com.epam.reportportal.core.events.annotations.WidgetLimitRange;
import com.epam.reportportal.infrastructure.annotations.In;
import com.epam.reportportal.infrastructure.model.ValidationConstraints;
import com.epam.reportportal.model.BaseEntityRQ;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Domain model object for creating and updating widget
 *
 * @author Aliaksei_Makayed
 */

@WidgetLimitRange
@ToString
public class WidgetRQ extends BaseEntityRQ {

  @Setter
  @Getter
  @NotBlank
  @Size(min = ValidationConstraints.MIN_NAME_LENGTH, max = ValidationConstraints.MAX_WIDGET_NAME_LENGTH)
  @JsonProperty(value = "name", required = true)
  private String name;

  @NotNull
  @JsonProperty(value = "widgetType", required = true)
  @In(allowedValues = {"oldLineChart", "investigatedTrend", "launchStatistics", "statisticTrend",
      "casesTrend", "notPassed", "overallStatistics", "uniqueBugTable", "bugTrend",
      "activityStream", "launchesComparisonChart", "launchesDurationChart", "launchesTable",
      "topTestCases", "flakyTestCases", "passingRateSummary", "passingRatePerLaunch",
      "productStatus", "mostTimeConsuming", "cumulative", "topPatternTemplates",
      "componentHealthCheck", "componentHealthCheckTable", "testCaseSearch"})
  @Schema(required = true, allowableValues =
      "oldLineChart, investigatedTrend, launchStatistics, statisticTrend,"
          + " casesTrend, notPassed, overallStatistics, uniqueBugTable, bugTrend, activityStream, launchesComparisonChart,"
          + " launchesDurationChart, launchesTable, topTestCases, flakyTestCases, passingRateSummary, passingRatePerLaunch,"
          + " productStatus, mostTimeConsuming, cumulative, topPatternTemplates, componentHealthCheck, componentHealthCheckTable,"
          + " testCaseSearch")
  private String widgetType;

  @Setter
  @Getter
  @Valid
  @JsonProperty(value = "contentParameters")
  private ContentParameters contentParameters;

  @Setter
  @Getter
  @JsonProperty(value = "filterIds")
  private List<Long> filterIds;

  @NotNull
  public String getWidgetType() {
    return widgetType;
  }

  public void setWidgetType(@NotNull String widgetType) {
    this.widgetType = widgetType;
  }

}
