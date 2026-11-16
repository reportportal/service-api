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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents test item container in index/analysis request/response.
 *
 * @author Ivan Sharamet
 */
@Data
@NoArgsConstructor
public class IndexTestItem {

  @JsonProperty("testItemId")
  private Long testItemId;

  @JsonProperty("testItemName")
  private String testItemName;

  @JsonProperty("issueType")
  private String issueTypeLocator;

  @JsonSerialize(using = LocalDateTimeSerializer.class)
  private LocalDateTime startTime;

  @JsonProperty("logs")
  private Set<IndexLog> logs;

  @JsonProperty("uniqueId")
  private String uniqueId;

  // used for boost item if it was not analyzed by analyzer
  @JsonProperty("isAutoAnalyzed")
  private boolean isAutoAnalyzed;

  @JsonProperty("testCaseHash")
  private Integer testCaseHash;

}
