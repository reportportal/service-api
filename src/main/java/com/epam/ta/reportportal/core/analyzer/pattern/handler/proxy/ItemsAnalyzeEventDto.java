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

package com.epam.ta.reportportal.core.analyzer.pattern.handler.proxy;

import java.util.List;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class ItemsAnalyzeEventDto {

  private long projectId;
  private long launchId;
  private List<Long> itemIds;
  private boolean isLastItem;

  public ItemsAnalyzeEventDto() {
  }

  public ItemsAnalyzeEventDto(long projectId, long launchId, List<Long> itemIds) {
    this.projectId = projectId;
    this.launchId = launchId;
    this.itemIds = itemIds;
  }

  public ItemsAnalyzeEventDto(long projectId, long launchId, List<Long> itemIds,
      boolean isLastItem) {
    this.projectId = projectId;
    this.launchId = launchId;
    this.itemIds = itemIds;
    this.isLastItem = isLastItem;
  }

  public long getProjectId() {
    return projectId;
  }

  public long getLaunchId() {
    return launchId;
  }

  public List<Long> getItemIds() {
    return itemIds;
  }

  public void setProjectId(long projectId) {
    this.projectId = projectId;
  }

  public void setLaunchId(long launchId) {
    this.launchId = launchId;
  }

  public void setItemIds(List<Long> itemIds) {
    this.itemIds = itemIds;
  }

  public boolean isLastItem() {
    return isLastItem;
  }

  public void setLastItem(boolean lastItem) {
    isLastItem = lastItem;
  }

  @Override
  public String toString() {
    return "ItemsPatternMessage{" +
        "projectId=" + projectId +
        ", launchId=" + launchId +
        ", itemIds=" + itemIds +
        ", isLastItem=" + isLastItem +
        '}';
  }
}
