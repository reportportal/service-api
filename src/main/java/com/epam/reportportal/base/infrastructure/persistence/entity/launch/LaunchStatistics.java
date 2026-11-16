/*
 * Copyright 2026 EPAM Systems
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

package com.epam.reportportal.base.infrastructure.persistence.entity.launch;

import com.epam.reportportal.base.infrastructure.persistence.entity.statistics.StatisticsField;
import com.epam.reportportal.base.infrastructure.persistence.entity.statistics.StatisticsView;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.Data;

/**
 * A counter row (pass/fail/etc.) attached to a launch.
 */
@Data
@Entity
@Table(name = "launch_statistics")
public class LaunchStatistics implements StatisticsView, Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "s_id")
  private Long id;

  @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
  @JoinColumn(name = "statistics_field_id")
  private StatisticsField statisticsField;

  @Column(name = "s_counter")
  private int counter;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "launch_id")
  private Launch launch;

  public LaunchStatistics() {
  }

  public LaunchStatistics(StatisticsField statisticsField, int counter) {
    this.statisticsField = statisticsField;
    this.counter = counter;
  }
}
