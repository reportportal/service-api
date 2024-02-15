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
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * User resource representation for responses
 *
 * @author Andrei_Ramanchuk
 */
@JsonInclude(Include.NON_NULL)
@Getter
@Setter
@ToString
public class UserResource {

  @NotNull
  @JsonProperty(value = "id", required = true)
  private Long id;

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
  private Map<String, AssignedProject> assignedProjects = new HashMap<>();

  @JsonProperty(value = "assignedOrganization")
  private Map<String, AssignedOrganization> assignedOrganizations = new HashMap<>();

  @Getter
  @Setter
  @ToString
  public static class AssignedProject {

    private String projectRole;
    private String entryType;
    private String projectKey;
    private String projectSlug;
    private Long organizationId;

  }

  @Getter
  @Setter
  @ToString
  public static class AssignedOrganization {

    private Long organizationId;
    private String organizationRole;
    private String organizationSlug;
    private String organizationName;

  }

}
