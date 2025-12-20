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

package com.epam.reportportal.infrastructure.events;

import com.epam.reportportal.core.events.domain.AbstractEvent;
import com.epam.reportportal.reporting.StartTestItemRQ;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * System event published when a root test item start request is received.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Getter
@NoArgsConstructor
public class StartRootItemRqEvent extends AbstractEvent<Void> {

  private String projectName;
  private StartTestItemRQ startTestItemRQ;

  /**
   * Constructs a StartRootItemRqEvent.
   *
   * @param projectName     The name of the project
   * @param startTestItemRQ The start test item request
   */
  public StartRootItemRqEvent(String projectName, StartTestItemRQ startTestItemRQ) {
    super();
    this.projectName = projectName;
    this.startTestItemRQ = startTestItemRQ;
  }

  @Override
  public boolean shouldPublishToRabbitMQ() {
    return false;
  }
}
