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

import com.epam.ta.reportportal.model.project.config.IssueSubTypeResource;
import com.epam.ta.reportportal.model.project.config.pattern.PatternTemplateResource;
import com.epam.ta.reportportal.model.project.email.ProjectNotificationConfigDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Project configuration model
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@JsonInclude(Include.NON_NULL)
public class ProjectConfiguration {

  @JsonProperty(value = "attributes", required = true)
  private Map<String, String> projectAttributes;

  @JsonProperty(value = "subTypes")
  private Map<String, List<IssueSubTypeResource>> subTypes;

  @JsonProperty(value = "notificationsConfiguration")
  private ProjectNotificationConfigDTO projectConfig;

  @JsonProperty(value = "patterns")
  private List<PatternTemplateResource> patterns;

  public Map<String, String> getProjectAttributes() {
    return projectAttributes;
  }

  public void setProjectAttributes(Map<String, String> projectAttributes) {
    this.projectAttributes = projectAttributes;
  }

  public Map<String, List<IssueSubTypeResource>> getSubTypes() {
    return subTypes;
  }

  public void setSubTypes(Map<String, List<IssueSubTypeResource>> subTypes) {
    this.subTypes = subTypes;
  }

  public ProjectNotificationConfigDTO getProjectConfig() {
    return projectConfig;
  }

  public void setProjectConfig(ProjectNotificationConfigDTO projectConfig) {
    this.projectConfig = projectConfig;
  }

  public List<PatternTemplateResource> getPatterns() {
    return patterns;
  }

  public void setPatterns(List<PatternTemplateResource> patterns) {
    this.patterns = patterns;
  }
}
