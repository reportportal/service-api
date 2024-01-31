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

package com.epam.ta.reportportal.model.activity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class UserActivityResource {

  @JsonProperty(value = "id", required = true)
  private Long id;

  @JsonProperty(value = "defaultProjectId", required = true)
  private Long defaultProjectId;

  @JsonProperty(value = "fullName", required = true)
  private String fullName;

  public UserActivityResource() {
  }

  public UserActivityResource(Long id, Long defaultProjectId, String fullName) {
    this.id = id;
    this.defaultProjectId = defaultProjectId;
    this.fullName = fullName;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getDefaultProjectId() {
    return defaultProjectId;
  }

  public void setDefaultProjectId(Long defaultProjectId) {
    this.defaultProjectId = defaultProjectId;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("UserActivityResource{");
    sb.append("id=").append(id);
    sb.append(", defaultProjectId=").append(defaultProjectId);
    sb.append(", fullName='").append(fullName).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
