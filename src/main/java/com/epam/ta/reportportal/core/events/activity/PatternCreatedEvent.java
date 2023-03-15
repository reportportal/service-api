/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.core.events.activity;

import static com.epam.ta.reportportal.entity.activity.Activity.ActivityEntityType.PATTERN;
import static com.epam.ta.reportportal.entity.activity.ActivityAction.CREATE_PATTERN;

import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import com.epam.ta.reportportal.ws.model.activity.PatternTemplateActivityResource;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class PatternCreatedEvent extends AbstractEvent implements ActivityEvent {

  private PatternTemplateActivityResource patternTemplateActivityResource;

  public PatternCreatedEvent() {
  }

  public PatternCreatedEvent(Long userId, String userLogin,
      PatternTemplateActivityResource patternTemplateActivityResource) {
    super(userId, userLogin);
    this.patternTemplateActivityResource = patternTemplateActivityResource;
  }

  public PatternTemplateActivityResource getPatternTemplateActivityResource() {
    return patternTemplateActivityResource;
  }

  public void setPatternTemplateActivityResource(
      PatternTemplateActivityResource patternTemplateActivityResource) {
    this.patternTemplateActivityResource = patternTemplateActivityResource;
  }

  @Override
  public Activity toActivity() {
    return new ActivityBuilder().addCreatedNow()
        .addActivityEntityType(PATTERN)
        .addAction(CREATE_PATTERN)
        .addObjectId(patternTemplateActivityResource.getId())
        .addObjectName(patternTemplateActivityResource.getName())
        .addProjectId(patternTemplateActivityResource.getProjectId())
        .addUserId(getUserId())
        .addUserName(getUserLogin())
        .get();
  }
}
