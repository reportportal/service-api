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

package com.epam.reportportal.base.model.launch;

import com.epam.reportportal.base.reporting.EntryCreatedAsyncRS;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Response model of launch start resource
 *
 * @author Andrei Varabyeu
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FinishLaunchRS extends EntryCreatedAsyncRS {

  @JsonProperty("number")
  private Long number;

  @JsonProperty("link")
  private String link;

  public FinishLaunchRS() {
  }

  public FinishLaunchRS(String id, Long number, String link) {
    super(id);
    this.number = number;
    this.link = link;
  }
}
