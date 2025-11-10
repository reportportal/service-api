/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.infrastructure.model.externalsystem;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * Allowed field value instance class
 *
 * @author Andrei_Ramanchuk
 */
@JsonInclude(Include.NON_NULL)
public class AllowedValue implements Serializable {

  @JsonProperty(value = "valueId")
  private String valueId;

  @JsonProperty(value = "valueName")
  private String valueName;

  public AllowedValue() {
  }

  public AllowedValue(String id, String name) {
    this.valueId = id;
    this.valueName = name;
  }

  public void setValueId(String id) {
    this.valueId = id;
  }

  public String getValueId() {
    return valueId;
  }

  public void setValueName(String value) {
    this.valueName = value;
  }

  public String getValueName() {
    return valueName;
  }
}
