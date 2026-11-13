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

package com.epam.reportportal.base.infrastructure.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * JSON Representation of Report Portal's Activity domain object.
 *
 * @see <a href="http://en.wikipedia.org/wiki/HATEOAS">HATEOAS Description</a>
 */
@JsonInclude(Include.NON_NULL)
@Getter
@Setter
@ToString
@NoArgsConstructor
public class ActivityResource {

  @NotNull
  @JsonProperty(value = "id", required = true)
  @Schema(requiredMode = RequiredMode.REQUIRED, description = "The unique ID of the activity", example = "1")
  private Long id;

  @NotNull
  @JsonProperty(value = "user", required = true)
  @Schema(requiredMode = RequiredMode.REQUIRED, description = "The user who performed the activity", example = "user")
  private String user;

  @NotNull
  @JsonProperty(value = "userId", required = true)
  @Schema(requiredMode = RequiredMode.REQUIRED, description = "The ID of the user who performed the activity", example = "1")
  private Long userId;

  @NotNull
  @JsonProperty(value = "loggedObjectId", required = true)
  @Schema(requiredMode = RequiredMode.REQUIRED, description = "The ID of the object on which activity was performed", example = "1")
  private Long loggedObjectId;

  @NotNull
  @JsonProperty(value = "lastModified", required = true)
  @Schema(requiredMode = RequiredMode.REQUIRED, description = "The time the activity was last modified", example = "2021-07-01T12:00:00Z")
  private Instant lastModified;

  @NotNull
  @JsonProperty(value = "actionType", required = true)
  @Schema(requiredMode = RequiredMode.REQUIRED, description = "The type of action performed", example = "startLaunch")
  private String actionType;

  @NotNull
  @JsonProperty(value = "objectType", required = true)
  @Schema(requiredMode = RequiredMode.REQUIRED, description = "The type of object on which the activity was performed", example = "LAUNCH")
  private String objectType;

  @NotNull
  @JsonProperty(value = "projectId", required = true)
  @Schema(requiredMode = RequiredMode.REQUIRED, description = "The ID of the project in which the activity was performed", example = "1")
  private Long projectId;

  @JsonProperty(value = "projectName")
  @Schema(description = "The name of the project in which the activity was performed", example = "project")
  private String projectName;

  @JsonProperty(value = "projectKey")
  private String projectKey;

  @JsonProperty(value = "details")
  @Schema(description = "The details of the activity, for example history of value", example = "{\"history\": [{\"field\": \"status\", \"newValue\": \"FAILED\", \"oldValue\": \"PASSED\"}]}")
  private Object details;

  @JsonProperty(value = "objectName")
  @Schema(description = "The name of the object on which the activity was performed", example = "Launch name")
  private String objectName;

}
