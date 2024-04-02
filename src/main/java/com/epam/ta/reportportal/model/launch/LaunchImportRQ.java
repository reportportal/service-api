/*
 * Copyright 2023 EPAM Systems
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

package com.epam.ta.reportportal.model.launch;

import static com.epam.reportportal.model.ValidationConstraints.MAX_NAME_LENGTH;
import static com.epam.reportportal.model.ValidationConstraints.MAX_PARAMETERS_LENGTH;
import static com.epam.reportportal.model.ValidationConstraints.MIN_LAUNCH_NAME_LENGTH;

import com.epam.ta.reportportal.ws.reporting.ItemAttributesRQ;
import com.epam.ta.reportportal.ws.reporting.Mode;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Data
public class LaunchImportRQ {

  @JsonProperty(value = "name")
  @Schema
  @Size(min = MIN_LAUNCH_NAME_LENGTH, max = MAX_NAME_LENGTH)
  protected String name;

  @JsonProperty(value = "description")
  private String description;

  @Size(max = MAX_PARAMETERS_LENGTH)
  @Valid
  @JsonProperty("attributes")
  @JsonAlias({ "attributes", "tags" })
  private Set<ItemAttributesRQ> attributes;

  @JsonProperty
  @JsonAlias({ "startTime", "start_time" })
  @Schema
  private Date startTime;

  @JsonProperty("mode")
  private Mode mode;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Set<ItemAttributesRQ> getAttributes() {
    return attributes;
  }

  public void setAttributes(
      Set<ItemAttributesRQ> attributes) {
    this.attributes = attributes;
  }

  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public Mode getMode() {
    return mode;
  }

  public void setMode(Mode mode) {
    this.mode = mode;
  }
}
