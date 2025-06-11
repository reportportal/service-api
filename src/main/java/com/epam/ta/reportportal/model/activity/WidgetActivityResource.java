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

package com.epam.ta.reportportal.model.activity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import lombok.Data;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Data
public class WidgetActivityResource {

  @JsonProperty(value = "id", required = true)
  private Long id;

  @JsonProperty(value = "projectId", required = true)
  private Long projectId;

  @JsonProperty(value = "name", required = true)
  private String name;

  @JsonProperty(value = "description")
  private String description;

  @JsonProperty(value = "itemsCount")
  private int itemsCount;

  @JsonProperty(value = "contentFields")
  @JsonDeserialize(as = LinkedHashSet.class)
  private Set<String> contentFields;

  @JsonProperty(value = "widgetOptions")
  @JsonDeserialize(as = LinkedHashMap.class)
  private Map<String, Object> widgetOptions;

}
