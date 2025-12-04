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

package com.epam.reportportal.core.events.domain;

import com.epam.reportportal.model.activity.TestItemActivityResource;
import com.epam.reportportal.model.analyzer.RelevantItemInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Andrei Varabyeu
 */
@Setter
@Getter
@NoArgsConstructor
public class ItemIssueTypeDefinedEvent extends AbstractEvent<TestItemActivityResource> {

  private RelevantItemInfo relevantItemInfo;

  public ItemIssueTypeDefinedEvent(TestItemActivityResource before, TestItemActivityResource after,
      Long userId, String userLogin, Long orgId) {
    super(userId, userLogin, before, after);
    this.organizationId = orgId;
  }

  public ItemIssueTypeDefinedEvent(TestItemActivityResource before, TestItemActivityResource after,
      String userLogin,
      RelevantItemInfo relevantItemInfo, Long orgId) {
    super();
    this.before = before;
    this.after = after;
    this.relevantItemInfo = relevantItemInfo;
    this.organizationId = orgId;
  }

  public boolean isAutoAnalyzed() {
    return getAfter() != null && getAfter().isAutoAnalyzed();
  }
}
