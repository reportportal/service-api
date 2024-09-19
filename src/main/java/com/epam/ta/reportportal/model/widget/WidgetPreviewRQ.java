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

import com.epam.reportportal.annotations.In;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Model object for getting widget preview content
 *
 * @author Pavel Bortnik
 */

@JsonInclude(Include.NON_NULL)
public class WidgetPreviewRQ {

  @NotNull
  @JsonProperty(value = "widgetType", required = true)
  @In(allowedValues = { "oldLineChart", "investigatedTrend", "launchStatistics", "statisticTrend",
      "casesTrend", "notPassed", "overallStatistics", "uniqueBugTable", "bugTrend",
      "activityStream", "launchesComparisonChart", "launchesDurationChart", "launchesTable",
      "topTestCases", "flakyTestCases", "passingRateSummary", "passingRatePerLaunch",
      "productStatus", "mostTimeConsuming", "cumulative" })
  @Schema(required = true, allowableValues =
      "oldLineChart, investigatedTrend, launchStatistics, statisticTrend,"
          + " casesTrend, notPassed, overallStatistics, uniqueBugTable, bugTrend, activityStream, launchesComparisonChart,"
          + " launchesDurationChart, launchesTable, topTestCases, flakyTestCases, passingRateSummary, passingRatePerLaunch,"
          + " productStatus, mostTimeConsuming, cumulative")
  private String widgetType;

  @Valid
  @JsonProperty(value = "contentParameters")
  private ContentParameters contentParameters;

  @JsonProperty(value = "filterIds")
  private List<Long> filterIds;

  @NotNull
  public String getWidgetType() {
    return widgetType;
  }

  public void setWidgetType(@NotNull String widgetType) {
    this.widgetType = widgetType;
  }

  public ContentParameters getContentParameters() {
    return contentParameters;
  }

  public void setContentParameters(ContentParameters contentParameters) {
    this.contentParameters = contentParameters;
  }

  public List<Long> getFilterIds() {
    return filterIds;
  }

  public void setFilterIds(List<Long> filterIds) {
    this.filterIds = filterIds;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    WidgetPreviewRQ that = (WidgetPreviewRQ) o;

    if (!widgetType.equals(that.widgetType)) {
      return false;
    }
    if (contentParameters != null ? !contentParameters.equals(that.contentParameters) :
        that.contentParameters != null) {
      return false;
    }
    return filterIds != null ? filterIds.equals(that.filterIds) : that.filterIds == null;
  }

  @Override
  public int hashCode() {
    int result = widgetType.hashCode();
    result = 31 * result + (contentParameters != null ? contentParameters.hashCode() : 0);
    result = 31 * result + (filterIds != null ? filterIds.hashCode() : 0);
    return result;
  }
}
