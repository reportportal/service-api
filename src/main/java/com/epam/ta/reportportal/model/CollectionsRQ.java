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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * @deprecated use {@link BulkRQ} instead
 */
@Deprecated
public class CollectionsRQ<T> {

  @Valid
  @NotNull
  @JsonProperty(value = "elements", required = true)
  private List<T> elements;

  public List<T> getElements() {
    return elements;
  }

  public void setElements(List<T> elements) {
    this.elements = elements;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("CollectionsRQ{");
    sb.append("elements=").append(elements);
    sb.append('}');
    return sb.toString();
  }
}
