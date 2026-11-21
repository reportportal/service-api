/*
 * Copyright 2019 EPAM Systems
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

package com.epam.reportportal.base.infrastructure.persistence.entity.widget.content;

import com.epam.reportportal.base.infrastructure.persistence.entity.item.ItemAttributePojo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

/**
 * Unique bug table row: sub-type, launch, URL, and statistics link.
 *
 * @author Ivan Budayeu
 */
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UniqueBugContent implements Serializable {

  @JsonProperty(value = "submitter")
  private String submitter;

  @JsonProperty(value = "url")
  private String url;

  @JsonProperty(value = "submitDate")
  private Instant submitDate;

  @JsonProperty(value = "items")
  private List<ItemInfo> items = Lists.newArrayList();

  @Setter
  @Getter
  public static class ItemInfo implements Serializable {

    @JsonProperty(value = "itemId")
    private Long testItemId;

    @JsonProperty(value = "itemName")
    private String testItemName;

    @JsonProperty(value = "launchId")
    private Long launchId;

    @JsonProperty(value = "path")
    private String path;

    @JsonProperty(value = "attributes")
    private Set<ItemAttributePojo> itemAttributeResources = Sets.newHashSet();

  }
}
