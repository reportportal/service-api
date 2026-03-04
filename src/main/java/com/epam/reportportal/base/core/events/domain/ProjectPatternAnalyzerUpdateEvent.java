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

import com.epam.reportportal.base.model.activity.ProjectAttributesActivityResource;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Event published when a project pattern analyzer configuration is updated.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Getter
@Setter
@RequiredArgsConstructor
public class ProjectPatternAnalyzerUpdateEvent extends
    AbstractEvent<ProjectAttributesActivityResource> {

  /**
   * Constructs a ProjectPatternAnalyzerUpdateEvent.
   *
   * @param before    The pattern analyzer configuration state before the update
   * @param after     The pattern analyzer configuration state after the update
   * @param userId    The ID of the user who updated the configuration
   * @param userLogin The login of the user who updated the configuration
   * @param orgId     The organization ID
   */
  public ProjectPatternAnalyzerUpdateEvent(ProjectAttributesActivityResource before,
      ProjectAttributesActivityResource after, Long userId, String userLogin, Long orgId) {
    super(userId, userLogin, before, after);
    this.organizationId = orgId;
  }
}
