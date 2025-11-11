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

package com.epam.reportportal.infrastructure.model.externalsystem;

import com.epam.reportportal.infrastructure.model.ValidationConstraints;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import lombok.Setter;

/**
 * Domain object for creating ticket in bug tracking system.
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
@Setter
@JsonInclude(Include.NON_NULL)
public class PostTicketRQ extends Authentic {

  @JsonProperty(value = "fields", required = true)
  private List<PostFormField> fields;

  @JsonProperty(value = "includeLogs")
  private boolean isIncludeLogs;

  @JsonProperty(value = "includeData")
  private boolean isIncludeScreenshots;

  @JsonProperty(value = "includeComments")
  private boolean isIncludeComments;

  @Min(value = ValidationConstraints.TICKET_MIN_LOG_SIZE)
  @Max(value = ValidationConstraints.TICKET_MAX_LOG_SIZE)
  @JsonProperty(value = "logQuantity")
  private int numberOfLogs;

  @JsonProperty(value = "item", required = true)
  private Long testItemId;

  @Size(max = 300)
  @JsonProperty(value = "backLinks", required = true)
  private Map<Long, String> backLinks;

  public Map<Long, String> getBackLinks() {
    return backLinks;
  }

  public List<PostFormField> getFields() {
    return fields;
  }

  public boolean getIsIncludeLogs() {
    return isIncludeLogs;
  }

  public boolean getIsIncludeScreenshots() {
    return isIncludeScreenshots;
  }

  public boolean getIsIncludeComments() {
    return isIncludeComments;
  }

  public int getNumberOfLogs() {
    return numberOfLogs;
  }

  public Long getTestItemId() {
    return testItemId;
  }

  @Override
  public String toString() {
    return "PostTicketRQ [fields=" + fields + ", isIncludeLogs=" + isIncludeLogs
        + ", isIncludeScreenshots=" + isIncludeScreenshots
        + ", isIncludeComments=" + isIncludeComments + ", numberOfLogs=" + numberOfLogs
        + ", testItemId=" + testItemId
        + ", backLinks=" + backLinks + "]";
  }
}
