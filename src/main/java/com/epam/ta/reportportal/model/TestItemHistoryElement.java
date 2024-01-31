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

package com.epam.ta.reportportal.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(Include.NON_NULL)
public class TestItemHistoryElement {

  @JsonProperty(value = "groupingField")
  private String groupingField;

  @JsonProperty(value = "resources")
  private List<TestItemResource> resources;

  public TestItemHistoryElement() {
  }

  public String getGroupingField() {
    return groupingField;
  }

  public void setGroupingField(String groupingField) {
    this.groupingField = groupingField;
  }

  public List<TestItemResource> getResources() {
    return resources;
  }

  public void setResources(List<TestItemResource> resources) {
    this.resources = resources;
  }

  @Override
  public String toString() {
    return "TestItemHistoryElement{" + "groupingField=" + groupingField + ", resources=" + resources
        + '}';
  }
}