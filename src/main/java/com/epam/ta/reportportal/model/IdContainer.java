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

package com.epam.ta.reportportal.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A simple class representing an object with an ID.
 */
@Data
@NoArgsConstructor
@Valid
public class IdContainer {

  @JsonProperty(required = true)
  @NotNull(message = "Id cannot be null")
  private Long id;

  /**
   * Constructs an {@code IdContainer} with the specified id.
   *
   * @param id the unique identifier, must not be {@code null}
   * @throws IllegalArgumentException if {@code id} is {@code null}
   */
  @JsonCreator
  public IdContainer(@JsonProperty("id") Long id) {
    if (id == null) {
      throw new IllegalArgumentException("Id must not be null");
    }
    this.id = id;
  }
}
