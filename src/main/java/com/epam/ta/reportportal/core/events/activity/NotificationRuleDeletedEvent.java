/*
 * Copyright 2025 EPAM Systems
 */

package com.epam.ta.reportportal.core.events.activity;

import com.epam.ta.reportportal.builder.ActivityBuilder;
import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityAction;
import com.epam.ta.reportportal.entity.activity.EventAction;
import com.epam.ta.reportportal.entity.activity.EventObject;
import com.epam.ta.reportportal.entity.activity.EventPriority;
import com.epam.ta.reportportal.entity.activity.EventSubject;
import com.epam.ta.reportportal.model.activity.NotificationRuleActivityResource;

/**
 * Activity event for deletion of a project notification rule.
 */
public class NotificationRuleDeletedEvent extends BeforeEvent<NotificationRuleActivityResource> implements ActivityEvent {

  private final Long orgId;

  public NotificationRuleDeletedEvent(NotificationRuleActivityResource before,
      Long userId, String userLogin, Long orgId) {
    super(userId, userLogin, before);
    this.orgId = orgId;
  }

  @Override
  public Activity toActivity() {
    return new ActivityBuilder()
        .addCreatedNow()
        .addAction(EventAction.DELETE)
        .addEventName(ActivityAction.DELETE_NOTIFICATION_RULE.getValue())
        .addPriority(EventPriority.MEDIUM)
        .addObjectId(getBefore().getId())
        .addObjectName(getBefore().getName())
        .addObjectType(EventObject.NOTIFICATION_RULE)
        .addProjectId(getBefore().getProjectId())
        .addOrganizationId(orgId)
        .addSubjectId(getUserId())
        .addSubjectName(getUserLogin())
        .addSubjectType(EventSubject.USER)
        .get();
  }
}
