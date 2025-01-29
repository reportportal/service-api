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

package com.epam.ta.reportportal.model.project;

import com.epam.ta.reportportal.model.ModelViews;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import java.time.Instant;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Project info resource representation for responses<br> {@link ModelViews.DefaultView} used as
 * default fields output<br> {@link ModelViews.FullProjectInfoView} used as extended fields
 * output<br>
 *
 * @author Dzmitry_Kavalets
 * @author Andrei_Ramanchuk
 */

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ProjectInfoResource {

  @NotNull
  @JsonProperty(value = "id")
  private Long projectId;

  @NotBlank
  @JsonProperty(value = "projectName")
  private String projectName;

  @NotNull
  @JsonProperty(value = "usersQuantity")
  private Integer usersQuantity;

  @NotNull
  @JsonProperty(value = "launchesQuantity")
  private Integer launchesQuantity;

  @JsonProperty(value = "launchesPerUser")
  @JsonView(ModelViews.FullProjectInfoView.class)
  private List<LaunchesPerUser> launchesPerUser;

  @JsonProperty(value = "uniqueTickets")
  @JsonView(ModelViews.FullProjectInfoView.class)
  private Integer uniqueTickets;

  @JsonProperty(value = "launchesPerWeek")
  @JsonView(ModelViews.FullProjectInfoView.class)
  private String launchesPerWeek;

  @NotNull
  @JsonProperty(value = "lastRun")
  private Instant lastRun;

  @NotNull
  @JsonProperty(value = "creationDate")
  private Instant creationDate;

  @JsonProperty(value = "entryType")
  private String entryType;

  @JsonProperty(value = "organization")
  private String organization;

}
