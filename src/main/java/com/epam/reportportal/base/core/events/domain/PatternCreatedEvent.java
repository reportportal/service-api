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

import com.epam.reportportal.base.model.activity.PatternTemplateActivityResource;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Event published when a pattern template is created.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Getter
@NoArgsConstructor
public class PatternCreatedEvent extends AbstractEvent<Void> {

  private PatternTemplateActivityResource patternTemplateActivityResource;

  /**
   * Constructs a PatternCreatedEvent.
   *
   * @param userId                          The ID of the user who created the pattern
   * @param userLogin                       The login of the user who created the pattern
   * @param patternTemplateActivityResource The pattern template activity resource
   * @param orgId                           The organization ID
   */
  public PatternCreatedEvent(Long userId, String userLogin,
      PatternTemplateActivityResource patternTemplateActivityResource, Long orgId) {
    super(userId, userLogin);
    this.patternTemplateActivityResource = patternTemplateActivityResource;
    this.organizationId = orgId;
  }

}
