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

import static com.epam.reportportal.base.infrastructure.model.ValidationConstraints.MAX_NUMBER_OF_FILTER_ENTITIES;
import static com.epam.reportportal.base.infrastructure.model.ValidationConstraints.MAX_USER_FILTER_NAME_LENGTH;
import static com.epam.reportportal.base.infrastructure.model.ValidationConstraints.MIN_COLLECTION_SIZE;
import static com.epam.reportportal.base.reporting.ValidationConstraints.MIN_NAME_LENGTH;

import com.epam.reportportal.base.infrastructure.annotations.In;
import com.epam.reportportal.base.model.BaseEntityRQ;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Domain object for filter actions
 *
 * @author Aliaksei_Makayed
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UpdateUserFilterRQ extends BaseEntityRQ {

  @NotBlank
  @Size(min = MIN_NAME_LENGTH, max = MAX_USER_FILTER_NAME_LENGTH)
  @JsonProperty(value = "name", required = true)
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private String name;

  @NotBlank
  @JsonProperty(value = "type", required = true)
  @In(allowedValues = {"launch", "testItem", "log"})
  @Schema(required = true, allowableValues = "launch, testitem, log")
  private String objectType;

  @Valid
  @NotNull
  @Size(min = MIN_COLLECTION_SIZE, max = MAX_NUMBER_OF_FILTER_ENTITIES)
  @JsonProperty(value = "conditions", required = true)
  @JsonDeserialize(as = LinkedHashSet.class)
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private Set<UserFilterCondition> conditions;

  @Valid
  @NotNull
  @Size(min = MIN_COLLECTION_SIZE)
  @JsonProperty(value = "orders", required = true)
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private List<Order> orders;

}
