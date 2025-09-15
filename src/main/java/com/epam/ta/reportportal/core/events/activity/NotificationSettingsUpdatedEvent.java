/*
 * Copyright 2025 EPAM Systems
 */

package com.epam.ta.reportportal.core.events.activity;

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.processParameter;
import static com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum.NOTIFICATIONS_EMAIL_ENABLED;
import static com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum.NOTIFICATIONS_ENABLED;
import static com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum.NOTIFICATIONS_SLACK_ENABLED;
import static com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum.NOTIFICATIONS_TELEGRAM_ENABLED;

import com.epam.ta.reportportal.builder.ActivityBuilder;
import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityAction;
import com.epam.ta.reportportal.entity.activity.EventAction;
import com.epam.ta.reportportal.entity.activity.EventObject;
import com.epam.ta.reportportal.entity.activity.EventPriority;
import com.epam.ta.reportportal.entity.activity.EventSubject;
import com.epam.ta.reportportal.model.activity.ProjectAttributesActivityResource;
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
