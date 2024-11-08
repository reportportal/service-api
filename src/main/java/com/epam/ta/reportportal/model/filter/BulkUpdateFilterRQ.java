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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * @deprecated use {@link UpdateUserFilterRQ} in conjunction with
 * {@link com.epam.ta.reportportal.model.BulkRQ}
 */
@Deprecated
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BulkUpdateFilterRQ extends UpdateUserFilterRQ {

  @NotBlank
  @JsonProperty(value = "id")
  private String id;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return "BulkUpdateFilterRQ{" + "id='" + id + '\'' + '}';
  }
}
