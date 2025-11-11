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

package com.epam.reportportal.infrastructure.persistence.entity.widget.content;

import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.ID;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.NAME;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.NUMBER;

import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * @author Ivan Budaev
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LatestLaunchContent implements Serializable {

  @JsonProperty(ID)
  private Long id;

  @JsonProperty(NAME)
  private String name;

  @JsonProperty(NUMBER)
  private Long number;

  public LatestLaunchContent() {
  }

  public LatestLaunchContent(Launch launch) {
    this.id = launch.getId();
    this.name = launch.getName();
    this.number = launch.getNumber();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Long getNumber() {
    return number;
  }

  public void setNumber(Long number) {
    this.number = number;
  }
}
