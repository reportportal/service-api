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

package com.epam.reportportal.base.model.widget;

import com.epam.reportportal.base.infrastructure.model.ValidationConstraints;
import com.epam.reportportal.base.model.filter.UserFilterResource;
import com.epam.reportportal.base.reporting.OwnedResource;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Widget definition returned to the UI (type, name, content, position).
 *
 * @author Dzmitry_Kavalets
 */
@Data
@EqualsAndHashCode(callSuper = true)
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

}
