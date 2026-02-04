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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

/**
 * Domain object for updating launch object.
 *
 * @author Aliaksei_Makayed
 */
@Setter
@Getter
@JsonInclude(Include.NON_NULL)
public class UpdateLaunchRQ {

  @JsonProperty("mode")
  @Schema(allowableValues = "DEFAULT, DEBUG")
  private Mode mode;

  @JsonProperty("description")
  private String description;

  @Size(max = MAX_PARAMETERS_LENGTH)
  @Valid
  @JsonProperty("attributes")
  private Set<ItemAttributeResource> attributes;

  @Override
  public String toString() {
    return "UpdateLaunchRQ{" + "mode=" + mode
        + '}';
  }
}
