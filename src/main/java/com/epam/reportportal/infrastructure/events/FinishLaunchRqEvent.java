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
import com.epam.reportportal.reporting.FinishExecutionRQ;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * System event published when a launch finish request is received.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Getter
@NoArgsConstructor
public class FinishLaunchRqEvent extends AbstractEvent<Void> {

  private String projectName;
  private String launchUuid;
  private FinishExecutionRQ finishExecutionRQ;

  /**
   * Constructs a FinishLaunchRqEvent.
   *
   * @param projectName       The name of the project
   * @param launchUuid        The UUID of the launch
   * @param finishExecutionRQ The finish execution request
   */
  public FinishLaunchRqEvent(String projectName, String launchUuid,
      FinishExecutionRQ finishExecutionRQ) {
    super();
    this.projectName = projectName;
    this.launchUuid = launchUuid;
    this.finishExecutionRQ = finishExecutionRQ;
  }

  @Override
  public boolean shouldPublishToRabbitMQ() {
    return false;
  }
}
