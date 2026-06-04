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

package com.epam.reportportal.base.infrastructure.persistence.entity.widget.content;

import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.DURATION;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.END_TIME;

import com.epam.reportportal.base.infrastructure.persistence.dao.converters.JpaInstantConverter;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import java.time.Instant;

/**
 * Launch duration widget: min/max/avg and related time fields.
 *
 * @author Ivan Budayeu
 */
public class LaunchesDurationContent extends AbstractLaunchStatisticsContent {

  @JsonProperty(value = "status")
  @Column(name = "status")
  private String status;

  @Column(name = "end_time")
  @JsonProperty(value = END_TIME)
  @Convert(converter = JpaInstantConverter.class)
  private Instant endTime;

  @Column(name = "duration")
  @JsonProperty(value = DURATION)
  private long duration;

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Instant getEndTime() {
    return endTime;
  }

  public void setEndTime(Instant endTime) {
    this.endTime = endTime;
  }

  public long getDuration() {
    return duration;
  }

  public void setDuration(long duration) {
    this.duration = duration;
  }
}
