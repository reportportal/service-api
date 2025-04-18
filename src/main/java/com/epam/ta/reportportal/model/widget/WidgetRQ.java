/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.model.widget;

import com.epam.reportportal.model.ValidationConstraints;
import com.epam.ta.reportportal.core.events.annotations.WidgetLimitRange;
import com.epam.ta.reportportal.model.BaseEntityRQ;
import com.epam.ta.reportportal.ws.annotations.In;
import com.epam.ta.reportportal.ws.annotations.NotBlankWithSize;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Domain model object for creating and updating widget
 *
 * @author Aliaksei_Makayed
 */

@WidgetLimitRange
@JsonInclude(Include.NON_NULL)
public class WidgetRQ extends BaseEntityRQ {

  @Setter
  @Getter
  @NotBlankWithSize(min = ValidationConstraints.MIN_NAME_LENGTH, max = ValidationConstraints.MAX_WIDGET_NAME_LENGTH)
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

  @Override
  public String toString() {
    return "WidgetRQ{" + "name='" + name + '\'' + ", widgetType='" + widgetType + '\''
        + ", contentParameters=" + contentParameters + ", filterIds=" + filterIds + '}';
  }
}
