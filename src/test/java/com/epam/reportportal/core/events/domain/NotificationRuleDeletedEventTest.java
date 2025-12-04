/*
 * Copyright 2025 EPAM Systems
 */

package com.epam.reportportal.core.events.domain;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.epam.reportportal.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventSubject;
import com.epam.reportportal.model.activity.NotificationRuleActivityResource;
import org.junit.jupiter.api.Test;

class NotificationRuleDeletedEventTest {

  @Test
  void toActivityWhenNotificationRuleProvidedShouldBuildDeleteActivity() {
    // given
    NotificationRuleActivityResource resource = new NotificationRuleActivityResource();
    resource.setId(11L);
    resource.setProjectId(22L);
    resource.setName("rule-A");
    // when
    NotificationRuleDeletedEvent event = new NotificationRuleDeletedEvent(resource, 100L, "user",
        200L);
    var activity = event.toActivity();
    // then
    assertNotNull(activity);
    assertEquals(EventAction.DELETE, activity.getAction());
    assertEquals("deleteNotificationRule", activity.getEventName());
    assertEquals(EventPriority.MEDIUM, activity.getPriority());
    assertEquals(11L, activity.getObjectId());
    assertEquals("rule-A", activity.getObjectName());
    assertEquals(22L, activity.getProjectId());
    assertEquals(200L, activity.getOrganizationId());
    assertEquals(100L, activity.getSubjectId());
    assertEquals("user", activity.getSubjectName());
    assertEquals(EventSubject.USER, activity.getSubjectType());
  }
}


