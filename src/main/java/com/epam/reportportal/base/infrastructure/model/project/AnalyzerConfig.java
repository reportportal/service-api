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

package com.epam.reportportal.base.infrastructure.model.project;

/*import static com.epam.reportportal.base.reporting.ValidationConstraints.MAX_SHOULD_MATCH;
import static com.epam.reportportal.base.reporting.ValidationConstraints.MIN_NUMBER_OF_LOG_LINES;
import static com.epam.reportportal.base.reporting.ValidationConstraints.MIN_SHOULD_MATCH;*/


import static com.epam.reportportal.base.infrastructure.model.ValidationConstraints.MAX_SHOULD_MATCH;
import static com.epam.reportportal.base.infrastructure.model.ValidationConstraints.MIN_NUMBER_OF_LOG_LINES;
import static com.epam.reportportal.base.infrastructure.model.ValidationConstraints.MIN_SHOULD_MATCH;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;


/**
 * @author Pavel Bortnik
 */
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema
public class AnalyzerConfig {

  @Min(value = MIN_SHOULD_MATCH)
  @Max(value = MAX_SHOULD_MATCH)
  @JsonProperty(value = "minShouldMatch")
  private Integer minShouldMatch;

  @Min(value = MIN_SHOULD_MATCH)
  @Max(value = MAX_SHOULD_MATCH)
  @JsonProperty(value = "searchLogsMinShouldMatch")
  private Integer searchLogsMinShouldMatch;

  @Min(value = MIN_NUMBER_OF_LOG_LINES)
  @JsonProperty(value = "numberOfLogLines")
  private Integer numberOfLogLines;

  @JsonProperty(value = "isAutoAnalyzerEnabled")
  private Boolean isAutoAnalyzerEnabled;

  @JsonProperty(value = "analyzerMode")
  @Schema(allowableValues = "ALL, LAUNCH_NAME, CURRENT_LAUNCH, PREVIOUS_LAUNCH, CURRENT_AND_THE_SAME_NAME")
  private String analyzerMode;

  @JsonProperty(value = "indexingRunning")
  @Schema(hidden = true)
  private boolean indexingRunning;

  @JsonProperty(value = "allMessagesShouldMatch")
  private boolean allMessagesShouldMatch;

  @JsonProperty(value = "largestRetryPriority")
  private boolean largestRetryPriority;

}
