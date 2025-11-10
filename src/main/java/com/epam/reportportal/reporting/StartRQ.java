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

import static com.epam.reportportal.reporting.ValidationConstraints.MAX_PARAMETERS_LENGTH;

import com.epam.reportportal.reporting.databind.MultiFormatDateDeserializer;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Base entity for start requests
 *
 * @author Andrei Varabyeu
 */
@JsonInclude(Include.NON_NULL)
@Data
public class StartRQ {

  @JsonProperty(value = "name", required = true)
  @Schema(requiredMode = RequiredMode.REQUIRED)
  protected String name;

  @JsonProperty(value = "description")
  private String description;

  @Size(max = MAX_PARAMETERS_LENGTH)
  @Valid
  @JsonProperty("attributes")
  @JsonAlias({"attributes", "tags"})
  private Set<ItemAttributesRQ> attributes;

  @NotNull
  @JsonProperty(required = true)
  @JsonAlias({"startTime", "start_time"})
  @Schema(requiredMode = RequiredMode.REQUIRED)
  @JsonDeserialize(using = MultiFormatDateDeserializer.class)
  private Instant startTime;

  @Schema(requiredMode = RequiredMode.REQUIRED)
  @JsonProperty(value = "uuid")
  @EqualsAndHashCode.Exclude
  private String uuid;

}
