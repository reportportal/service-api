/*
 * Copyright 2025 EPAM Systems
 */

package com.epam.ta.reportportal.core.events.activity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.epam.ta.reportportal.entity.activity.EventAction;
import com.epam.ta.reportportal.entity.activity.EventPriority;
import com.epam.ta.reportportal.entity.activity.EventSubject;
import com.epam.ta.reportportal.model.activity.NotificationRuleActivityResource;
import org.junit.jupiter.api.Test;

class NotificationRuleCreatedEventTest {

  @Test
  void toActivityWhenNotificationRuleProvidedShouldBuildActivityWithExpectedFields() {
    // given
    NotificationRuleActivityResource resource = new NotificationRuleActivityResource();
    resource.setId(11L);
    resource.setProjectId(22L);
    resource.setName("rule-A");
    // when
    NotificationRuleCreatedEvent event = new NotificationRuleCreatedEvent(resource, 100L, "user", 200L);

    var activity = event.toActivity();
    // then
    assertNotNull(activity);
    assertEquals(EventAction.CREATE, activity.getAction());
    assertEquals("createNotificationRule", activity.getEventName());
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
