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

package com.epam.reportportal.base.reporting;

import static com.epam.reportportal.base.reporting.ValidationConstraints.MAX_PARAMETERS_LENGTH;

import com.epam.reportportal.base.infrastructure.annotations.NotBlankWithSize;
import com.epam.reportportal.base.reporting.databind.MultiFormatDateDeserializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@JsonInclude(Include.NON_NULL)
@Getter
@Setter
@ToString
public class MergeLaunchesRQ {

  @NotBlankWithSize(min = ValidationConstraints.MIN_LAUNCH_NAME_LENGTH, max = ValidationConstraints.MAX_NAME_LENGTH)
  @JsonProperty(value = "name", required = true)
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private String name;

  @JsonProperty(value = "description")
  private String description;

  @Size(max = MAX_PARAMETERS_LENGTH)
  @Valid
  @JsonProperty("attributes")
  private Set<ItemAttributeResource> attributes;

  @JsonProperty(value = "startTime")
  @Schema
  @JsonDeserialize(using = MultiFormatDateDeserializer.class)
  private Instant startTime;

  @JsonProperty("mode")
  private Mode mode;

  @NotEmpty
  @JsonProperty(value = "launches", required = true)
  @Schema(description = "A set of IDs of the launches to be merged.", requiredMode = RequiredMode.REQUIRED)
  private Set<Long> launches;

  @JsonProperty(value = "endTime")
  @Schema
  @JsonDeserialize(using = MultiFormatDateDeserializer.class)
  private Instant endTime;

  @NotNull
  @JsonProperty("mergeType")
  @Schema(allowableValues = "BASIC, DEEP")
  private String mergeStrategyType;

  @JsonProperty(value = "extendSuitesDescription", required = true)
  private boolean extendSuitesDescription;

}
