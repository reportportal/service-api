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
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import lombok.Setter;

/**
 * Represents response from GET /user GitHub API.
 *
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
@Setter
class UserResource implements Serializable {

  @JsonProperty("login")
  private String login;

  @JsonProperty("email")
  private String email;

  @JsonProperty("name")
  private String name;

  @JsonProperty("avatar_url")
  private String avatarUrl;

  @JsonProperty("organizations_url")
  private String organizationsUrl;

  private Map<String, Object> details = new HashMap<>();

  public String getLogin() {
    return login;
  }


  public String getEmail() {
    return email;
  }

  public String getName() {
    return name;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public String getOrganizationsUrl() {
    return organizationsUrl;
  }

  @JsonAnyGetter
  public Map<String, Object> any() {
    return details;
  }

  @JsonAnySetter
  public void setUnknown(String name, Object value) {
    details.put(name, value);
  }

}
