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
 * Event published when an issue type is defined for a test item. Use {@link #isSystemEvent()} to
 * distinguish between user-initiated definitions and analyzer-initiated definitions.
 *
 * @author Andrei Varabyeu
 */
@Setter
@Getter
@NoArgsConstructor
public class ItemIssueTypeDefinedEvent extends AbstractEvent<TestItemActivityResource> {

  private RelevantItemInfo relevantItemInfo;

  /**
   * Constructor for user-initiated issue type definition.
   *
   * @param before    Test item state before issue type definition
   * @param after     Test item state after issue type definition
   * @param userId    User ID who defined the issue type
   * @param userLogin User login who defined the issue type
   * @param orgId     Organization ID
   */
  public ItemIssueTypeDefinedEvent(TestItemActivityResource before, TestItemActivityResource after,
      Long userId, String userLogin, Long orgId) {
    super(userId, userLogin, before, after);
    this.organizationId = orgId;
  }

  /**
   * Constructor for analyzer-initiated issue type definition.
   *
   * @param before           Test item state before issue type definition
   * @param after            Test item state after issue type definition
   * @param analyzerName     Name of the analyzer that defined the issue type (e.g., "auto-analyzer")
   * @param relevantItemInfo Information about the relevant item used by analyzer
   * @param orgId            Organization ID
   */
  public ItemIssueTypeDefinedEvent(TestItemActivityResource before, TestItemActivityResource after,
      String analyzerName, RelevantItemInfo relevantItemInfo, Long orgId) {
    super();
    this.userLogin = analyzerName;
    this.before = before;
    this.after = after;
    this.relevantItemInfo = relevantItemInfo;
    this.organizationId = orgId;
  }
}
