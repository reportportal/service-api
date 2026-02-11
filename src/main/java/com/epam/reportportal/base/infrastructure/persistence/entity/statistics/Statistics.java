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

package com.epam.reportportal.base.infrastructure.persistence.entity.statistics;

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
import java.util.Objects;

/**
 * @author Ivan Budayeu
 */
@Entity
@Table(name = "statistics")
public class Statistics implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "s_id")
  private Long id;

  @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
  @JoinColumn(name = "statistics_field_id")
  private StatisticsField statisticsField;

  @Column(name = "s_counter")
  private int counter;

  @Column(name = "launch_id")
  private Long launchId;

  @Column(name = "item_id")
  private Long itemId;

  public Statistics() {
  }

  public Statistics(StatisticsField statisticsField, int counter) {
    this.statisticsField = statisticsField;
    this.counter = counter;
  }

  public Statistics(StatisticsField statisticsField, int counter, Long launchId) {
    this.statisticsField = statisticsField;
    this.counter = counter;
    this.launchId = launchId;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public StatisticsField getStatisticsField() {
    return statisticsField;
  }

  public void setStatisticsField(StatisticsField statisticsField) {
    this.statisticsField = statisticsField;
  }

  public int getCounter() {
    return counter;
  }

  public void setCounter(int counter) {
    this.counter = counter;
  }

  public Long getLaunchId() {
    return launchId;
  }

  public void setLaunchId(Long launchId) {
    this.launchId = launchId;
  }

  public Long getItemId() {
    return itemId;
  }

  public void setItemId(Long itemId) {
    this.itemId = itemId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Statistics that = (Statistics) o;
    return counter == that.counter && Objects.equals(id, that.id) && Objects.equals(statisticsField,
        that.statisticsField)
        && Objects.equals(launchId, that.launchId) && Objects.equals(itemId, that.itemId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, statisticsField, counter, launchId, itemId);
  }
}
