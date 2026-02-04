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
import com.epam.reportportal.base.model.BaseEntityRQ;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Domain object for updating widget positions.
 *
 * @author Pavel Bortnik
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UpdateDashboardRQ extends BaseEntityRQ {

  @NotBlank
  @Size(min = ValidationConstraints.MIN_NAME_LENGTH, max = ValidationConstraints.MAX_DASHBOARD_NAME_LENGTH)
  @JsonProperty(value = "name", required = true)
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private String name;

  @Valid
  @JsonProperty(value = "updateWidgets")
  private List<DashboardResource.WidgetObjectModel> widgets;

  @Override
  public String toString() {
    return "UpdateDashboardRQ{" + "name='" + name + '\'' + ", widgets=" + widgets + '}';
  }
}
