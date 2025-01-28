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

import com.epam.ta.reportportal.model.project.config.ProjectConfigurationUpdate;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import jakarta.validation.Valid;

/**
 * Update project request model
 *
 * @author Andrei_Ramanchuk
 */
@JsonInclude(Include.NON_NULL)
public class UpdateProjectRQ {

  @JsonProperty(value = "users")
  private Map<String, String> userRoles;

  @Valid
  @JsonProperty(value = "configuration")
  private ProjectConfigurationUpdate configuration;

  /**
   * @return the userRoles
   */
  public Map<String, String> getUserRoles() {
    return userRoles;
  }

  /**
   * @param userRoles the userRoles to set
   */
  public void setUserRoles(Map<String, String> userRoles) {
    this.userRoles = userRoles;
  }

  public ProjectConfigurationUpdate getConfiguration() {
    return configuration;
  }

  public void setConfiguration(ProjectConfigurationUpdate configuration) {
    this.configuration = configuration;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("UpdateProjectRQ{");
    sb.append(", userRoles=").append(userRoles);
    sb.append(", configuration=").append(configuration);
    sb.append('}');
    return sb.toString();
  }
}
