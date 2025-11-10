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

package com.epam.reportportal.core.events.activity;

import com.epam.reportportal.core.events.ActivityEvent;
import com.epam.reportportal.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventSubject;
import com.epam.reportportal.model.activity.NotificationRuleActivityResource;

/**
 * Activity event for creation of a project notification rule.
 */
public class NotificationRuleCreatedEvent extends AbstractEvent implements ActivityEvent {

  private final NotificationRuleActivityResource notificationRuleActivityResource;
  private final Long orgId;

  public NotificationRuleCreatedEvent(NotificationRuleActivityResource notificationRuleActivityResource,
      Long userId, String userLogin, Long orgId) {
    super(userId, userLogin);
    this.notificationRuleActivityResource = notificationRuleActivityResource;
    this.orgId = orgId;
  }

  @Override
  public Activity toActivity() {
    return new ActivityBuilder()
        .addCreatedNow()
        .addAction(EventAction.CREATE)
        .addEventName(ActivityAction.CREATE_NOTIFICATION_RULE.getValue())
        .addPriority(EventPriority.MEDIUM)
        .addObjectId(notificationRuleActivityResource.getId())
        .addObjectName(notificationRuleActivityResource.getName())
        .addObjectType(EventObject.NOTIFICATION_RULE)
        .addProjectId(notificationRuleActivityResource.getProjectId())
        .addOrganizationId(orgId)
        .addSubjectId(getUserId())
        .addSubjectName(getUserLogin())
        .addSubjectType(EventSubject.USER)
        .get();
  }
}
