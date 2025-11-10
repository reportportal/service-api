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

package com.epam.reportportal.model.log;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class GetLogsUnderRq {

  @NotNull
  @JsonProperty(value = "itemIds")
  private List<Long> itemIds;

  @NotNull
  @JsonProperty(value = "logLevel")
  private String logLevel;

  public GetLogsUnderRq() {
  }

  public List<Long> getItemIds() {
    return itemIds;
  }

  public void setItemIds(List<Long> itemIds) {
    this.itemIds = itemIds;
  }

  public String getLogLevel() {
    return logLevel;
  }

  public void setLogLevel(String logLevel) {
    this.logLevel = logLevel;
  }
}
