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

import com.epam.ta.reportportal.model.WarningAwareRS;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Aliaksandr_Kazantsau
 */
@JsonInclude(Include.NON_NULL)
public class CreateUserRS extends WarningAwareRS {

  @JsonProperty("id")
  private Long id;

  @JsonProperty("login")
  private String login;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("CreateUserRS{");
    sb.append("id=").append(id);
    sb.append(", login='").append(login).append('\'');
    sb.append('}');
    return sb.toString();
  }
}