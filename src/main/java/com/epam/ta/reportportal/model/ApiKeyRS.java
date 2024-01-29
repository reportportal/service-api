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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import javax.validation.constraints.NotNull;

/**
 * Api key representation for response
 *
 * @author Andrei Piankouski
 */
@JsonInclude(Include.NON_NULL)
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
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
  @JsonProperty(value = "created_at")
  private Date createdAt;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  @JsonProperty(value = "last_used_at")
  private Date lastUsedAt;

  @JsonProperty(value = "api_key")
  private String apiKey;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public Date getLastUsedAt() {
    return lastUsedAt;
  }

  public void setLastUsedAt(Date lastUsedAt) {
    this.lastUsedAt = lastUsedAt;
  }

  @Override
  public String toString() {
    return "ApiKeyRS{" + "id=" + id + ", name='" + name + '\'' + ", userId=" + userId
        + ", createdAt=" + createdAt + ", lastUsedAt=" + lastUsedAt + ", apiKey='" + apiKey + '\''
        + '}';
  }
}
