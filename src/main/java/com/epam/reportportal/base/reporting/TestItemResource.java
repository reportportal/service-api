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

package com.epam.reportportal.base.reporting;

import com.epam.reportportal.base.reporting.databind.MultiFormatDateDeserializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * JSON Representation of Report Portal domain object
 *
 * @author Andrei Varabyeu
 */
@JsonInclude(Include.NON_NULL)
@Getter
@Setter
@ToString
public class TestItemResource {

  @JsonProperty(value = "id")
  private Long itemId;

  @JsonProperty(value = "uuid")
  private String uuid;

  @JsonProperty(value = "name")
  private String name;

  @JsonProperty(value = "codeRef")
  private String codeRef;

  @JsonProperty(value = "description")
  private String description;

  @JsonProperty(value = "parameters")
  private List<ParameterResource> parameters;

  @JsonProperty(value = "attributes")
  private Set<ItemAttributeResource> attributes;

  @JsonProperty(value = "type")
  private String type;

  @JsonProperty(value = "startTime")
  @JsonDeserialize(using = MultiFormatDateDeserializer.class)
  private Instant startTime;

  @JsonProperty(value = "endTime")
  @JsonDeserialize(using = MultiFormatDateDeserializer.class)
  private Instant endTime;

  @JsonProperty(value = "status")
  private String status;

  @JsonProperty(value = "statistics")
  private StatisticsResource statisticsResource;

  @JsonProperty(value = "parent")
  private Long parent;

  @JsonProperty(value = "pathNames")
  private PathNameResource pathNames;

  @JsonProperty(value = "launchStatus")
  private String launchStatus;

  @JsonProperty(value = "issue")
  private Issue issue;

  @JsonProperty(value = "hasChildren")
  private boolean hasChildren;

  @JsonProperty(value = "hasStats")
  private boolean hasStats;

  @JsonProperty(value = "launchId")
  private Long launchId;

  @JsonProperty(value = "uniqueId")
  private String uniqueId;

  @JsonProperty(value = "testCaseId")
  private String testCaseId;

  @JsonProperty(value = "testCaseHash")
  private Integer testCaseHash;

  @JsonProperty(value = "patternTemplates")
  private Set<String> patternTemplates;

  @JsonProperty(value = "retries")
  private List<TestItemResource> retries;

  @JsonProperty(value = "path")
  private String path;

  @JsonProperty(value = "hasNestedSteps")
  private boolean hasNestedSteps;

  @JsonProperty(value = "analysisOwner")
  private String analysisOwner;

}
