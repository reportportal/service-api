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

package com.epam.reportportal.model.activity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Activity resource for log type events.
 *
 */
@Data
public class LogTypeActivityResource {

  @JsonProperty(value = "id", required = true)
  private Long id;

  @JsonProperty(value = "projectId", required = true)
  private Long projectId;

  @JsonProperty(value = "name", required = true)
  private String name;

  @JsonProperty(value = "level")
  private Integer level;

  @JsonProperty(value = "labelColor")
  private String labelColor;

  @JsonProperty(value = "backgroundColor")
  private String backgroundColor;

  @JsonProperty(value = "textColor")
  private String textColor;

  @JsonProperty(value = "textStyle")
  private String textStyle;

  @JsonProperty(value = "isFilterable")
  private Boolean isFilterable;
}
