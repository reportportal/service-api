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
import com.epam.reportportal.reporting.StartLaunchRQ;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * System event published when a launch start request is received.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Getter
@NoArgsConstructor
public class StartLaunchRqEvent extends AbstractEvent<Void> {

  private String projectName;
  private StartLaunchRQ startLaunchRQ;

  /**
   * Constructs a StartLaunchRqEvent.
   *
   * @param projectName   The name of the project
   * @param startLaunchRQ The start launch request
   */
  public StartLaunchRqEvent(String projectName, StartLaunchRQ startLaunchRQ) {
    super();
    this.projectName = projectName;
    this.startLaunchRQ = startLaunchRQ;
  }

  @Override
  public boolean shouldPublishToRabbitMQ() {
    return false;
  }
}
