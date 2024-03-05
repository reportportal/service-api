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
import java.util.Date;
import java.util.List;
import javax.validation.constraints.NotNull;

/**
 * Project resource representation for responses
 *
 * @author Pavel Bortnik
 */
public class ProjectResource {

  @NotNull
  @JsonProperty(value = "projectId", required = true)
  private Long projectId;

  @NotNull
  @JsonProperty(value = "projectName", required = true)
  private String projectName;

  @JsonProperty(value = "entryType", required = true)
  private String entryType;

  @NotNull
  @JsonProperty(value = "configuration", required = true)
  private ProjectConfiguration configuration;

  @JsonProperty(value = "users")
  private List<ProjectUser> users;

  @JsonProperty(value = "integrations")
  private List<IntegrationResource> integrations;

  @JsonProperty(value = "organization")
  private String organization;

  @JsonProperty(value = "allocatedStorage")
  private Long allocatedStorage;

  @NotNull
  @JsonProperty(value = "creationDate")
  private Date creationDate;

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public Long getProjectId() {
    return projectId;
  }

  public void setProjectId(Long projectId) {
    this.projectId = projectId;
  }

  public String getProjectName() {
    return projectName;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  public String getEntryType() {
    return entryType;
  }

  public void setEntryType(String entryType) {
    this.entryType = entryType;
  }

  public void setConfiguration(
      ProjectConfiguration configuration) {
    this.configuration = configuration;
  }

  public ProjectConfiguration getConfiguration() {
    return configuration;
  }

  public List<IntegrationResource> getIntegrations() {
    return integrations;
  }

  public void setIntegrations(List<IntegrationResource> integrations) {
    this.integrations = integrations;
  }

  public List<ProjectUser> getUsers() {
    return users;
  }

  public void setUsers(List<ProjectUser> users) {
    this.users = users;
  }

  public String getOrganization() {
    return organization;
  }

  public void setOrganization(String organization) {
    this.organization = organization;
  }

  public Long getAllocatedStorage() {
    return allocatedStorage;
  }

  public void setAllocatedStorage(Long allocatedStorage) {
    this.allocatedStorage = allocatedStorage;
  }

  public static class ProjectUser {

    @JsonProperty(value = "login")
    private String login;

    @JsonProperty(value = "projectRole")
    private String projectRole;

    public String getLogin() {
      return login;
    }

    public void setLogin(String login) {
      this.login = login;
    }

    public void setProjectRole(String value) {
      this.projectRole = value;
    }

    public String getProjectRole() {
      return projectRole;
    }

    @Override
    public String toString() {
      return "ProjectUser{" + "projectRole='" + projectRole + '\'' + '}';
    }
  }
}
