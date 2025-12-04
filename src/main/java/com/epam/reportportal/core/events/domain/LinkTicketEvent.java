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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Andrei Varabyeu
 */
@Setter
@Getter
@NoArgsConstructor
public class LinkTicketEvent extends AbstractEvent<TestItemActivityResource> {

  private boolean isLinkedByAnalyzer;

  public LinkTicketEvent(TestItemActivityResource before, TestItemActivityResource after,
      Long userId, String userLogin,
      boolean isLinkedByAnalyzer, Long organizationId) {
    super(userId, userLogin, before, after);
    this.isLinkedByAnalyzer = isLinkedByAnalyzer;
    this.organizationId = organizationId;
  }

  public LinkTicketEvent(TestItemActivityResource before, TestItemActivityResource after,
      String userLogin,
      boolean isLinkedByAnalyzer, Long organizationId) {
    super();
    this.before = before;
    this.after = after;
    this.isLinkedByAnalyzer = isLinkedByAnalyzer;
    this.organizationId = organizationId;
  }
}
