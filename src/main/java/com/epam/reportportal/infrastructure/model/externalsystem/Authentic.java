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

package com.epam.reportportal.infrastructure.model.externalsystem;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Basic class for accessible data //TODO: AV - refactor to support polymorphism
 *
 * @author Andrei_Ramanchuk
 */
@JsonInclude(Include.NON_NULL)
public class Authentic {

  @JsonProperty(value = "username")
  private String username;

  @JsonProperty(value = "password")
  private String password;

  @JsonProperty(value = "domain")
  private String domain;

  @JsonProperty(value = "token")
  private String token;

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getUsername() {
    return username;
  }

  public void setPassword(String pass) {
    this.password = pass;
  }

  public String getPassword() {
    return password;
  }

  public void setToken(String value) {
    this.token = value;
  }

  public String getToken() {
    return token;
  }

  @Override
  public String toString() {
    return "Authentic{" + "username='" + username + '\''
        + ", password='" + password + '\'' //NOSONAR
        + ", token='" + token + '\''
        + '}';
  }
}
