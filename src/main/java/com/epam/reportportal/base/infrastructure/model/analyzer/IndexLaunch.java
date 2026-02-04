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

package com.epam.reportportal.base.infrastructure.model.analyzer;

import com.epam.reportportal.base.infrastructure.databind.LocalDateTimeSerializer;
import com.epam.reportportal.base.infrastructure.model.project.AnalyzerConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents launch container in index/analysis request/response.
 *
 * @author Ivan Sharamet
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IndexLaunch {

  @JsonProperty("launchId")
  private Long launchId;

  @JsonProperty("launchName")
  private String launchName;

  @JsonProperty("launchStartTime")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  private LocalDateTime launchStartTime;

  @JsonProperty("project")
  private Long projectId;

  @JsonProperty("analyzerConfig")
  private AnalyzerConfig analyzerConfig;

  @JsonProperty("testItems")
  private List<IndexTestItem> testItems;

  @JsonProperty("clusters")
  private Map<Long, String> clusters;

  @JsonProperty("launchNumber")
  private Long launchNumber;

  @JsonProperty("previousLaunchId")
  private Long previousLaunchId;

}
