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

package com.epam.reportportal.base.model.project.config.pattern;


import static com.epam.reportportal.base.infrastructure.model.ValidationConstraints.MAX_ANALYSIS_PATTERN_NAME_LENGTH;
import static com.epam.reportportal.base.infrastructure.model.ValidationConstraints.MIN_ANALYSIS_PATTERN_NAME_LENGTH;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Data
@NoArgsConstructor
public class CreatePatternTemplateRQ {

  @NotBlank
  @Size(min = MIN_ANALYSIS_PATTERN_NAME_LENGTH, max = MAX_ANALYSIS_PATTERN_NAME_LENGTH)
  @JsonProperty(value = "name")
  private String name;

  @NotBlank
  @JsonProperty(value = "value")
  private String value;

  @NotBlank
  @JsonProperty(value = "type")
  private String type;

  @NotNull
  @JsonProperty(value = "enabled")
  private Boolean enabled;

}
