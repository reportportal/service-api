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

package com.epam.reportportal.base.extension.event;

import com.epam.reportportal.base.core.events.domain.AbstractEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * System event published when launch auto-analysis finishes.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Getter
@NoArgsConstructor
public class LaunchAutoAnalysisFinishEvent extends AbstractEvent<Void> {

  private Long launchId;

  /**
   * Constructs a LaunchAutoAnalysisFinishEvent.
   *
   * @param launchId The ID of the launch
   */
  public LaunchAutoAnalysisFinishEvent(Long launchId) {
    super();
    this.launchId = launchId;
  }
}
