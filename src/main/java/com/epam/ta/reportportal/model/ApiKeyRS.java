/*
 * Copyright 2023 EPAM Systems
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Api key representation for response
 *
 * @author Andrei Piankouski
 */
@JsonInclude(Include.NON_NULL)
@Getter
@Setter
@ToString
public class ApiKeyRS {

  @NotNull
  @JsonProperty(value = "id", required = true)
  private Long id;

  @NotNull
  @JsonProperty(value = "name", required = true)
  private String name;

  @NotNull
  @JsonProperty(value = "user_id", required = true)
  private Long userId;

  @NotNull
  @JsonProperty(value = "created_at")
  private Instant createdAt;

  @JsonProperty(value = "last_used_at")
  private Instant lastUsedAt;

  @JsonProperty(value = "api_key")
  private String apiKey;

}
