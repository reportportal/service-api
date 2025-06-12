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

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.PATTERN_NAME;

import com.epam.ta.reportportal.builder.ActivityBuilder;
import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityAction;
import com.epam.ta.reportportal.entity.activity.EventAction;
import com.epam.ta.reportportal.entity.activity.EventObject;
import com.epam.ta.reportportal.entity.activity.EventPriority;
import com.epam.ta.reportportal.entity.activity.EventSubject;
import com.epam.ta.reportportal.entity.activity.HistoryField;
import com.epam.ta.reportportal.model.activity.PatternTemplateActivityResource;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Setter
@Getter
public class PatternMatchedEvent extends AbstractEvent implements ActivityEvent {

  private String itemName;

  private Long itemId;
  private Long orgId;

  private PatternTemplateActivityResource patternTemplateActivityResource;

  public PatternMatchedEvent() {
  }

  public PatternMatchedEvent(String itemName, Long itemId,
      PatternTemplateActivityResource patternTemplateActivityResource, Long orgId) {
    this.itemName = itemName;
    this.itemId = itemId;
    this.patternTemplateActivityResource = patternTemplateActivityResource;
    this.orgId = orgId;
  }

  @Override
  public Activity toActivity() {
    HistoryField patternNameField = new HistoryField();
    patternNameField.setField(PATTERN_NAME);
    patternNameField.setNewValue(patternTemplateActivityResource.getName());

    return new ActivityBuilder()
        .addCreatedNow()
        .addAction(EventAction.MATCH)
        .addEventName(ActivityAction.PATTERN_MATCHED.getValue())
        .addPriority(EventPriority.LOW)
        .addObjectId(itemId)
        .addObjectName(itemName)
        .addObjectType(EventObject.ITEM_ISSUE)
        .addProjectId(patternTemplateActivityResource.getProjectId())
        .addOrganizationId(orgId)
        .addSubjectName("Pattern Analysis")
        .addSubjectType(EventSubject.RULE)
        .addHistoryField(Optional.of(patternNameField))
        .get();
  }
}
