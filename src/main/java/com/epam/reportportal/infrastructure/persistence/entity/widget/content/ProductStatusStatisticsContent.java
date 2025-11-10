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

import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.ATTRIBUTE_VALUE;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.ATTRIBUTE_VALUES;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.AVERAGE_PASSING_RATE;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.DURATION;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.FILTER_NAME;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.PASSING_RATE;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.SUM;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductStatusStatisticsContent extends AbstractLaunchStatisticsContent {

  @Column(name = "status")
  @JsonProperty(value = "status")
  private String status;

  @Column(name = ATTRIBUTE_VALUES)
  @JsonProperty(value = "attributes")
  private Map<String, Set<String>> attributes;

  @JsonProperty(value = "values")
  private Map<String, String> values = new LinkedHashMap<>();

  @Column(name = SUM)
  @JsonProperty(value = SUM)
  private Map<String, Integer> totalStatistics = new LinkedHashMap<>();

  @Column(name = DURATION)
  @JsonProperty(value = DURATION)
  private Long duration;

  @Column(name = PASSING_RATE)
  @JsonProperty(value = PASSING_RATE)
  private Double passingRate;

  @JsonProperty(value = AVERAGE_PASSING_RATE)
  private Double averagePassingRate;

  @JsonIgnore
  @Column(name = ATTRIBUTE_VALUE)
  private String tagValue;

  @JsonIgnore
  @Column(name = FILTER_NAME)
  private String filterName;

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Map<String, Set<String>> getAttributes() {
    return attributes;
  }

  public void setAttributes(Map<String, Set<String>> attributes) {
    this.attributes = attributes;
  }

  public Map<String, String> getValues() {
    return values;
  }

  public void setValues(Map<String, String> values) {
    this.values = values;
  }

  public Map<String, Integer> getTotalStatistics() {
    return totalStatistics;
  }

  public void setTotalStatistics(Map<String, Integer> totalStatistics) {
    this.totalStatistics = totalStatistics;
  }

  public Long getDuration() {
    return duration;
  }

  public void setDuration(Long duration) {
    this.duration = duration;
  }

  public Double getPassingRate() {
    return passingRate;
  }

  public void setPassingRate(Double passingRate) {
    this.passingRate = passingRate;
  }

  public Double getAveragePassingRate() {
    return averagePassingRate;
  }

  public void setAveragePassingRate(Double averagePassingRate) {
    this.averagePassingRate = averagePassingRate;
  }

  public String getTagValue() {
    return tagValue;
  }

  public void setTagValue(String tagValue) {
    this.tagValue = tagValue;
  }

  public String getFilterName() {
    return filterName;
  }

  public void setFilterName(String filterName) {
    this.filterName = filterName;
  }
}
