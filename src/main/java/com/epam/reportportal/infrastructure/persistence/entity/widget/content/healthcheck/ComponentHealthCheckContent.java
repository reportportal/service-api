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

package com.epam.reportportal.infrastructure.persistence.entity.widget.content.healthcheck;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComponentHealthCheckContent implements Serializable {

  @JsonProperty(value = "attributeValue")
  private String attributeValue;

  @JsonProperty(value = "total")
  private Long total;

  @JsonProperty(value = "passingRate")
  private Double passingRate;

  public ComponentHealthCheckContent() {
  }

  public ComponentHealthCheckContent(String attributeValue, Long total, Double passingRate) {
    this.attributeValue = attributeValue;
    this.total = total;
    this.passingRate = passingRate;
  }

  public String getAttributeValue() {
    return attributeValue;
  }

  public void setAttributeValue(String attributeValue) {
    this.attributeValue = attributeValue;
  }

  public Long getTotal() {
    return total;
  }

  public void setTotal(Long total) {
    this.total = total;
  }

  public Double getPassingRate() {
    return passingRate;
  }

  public void setPassingRate(Double passingRate) {
    this.passingRate = passingRate;
  }
}
