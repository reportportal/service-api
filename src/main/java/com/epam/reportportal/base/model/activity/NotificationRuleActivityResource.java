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

package com.epam.reportportal.base.model.activity;

import com.epam.reportportal.base.reporting.ItemAttributeResource;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;

@Data
public class NotificationRuleActivityResource {

  @JsonProperty(value = "id", required = true)
  private Long id;

  @JsonProperty(value = "projectId", required = true)
  private Long projectId;

  @JsonProperty(value = "name", required = true)
  private String name;

  @JsonProperty(value = "recipients")
  private List<String> recipients;

  @JsonProperty(value = "launchNames")
  private List<String> launchNames;

  @JsonProperty(value = "attributes")
  @JsonDeserialize(as = LinkedHashSet.class)
  private Set<ItemAttributeResource> attributes;

  @JsonProperty(value = "enabled")
  private boolean enabled;

  @JsonProperty(value = "type")
  private String type;

  @JsonProperty(value = "attributesOperator")
  private String attributesOperator;

  @JsonProperty(value = "ruleDetails")
  private Map<String, Object> ruleDetails;

  @JsonProperty(value = "sendCase")
  private String sendCase;
}
