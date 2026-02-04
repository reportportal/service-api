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

package com.epam.reportportal.base.model.project;

import static com.epam.reportportal.base.infrastructure.model.ValidationConstraints.PROJECT_NAME_REGEXP;

import com.epam.reportportal.base.reporting.ValidationConstraints;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Data
public class DeleteProjectRQ {

  @NotBlank
  @JsonProperty(value = "projectName", required = true)
  @Pattern(regexp = PROJECT_NAME_REGEXP)
  @Size(min = ValidationConstraints.MIN_NAME_LENGTH, max = ValidationConstraints.MAX_NAME_LENGTH)
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private String projectName;

}
