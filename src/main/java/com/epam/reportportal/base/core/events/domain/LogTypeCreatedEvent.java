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

import com.epam.reportportal.base.model.activity.LogTypeActivityResource;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Event published when a log type is created.
 */
@Getter
@NoArgsConstructor
public class LogTypeCreatedEvent extends AbstractEvent<Void> {

  private LogTypeActivityResource logTypeActivityResource;

  /**
   * Constructs a LogTypeCreatedEvent.
   *
   * @param logTypeActivityResource The log type activity resource
   * @param userId                  The ID of the user who created the log type
   * @param userLogin               The login of the user who created the log type
   */
  public LogTypeCreatedEvent(LogTypeActivityResource logTypeActivityResource, Long userId,
      String userLogin) {
    super(userId, userLogin);
    this.logTypeActivityResource = logTypeActivityResource;
  }
}
