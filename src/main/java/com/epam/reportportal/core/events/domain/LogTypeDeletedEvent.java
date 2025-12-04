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

import com.epam.reportportal.model.activity.LogTypeActivityResource;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Event published when a log type is deleted.
 */
@Getter
@NoArgsConstructor
public class LogTypeDeletedEvent extends AbstractEvent<LogTypeActivityResource> {

  public LogTypeDeletedEvent(LogTypeActivityResource before, Long userId, String userLogin) {
    super(userId, userLogin, before, null);
  }
}
