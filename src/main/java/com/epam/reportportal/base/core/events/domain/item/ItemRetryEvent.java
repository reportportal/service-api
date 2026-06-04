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

package com.epam.reportportal.base.core.events.domain.item;

import com.epam.reportportal.base.core.events.domain.AbstractEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * System event triggered when a test item is retried.
 *
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Getter
@NoArgsConstructor
public class ItemRetryEvent extends AbstractEvent<Void> {

  private Long launchId;
  private Long itemId;

  private ItemRetryEvent(Long projectId, Long launchId, Long itemId) {
    super();
    this.projectId = projectId;
    this.launchId = launchId;
    this.itemId = itemId;
  }

  public static ItemRetryEvent of(Long projectId, Long launchId, Long itemId) {
    return new ItemRetryEvent(projectId, launchId, itemId);
  }

}
