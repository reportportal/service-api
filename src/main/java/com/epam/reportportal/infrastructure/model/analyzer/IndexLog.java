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

package com.epam.reportportal.infrastructure.model.analyzer;

import com.epam.reportportal.infrastructure.databind.LocalDateTimeSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents log container in index/analysis request/response.
 *
 * @author Ivan Sharamet
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IndexLog {

  @JsonProperty("logId")
  private Long logId;

  @JsonProperty("logLevel")
  private int logLevel;

  @JsonProperty("logTime")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  private LocalDateTime logTime;

  @JsonProperty("message")
  private String message;

  @JsonProperty("clusterId")
  private Long clusterId;

}
