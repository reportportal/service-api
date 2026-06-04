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

package com.epam.reportportal.base.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Shared entity can used for sending information to client about shared resource. Contains only information about name
 * and owner of entity.
 *
 * @author Aliaksei_Makayed
 *
 */
@Setter
@Getter
@ToString
@JsonInclude(Include.NON_NULL)
public class OwnedEntityResource {

  @JsonProperty(value = "id")
  private String id;

  @JsonProperty(value = "name")
  private String name;

  @JsonProperty(value = "owner")
  private String owner;

  @JsonProperty(value = "description")
  private String description;

  @JsonProperty(value = "locked")
  private boolean locked;

}
