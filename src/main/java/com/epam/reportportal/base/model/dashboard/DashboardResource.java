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

package com.epam.reportportal.base.model.dashboard;

import com.epam.reportportal.base.infrastructure.model.ValidationConstraints;
import com.epam.reportportal.base.model.Position;
import com.epam.reportportal.base.reporting.OwnedResource;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Domain model DashBoard resource object. JSON Representation of Report Portal domain object.
 *
 * @author Aliaksei_Makayed
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DashboardResource extends OwnedResource {

  @NotNull
  @JsonProperty(value = "id", required = true)
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private Long dashboardId;

  @NotBlank
  @Size(min = ValidationConstraints.MIN_NAME_LENGTH, max = ValidationConstraints.MAX_DASHBOARD_NAME_LENGTH)
  @JsonProperty(value = "name", required = true)
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private String name;

  @JsonProperty(value = "widgets")
  private List<WidgetObjectModel> widgets;

  @Data
  public static class WidgetObjectModel {

    @JsonProperty(value = "widgetName")
    private String name;

    @NotNull
    @JsonProperty(value = "widgetId")
    private Long widgetId;

    @JsonProperty(value = "widgetType")
    private String widgetType;

    @JsonProperty(value = "widgetSize")
    private com.epam.reportportal.base.model.Size widgetSize = new com.epam.reportportal.base.model.Size();

    @JsonProperty(value = "widgetPosition")
    private Position widgetPosition = new Position();

    @JsonProperty(value = "widgetOptions")
    private Map<String, Object> widgetOptions;

    public WidgetObjectModel() {
    }

    public WidgetObjectModel(String name, Long widgetId, com.epam.reportportal.base.model.Size widgetSize,
        Position widgetPosition) {
      this.name = name;
      this.widgetId = widgetId;
      this.widgetSize = widgetSize;
      this.widgetPosition = widgetPosition;
    }

  }


}
