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

package com.epam.reportportal.base.infrastructure.persistence.entity.item.issue;

import java.io.Serializable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class IssueEntityPojo implements Serializable {

  private Long itemId;
  private Long issueTypeId;
  private String description;
  private boolean autoAnalyzed;
  private boolean ignoreAnalyzer;

  public IssueEntityPojo() {
  }

  public IssueEntityPojo(Long itemId, Long issueTypeId, String description, boolean autoAnalyzed,
      boolean ignoreAnalyzer) {
    this.itemId = itemId;
    this.issueTypeId = issueTypeId;
    this.description = description;
    this.autoAnalyzed = autoAnalyzed;
    this.ignoreAnalyzer = ignoreAnalyzer;
  }

  public Long getItemId() {
    return itemId;
  }

  public void setItemId(Long itemId) {
    this.itemId = itemId;
  }

  public Long getIssueTypeId() {
    return issueTypeId;
  }

  public void setIssueTypeId(Long issueTypeId) {
    this.issueTypeId = issueTypeId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isAutoAnalyzed() {
    return autoAnalyzed;
  }

  public void setAutoAnalyzed(boolean autoAnalyzed) {
    this.autoAnalyzed = autoAnalyzed;
  }

  public boolean isIgnoreAnalyzer() {
    return ignoreAnalyzer;
  }

  public void setIgnoreAnalyzer(boolean ignoreAnalyzer) {
    this.ignoreAnalyzer = ignoreAnalyzer;
  }
}
