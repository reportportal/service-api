/*
 * Copyright 2023 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.epam.ta.reportportal.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import jakarta.validation.constraints.NotNull;

/**
 * Keep all search criteria for request.
 *
 * @author Ryhor_Kukharenka
 */
@JsonInclude(Include.NON_NULL)
public class SearchCriteriaRQ {

  @NotNull
  @JsonProperty(value = "search_criterias")
  private Set<SearchCriteria> criteriaList;

  public SearchCriteriaRQ() {
  }

  public SearchCriteriaRQ(Set<SearchCriteria> criteriaList) {
    this.criteriaList = criteriaList;
  }

  public Set<SearchCriteria> getCriteriaList() {
    return criteriaList;
  }

  public void setCriteriaList(Set<SearchCriteria> criteriaList) {
    this.criteriaList = criteriaList;
  }

}
