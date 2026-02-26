/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.model.filter;


import static com.epam.reportportal.base.infrastructure.model.ValidationConstraints.MAX_USER_FILTER_NAME_LENGTH;
import static com.epam.reportportal.base.infrastructure.model.ValidationConstraints.MIN_COLLECTION_SIZE;
import static com.epam.reportportal.base.reporting.ValidationConstraints.MIN_NAME_LENGTH;

import com.epam.reportportal.base.infrastructure.annotations.In;
import com.epam.reportportal.base.reporting.OwnedResource;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * JSON Representation of ReportPortal's UserFilter domain object
 *
 * @author Aliaksei_Makayed
 */

@Data
@EqualsAndHashCode(callSuper = true)
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

  @In(allowedValues = {"launch", "testItem", "log"})
  @NotNull
  @JsonProperty(value = "type", required = true)
  private String objectType;

  @NotNull
  @JsonProperty(value = "owner", required = true)
  private String owner;

}
