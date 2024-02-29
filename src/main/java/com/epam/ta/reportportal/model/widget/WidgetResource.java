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

import com.epam.ta.reportportal.model.filter.UserFilterResource;
import com.epam.ta.reportportal.ws.model.ValidationConstraints;
import com.epam.ta.reportportal.ws.reporting.OwnedResource;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author Dzmitry_Kavalets
 */
public class WidgetResource extends OwnedResource {

  @NotNull
  @JsonProperty(value = "id", required = true)
  private Long widgetId;

  @NotBlank
  @Size(min = ValidationConstraints.MIN_NAME_LENGTH, max = ValidationConstraints.MAX_WIDGET_NAME_LENGTH)
  @JsonProperty(value = "name", required = true)
  private String name;

  @NotNull
  @JsonProperty(value = "widgetType", required = true)
  @Schema(required = true, allowableValues =
      "oldLineChart, investigatedTrend, launchStatistics, statisticTrend,"
          + " casesTrend, notPassed, overallStatistics, uniqueBugTable, bugTrend, activityStream, launchesComparisonChart,"
          + " launchesDurationChart, launchesTable, topTestCases, flakyTestCases, passingRateSummary, passingRatePerLaunch,"
          + " productStatus, mostTimeConsuming, cumulative")
  private String widgetType;

  @NotNull
  @Valid
  @JsonProperty(value = "contentParameters", required = true)
  private ContentParameters contentParameters;

  @JsonProperty(value = "appliedFilters")
  private List<UserFilterResource> appliedFilters;

  @JsonProperty(value = "content")
  private Map<String, ?> content;

  public Long getWidgetId() {
    return widgetId;
  }

  public void setWidgetId(Long widgetId) {
    this.widgetId = widgetId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<UserFilterResource> getAppliedFilters() {
    return appliedFilters;
  }

  public void setAppliedFilters(List<UserFilterResource> appliedFilters) {
    this.appliedFilters = appliedFilters;
  }

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

  public Map<String, ?> getContent() {
    return content;
  }

  public void setContent(Map<String, ?> content) {
    this.content = content;
  }

  @Override
  public String toString() {
    return "WidgetResource{" + "widgetId=" + widgetId + ", name='" + name + '\'' + ", widgetType='"
        + widgetType + '\'' + ", contentParameters=" + contentParameters + ", appliedFilters="
        + appliedFilters + ", content=" + content + '}';
  }
}