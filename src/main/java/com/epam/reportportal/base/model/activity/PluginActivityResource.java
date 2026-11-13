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

package com.epam.reportportal.base.model.activity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PluginActivityResource {

  @JsonProperty(value = "id", required = true)
  private Long id;
  @JsonProperty(value = "name")
  private String name;

  @JsonProperty(value = "enabled")
  private boolean enabled;

  @JsonProperty(value = "version")
  private String version;

}
