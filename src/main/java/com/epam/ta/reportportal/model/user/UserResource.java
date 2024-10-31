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

package com.epam.ta.reportportal.model.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * User resource representation for responses
 *
 * @author Andrei_Ramanchuk
 */
@Data
@JsonInclude(Include.NON_NULL)
public class UserResource {

  @NotNull
  @JsonProperty(value = "id", required = true)
  private Long id;

  @JsonProperty
  private UUID uuid;

  @JsonProperty
  private String externalId;

  @JsonProperty
  private boolean active;

  @NotNull
  @JsonProperty(value = "userId", required = true)
  private String userId;

  @JsonProperty(value = "email", required = true)
  private String email;

  @JsonProperty(value = "photoId")
  private String photoId;

  @JsonProperty(value = "fullName")
  private String fullName;

  @JsonProperty(value = "accountType")
  private String accountType;

  @JsonProperty(value = "userRole")
  private String userRole;

  @JsonProperty(value = "photoLoaded")
  private boolean isLoaded;

  @JsonProperty(value = "metadata")
  private Object metadata;

  @JsonProperty(value = "assignedProjects")
  private Map<String, AssignedProject> assignedProjects;

  @Data
  public static class AssignedProject {

    private String projectRole;
    private String entryType;

  }
}
