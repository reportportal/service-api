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

package com.epam.reportportal.core.events.domain.item;

import com.epam.reportportal.core.events.domain.AbstractEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * System event triggered when an issue is resolved.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Getter
@NoArgsConstructor
public class IssueResolvedEvent extends AbstractEvent<Void> {

  private Long itemId;
  private Long launchId;

  /**
   * Constructs an IssueResolvedEvent.
   *
   * @param itemId    The ID of the test item whose issue was resolved
   * @param launchId  The ID of the launch containing the test item
   * @param projectId The project ID
   */
  public IssueResolvedEvent(Long itemId, Long launchId, Long projectId) {
    super();
    this.itemId = itemId;
    this.launchId = launchId;
    this.projectId = projectId;
  }

}
