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

import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.FLAKY_COUNT;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.ITEM_NAME;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.LAUNCH_ID;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.START_TIME_HISTORY;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.STATUSES;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.TOTAL;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.UNIQUE_ID;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import lombok.Data;

/**
 * A flaky test case row: statuses over time, counts, and name.
 *
 * @author Ivan Budayeu
 */
@Data
public class FlakyCasesTableContent implements Serializable {

  @JsonProperty(value = STATUSES)
  @Column(name = STATUSES)
  private String[] statuses;

  @JsonProperty(value = FLAKY_COUNT)
  @Column(name = FLAKY_COUNT)
  private Long flakyCount;

  @JsonProperty(value = TOTAL)
  @Column(name = TOTAL)
  private Long total;

  @JsonProperty(value = "itemName")
  @Column(name = ITEM_NAME)
  private String itemName;

  @JsonProperty(value = "uniqueId")
  @Column(name = UNIQUE_ID)
  private String uniqueId;

  @JsonProperty(value = "startTime")
  @Column(name = START_TIME_HISTORY)
  private List<Instant> startTime;

  @Column(name = LAUNCH_ID)
  private Long launchId;

  public FlakyCasesTableContent() {
  }
}
