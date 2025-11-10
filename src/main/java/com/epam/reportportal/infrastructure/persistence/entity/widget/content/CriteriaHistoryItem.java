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

import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.CRITERIA;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.LAUNCH_ID;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.START_TIME_HISTORY;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.STATUS_HISTORY;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.TOTAL;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.UNIQUE_ID;

import jakarta.persistence.Column;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Ivan Budaev
 */
@Getter
@Setter
public class CriteriaHistoryItem implements Serializable {

  @Column(name = UNIQUE_ID)
  private String uniqueId;

  @Column(name = "name")
  private String name;

  @Column(name = TOTAL)
  private Long total;

  @Column(name = CRITERIA)
  private Long criteria;

  @Column(name = STATUS_HISTORY)
  private Boolean[] status;

  @Column(name = START_TIME_HISTORY)
  private List<Instant> startTime;

  @Column(name = LAUNCH_ID)
  private Long launchId;

}
