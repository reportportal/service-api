/*
 * Copyright 2023 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.epam.ta.reportportal.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.time.Instant;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * JSON Representation of ReportPortal's Activity domain object.
 *
 * @author Ryhor_Kukharenka
 */
@JsonInclude(Include.NON_NULL)
@Getter
@Setter
@Builder
@ToString
public class ActivityEventResource {

  @NotNull
  @JsonProperty(value = "id", required = true)
  @Schema(requiredMode = RequiredMode.REQUIRED, description = "The unique ID of the activity",
      example = "1")
  private Long id;

  @NotNull
  @JsonProperty(value = "created_at")
  @Schema(description = "The time the activity was created", example = "2021-07-01T12:00:00Z")
  private Instant createdAt;

  @NotNull
  @JsonProperty(value = "event_name", required = true)
  @Schema(requiredMode = RequiredMode.REQUIRED, description = "The name of the event", example =
      "updateItem")
  private String eventName;

  @JsonProperty(value = "object_id")
  @Schema(requiredMode = RequiredMode.REQUIRED, description = "The ID of the object on which "
      + "activity was performed", example = "1")
  private Long objectId;

  @NotNull
  @JsonProperty(value = "object_name", required = true)
  @Schema(requiredMode = RequiredMode.REQUIRED, description = "The name of the object on which "
      + "activity was performed", example = "Test item name")
  private String objectName;

  @NotNull
  @JsonProperty(value = "object_type", required = true)
  @Schema(requiredMode = RequiredMode.REQUIRED, description = "The type of the object on which "
      + "activity was performed", example = "itemIssue")
  private String objectType;

  @JsonProperty(value = "project_id")
  @Schema(requiredMode = RequiredMode.REQUIRED, description = "The ID of the project", example =
      "1")
  private Long projectId;

  @JsonProperty(value = "project_name")
  @Schema(requiredMode = RequiredMode.REQUIRED, description = "The name of the project", example
      = "Project name")
  private String projectName;

  @NotNull
  @JsonProperty(value = "subject_name", required = true)
  @Schema(requiredMode = RequiredMode.REQUIRED, description = "The name of the subject who "
      + "performed the activity", example = "Username")
  private String subjectName;

  @NotNull
  @JsonProperty(value = "subject_type", required = true)
  @Schema(requiredMode = RequiredMode.REQUIRED, description = "The type of the subject who "
      + "performed the activity", example = "user")
  private String subjectType;

  @NotNull
  @JsonProperty(value = "subject_id", required = true)
  @Schema(requiredMode = RequiredMode.REQUIRED, description = "The ID of the subject who "
      + "performed the activity", example = "1")
  private String subjectId;

  @JsonProperty(value = "details")
  @Schema(description = "The details of the activity, for example history of value", example = """
      {
          "history": [
              {
                  "field": "status",
                  "newValue": "FAILED",
                  "oldValue": "PASSED"
              }
          ]
      }
      """)
  private Object details;

}
