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

package com.epam.ta.reportportal.model.filter;

import static com.epam.ta.reportportal.ws.model.ValidationConstraints.MAX_USER_FILTER_NAME_LENGTH;
import static com.epam.ta.reportportal.ws.model.ValidationConstraints.MIN_COLLECTION_SIZE;
import static com.epam.ta.reportportal.ws.model.ValidationConstraints.MIN_NAME_LENGTH;

import com.epam.ta.reportportal.ws.annotations.In;
import com.epam.ta.reportportal.ws.reporting.OwnedResource;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * JSON Representation of Report Portal's UserFilter domain object
 *
 * @author Aliaksei_Makayed
 */

@JsonInclude(Include.NON_NULL)
public class UserFilterResource extends OwnedResource {

  @NotNull
  @JsonProperty(value = "id", required = true)
  private Long filterId;

  @NotBlank
  @Size(min = MIN_NAME_LENGTH, max = MAX_USER_FILTER_NAME_LENGTH)
  @JsonProperty(value = "name", required = true)
  private String name;

  @Valid
  @NotNull
  @Size(min = MIN_COLLECTION_SIZE)
  @JsonProperty(value = "conditions", required = true)
  private Set<UserFilterCondition> conditions;

  @Size(min = MIN_COLLECTION_SIZE)
  @JsonProperty(value = "orders", required = true)
  private List<Order> orders;

  @In(allowedValues = { "launch", "testItem", "log" })
  @NotNull
  @JsonProperty(value = "type", required = true)
  private String objectType;

  @NotNull
  @JsonProperty(value = "owner", required = true)
  private String owner;

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Set<UserFilterCondition> getConditions() {
    return conditions;
  }

  public void setConditions(Set<UserFilterCondition> conditions) {
    this.conditions = conditions;
  }

  public List<Order> getOrders() {
    return orders;
  }

  public void setOrders(List<Order> orders) {
    this.orders = orders;
  }

  public Long getFilterId() {
    return filterId;
  }

  public void setFilterId(Long filterId) {
    this.filterId = filterId;
  }

  public String getObjectType() {
    return objectType;
  }

  public void setObjectType(String objectType) {
    this.objectType = objectType;
  }

  @Override
  public String toString() {
    return "UserFilterResource{" + "filterId='" + filterId + '\'' + ", name='" + name + '\''
        + ", conditions=" + conditions + ", orders=" + orders + ", objectType='" + objectType + '\''
        + ", owner='" + owner + '\'' + "} " + super.toString();
  }
}
