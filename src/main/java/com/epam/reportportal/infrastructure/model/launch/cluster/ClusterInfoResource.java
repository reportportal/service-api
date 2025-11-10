/*
 * Copyright 2021 EPAM Systems
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

package com.epam.reportportal.infrastructure.model.launch.cluster;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class ClusterInfoResource {

  @JsonProperty(value = "id")
  private Long id;

  @JsonProperty(value = "index")
  private Long index;

  @JsonProperty(value = "launchId")
  private Long launchId;

  @JsonProperty(value = "message")
  private String message;

  @JsonProperty(value = "metadata")
  private Map<String, Object> metadata;

  @JsonProperty(value = "matchedTests")
  private Long matchedTests;

  public ClusterInfoResource(Long id, Long index, Long launchId, String message, Long matchedTests) {
    this.id = id;
    this.index = index;
    this.launchId = launchId;
    this.message = message;
    this.matchedTests = matchedTests;
  }

}
