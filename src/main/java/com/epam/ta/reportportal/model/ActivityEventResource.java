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
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * JSON Representation of Report Portal's Activity domain object.
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
  @ApiModelProperty(required = true)
  private Long id;

  @NotNull
  @JsonProperty(value = "created_at")
  private LocalDateTime createdAt;

  @NotNull
  @JsonProperty(value = "event_name", required = true)
  @ApiModelProperty(required = true)
  private String eventName;

  @JsonProperty(value = "object_id")
  @ApiModelProperty(required = true)
  private Long objectId;

  @NotNull
  @JsonProperty(value = "object_name", required = true)
  @ApiModelProperty(required = true)
  private String objectName;

  @NotNull
  @JsonProperty(value = "object_type", required = true)
  @ApiModelProperty(required = true)
  private String objectType;

  @JsonProperty(value = "project_id")
  @ApiModelProperty(required = true)
  private Long projectId;

  @JsonProperty(value = "project_name")
  @ApiModelProperty(required = true)
  private String projectName;

  @NotNull
  @JsonProperty(value = "subject_name", required = true)
  @ApiModelProperty(required = true)
  private String subjectName;

  @NotNull
  @JsonProperty(value = "subject_type", required = true)
  @ApiModelProperty(required = true)
  private String subjectType;

  @NotNull
  @JsonProperty(value = "subject_id", required = true)
  @ApiModelProperty(required = true)
  private String subjectId;

  @JsonProperty(value = "details")
  private Object details;

}
