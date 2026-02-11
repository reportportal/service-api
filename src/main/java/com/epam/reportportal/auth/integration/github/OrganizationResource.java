/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.auth.integration.github;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.HashMap;
import java.util.Map;

/**
 * GitHub API: Organization resource representation.
 *
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
public class OrganizationResource {

  private String login;
  private Long id;
  private String url;
  private String description;

  private Map<String, Object> details = new HashMap<>();

  @JsonAnyGetter
  public Map<String, Object> any() {
    return details;
  }

  @JsonAnySetter
  public void setUnknown(String name, Object value) {
    details.put(name, value);
  }

  public String getLogin() {
    return login;
  }

  public Long getId() {
    return id;
  }

  public String getUrl() {
    return url;
  }

  public String getDescription() {
    return description;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setDetails(Map<String, Object> details) {
    this.details = details;
  }
}
