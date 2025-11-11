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
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.NUMBER;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.PASSED;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.SKIPPED;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.TOTAL;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import java.io.Serializable;

/**
 * @author Ivan Budayeu
 */
public class PassingRateStatisticsResult implements Serializable {

  @Column(name = ID)
  @JsonProperty(value = ID)
  private Long id;

  @Column(name = NUMBER)
  @JsonProperty(value = NUMBER)
  private int number;

  @Column(name = PASSED)
  @JsonProperty(value = PASSED)
  private int passed;

  @Column(name = TOTAL)
  @JsonProperty(value = TOTAL)
  private int total;

  @Column(name = SKIPPED)
  @JsonProperty(value = SKIPPED)
  private int skipped;

  public PassingRateStatisticsResult() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public int getNumber() {
    return number;
  }

  public void setNumber(int number) {
    this.number = number;
  }

  public int getPassed() {
    return passed;
  }

  public void setPassed(int passed) {
    this.passed = passed;
  }

  public int getTotal() {
    return total;
  }

  public void setTotal(int total) {
    this.total = total;
  }

  public int getSkipped() {
    return skipped;
  }

  public void setSkipped(int skipped) {
    this.skipped = skipped;
  }
}
