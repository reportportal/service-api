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

package com.epam.reportportal.reporting;

import com.epam.reportportal.reporting.databind.MultiFormatDateDeserializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * JSON Representation of Report Portal's Launch domain object
 *
 * @author Andrei Varabyeu
 */
@JsonInclude(Include.NON_NULL)
@Getter
@Setter
@ToString
public class LaunchResource extends OwnedResource {

  @NotNull
  @JsonProperty(value = "id", required = true)
  private Long launchId;

  @NotBlank
  @JsonProperty(value = "uuid", required = true)
  private String uuid;

  @NotBlank
  @Size(min = ValidationConstraints.MIN_NAME_LENGTH, max = ValidationConstraints.MAX_NAME_LENGTH)
  @JsonProperty(value = "name", required = true)
  private String name;

  @NotNull
  @JsonProperty(value = "number", required = true)
  private Long number;

  @JsonProperty(value = "description")
  @Size(max = ValidationConstraints.MAX_LAUNCH_DESCRIPTION_LENGTH)
  private String description;

  @NotNull
  @JsonProperty(value = "startTime", required = true)
  @JsonDeserialize(using = MultiFormatDateDeserializer.class)
  private Instant startTime;

  @JsonProperty(value = "endTime")
  @JsonDeserialize(using = MultiFormatDateDeserializer.class)
  private Instant endTime;

  @JsonProperty(value = "lastModified")
  @JsonDeserialize(using = MultiFormatDateDeserializer.class)
  private Instant lastModified;

  @NotNull
  @JsonProperty(value = "status", required = true)
  private String status;

  @JsonProperty(value = "statistics")
  @Valid
  private StatisticsResource statisticsResource;

  @JsonProperty(value = "attributes")
  private Set<ItemAttributeResource> attributes;

  @JsonProperty(value = "mode")
  private Mode mode;

  @JsonProperty(value = "analysing")
  private Set<String> analyzers = new LinkedHashSet<>();

  @JsonProperty(value = "approximateDuration")
  private double approximateDuration;

  @JsonProperty(value = "hasRetries")
  private boolean hasRetries;

  @JsonProperty(value = "rerun")
  private boolean rerun;

  @JsonProperty(value = "metadata")
  private Map<String, Object> metadata;

  @JsonProperty(value = "retentionPolicy")
  private RetentionPolicy retentionPolicy;

}
