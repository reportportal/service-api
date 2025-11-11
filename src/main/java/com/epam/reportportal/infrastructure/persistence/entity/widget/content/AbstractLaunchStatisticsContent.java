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

import com.epam.reportportal.infrastructure.persistence.dao.converters.JpaInstantConverter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import java.io.Serializable;
import java.time.Instant;

/**
 * @author Ivan Budayeu
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class AbstractLaunchStatisticsContent implements Serializable {

  @Column(name = ID)
  @JsonProperty(value = ID)
  private Long id;

  @Column(name = "number")
  @JsonProperty(value = "number")
  private Integer number;

  @Column(name = "name")
  @JsonProperty(value = "name")
  private String name;

  @Column(name = "start_time")
  @JsonProperty(value = "startTime")
  @Convert(converter = JpaInstantConverter.class)
  private Instant startTime;

  public AbstractLaunchStatisticsContent() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Integer getNumber() {
    return number;
  }

  public void setNumber(Integer number) {
    this.number = number;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Instant getStartTime() {
    return startTime;
  }

  public void setStartTime(Instant startTime) {
    this.startTime = startTime;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("AbstractLaunchStatisticsContent{");
    sb.append("id=").append(id);
    sb.append(", number=").append(number);
    sb.append(", name='").append(name).append('\'');
    sb.append(", startTime=").append(startTime);
    sb.append('}');
    return sb.toString();
  }
}
