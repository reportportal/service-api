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

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * Launch duration widget: min/max/avg and related time fields.
 *
 * @author Ivan Budayeu
 */
@Setter
@Getter
public class LaunchesDurationContent extends AbstractLaunchStatisticsContent {

  @JsonProperty(value = "status")
  @Column(name = "status")
  private String status;

  @Column(name = "end_time")
  @JsonProperty(value = END_TIME)
  private Instant endTime;

  @Column(name = "duration")
  @JsonProperty(value = DURATION)
  private long duration;

}
