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

package com.epam.ta.reportportal.model.project;

import com.epam.ta.reportportal.model.integration.IntegrationResource;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Project resource representation for responses
 *
 * @author Pavel Bortnik
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class ProjectResource {

  @NotNull
  @JsonProperty(value = "projectId", required = true)
  private Long projectId;

  @NotNull
  @JsonProperty(value = "projectName", required = true)
  private String projectName;

  @JsonProperty(value = "projectKey")
  private String projectKey;

  @JsonProperty(value = "projectSlug")
  private String projectSlug;

  @NotNull
  @JsonProperty(value = "configuration", required = true)
  private ProjectConfiguration configuration;

  @JsonProperty(value = "users")
  private List<ProjectUser> users;

  @JsonProperty(value = "integrations")
  private List<IntegrationResource> integrations;

  @JsonProperty(value = "organization")
  private String organization;

  @JsonProperty(value = "organizationId")
  private Long organizationId;

  @JsonProperty(value = "allocatedStorage")
  private Long allocatedStorage;

  @NotNull
  @JsonProperty(value = "creationDate")
  private Instant creationDate;

  @Getter
  @Setter
  @ToString
  public static class ProjectUser {

    @JsonProperty(value = "login")
    private String login;

    @JsonProperty(value = "projectRole")
    private String projectRole;

  }
}
