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

package com.epam.reportportal.base.core.events.domain;

import com.epam.reportportal.base.model.activity.TestItemActivityResource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Event published when a ticket is linked to a test item issue. Use {@link #isSystemEvent()} to distinguish between
 * user-initiated links and analyzer-initiated links.
 *
 * @author Andrei Varabyeu
 */
@Setter
@Getter
@NoArgsConstructor
public class LinkTicketEvent extends AbstractEvent<TestItemActivityResource> {

  /**
   * Constructor for user-initiated ticket link.
   *
   * @param before         Test item state before linking
   * @param after          Test item state after linking
   * @param userId         User ID who linked the ticket
   * @param userLogin      User login who linked the ticket
   * @param organizationId Organization ID
   */
  public LinkTicketEvent(TestItemActivityResource before, TestItemActivityResource after,
      Long userId, String userLogin, Long organizationId) {
    super(userId, userLogin, before, after);
    this.organizationId = organizationId;
  }

  /**
   * Constructor for analyzer-initiated ticket link.
   *
   * @param before         Test item state before linking
   * @param after          Test item state after linking
   * @param analyzerName   Name of the analyzer that linked the ticket (e.g., "auto-analyzer")
   * @param organizationId Organization ID
   */
  public LinkTicketEvent(TestItemActivityResource before, TestItemActivityResource after,
      String analyzerName, Long organizationId) {
    super();
    this.userLogin = analyzerName;
    this.before = before;
    this.after = after;
    this.organizationId = organizationId;
  }
}
