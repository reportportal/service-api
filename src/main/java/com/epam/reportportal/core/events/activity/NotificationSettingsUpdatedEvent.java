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

import static com.epam.reportportal.core.events.activity.util.ActivityDetailsUtil.processParameter;
import static com.epam.reportportal.infrastructure.persistence.entity.enums.ProjectAttributeEnum.NOTIFICATIONS_EMAIL_ENABLED;
import static com.epam.reportportal.infrastructure.persistence.entity.enums.ProjectAttributeEnum.NOTIFICATIONS_ENABLED;
import static com.epam.reportportal.infrastructure.persistence.entity.enums.ProjectAttributeEnum.NOTIFICATIONS_SLACK_ENABLED;
import static com.epam.reportportal.infrastructure.persistence.entity.enums.ProjectAttributeEnum.NOTIFICATIONS_TELEGRAM_ENABLED;

import com.epam.reportportal.core.events.ActivityEvent;
import com.epam.reportportal.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventSubject;
import com.epam.reportportal.model.activity.ProjectAttributesActivityResource;
import java.util.stream.Stream;

/**
 * Activity event for updating project notification settings (enabled flags).
 */
public class NotificationSettingsUpdatedEvent extends AroundEvent<ProjectAttributesActivityResource>
    implements ActivityEvent {

  private final Long orgId;

  public NotificationSettingsUpdatedEvent(ProjectAttributesActivityResource before,
      ProjectAttributesActivityResource after,
      Long userId,
      String userLogin,
      Long orgId) {
    super(userId, userLogin, before, after);
    this.orgId = orgId;
  }

  @Override
  public Activity toActivity() {
    final ActivityBuilder activityBuilder = new ActivityBuilder()
        .addCreatedNow()
        .addAction(EventAction.UPDATE)
        .addEventName(ActivityAction.UPDATE_NOTIFICATION_SETTINGS.getValue())
        .addPriority(EventPriority.MEDIUM)
        .addObjectId(getBefore().getProjectId())
        .addObjectName("notifications_settings")
        .addObjectType(EventObject.NOTIFICATION_RULE)
        .addProjectId(getBefore().getProjectId())
        .addOrganizationId(orgId)
        .addSubjectId(getUserId())
        .addSubjectName(getUserLogin())
        .addSubjectType(EventSubject.USER);

    Stream.of(NOTIFICATIONS_ENABLED, NOTIFICATIONS_EMAIL_ENABLED, NOTIFICATIONS_TELEGRAM_ENABLED,
            NOTIFICATIONS_SLACK_ENABLED)
        .map(type -> processParameter(getBefore().getConfig(), getAfter().getConfig(), type.getAttribute()))
        .forEach(activityBuilder::addHistoryField);

    return activityBuilder.get();
  }
}
