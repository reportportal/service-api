package com.epam.ta.reportportal.model.organization;

/*
 * Copyright 2024 EPAM Systems
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


import com.epam.ta.reportportal.entity.enums.OrganizationType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Basic JSON representation of Organization.
 *
 * @author Andrei Piankouski
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@ToString
public class OrganizationInfoResource {

  @NotNull
  @JsonProperty(value = "id", required = true)
  private Long id;

  @NotNull
  @JsonProperty(value = "name", required = true)
  private String name;

  @NotNull
  @JsonProperty(value = "slug", required = true)
  private String slug;

  @NotNull
  @JsonProperty(value = "type", required = true)
  private OrganizationType type;

  @NotNull
  @JsonProperty(value = "creationDate", required = true)
  private LocalDateTime creationDate;

  @JsonProperty(value = "usersQuantity", required = true)
  private int usersQuantity;

  @JsonProperty(value = "projectsQuantity", required = true)
  private int projectsQuantity;

  @JsonProperty(value = "launchesQuantity", required = true)
  private int launchesQuantity;

  @JsonProperty(value = "lastRun", required = true)
  private LocalDateTime lastRun;

}
