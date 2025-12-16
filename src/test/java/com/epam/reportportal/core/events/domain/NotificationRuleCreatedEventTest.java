/*
 * Copyright 2025 EPAM Systems
 */

package com.epam.reportportal.core.events.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.epam.reportportal.ws.rabbit.activity.converter.NotificationRuleCreatedEventConverter;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventSubject;
import com.epam.reportportal.model.activity.NotificationRuleActivityResource;
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
    NotificationRuleCreatedEvent event = new NotificationRuleCreatedEvent(resource, 100L, "user",
        200L);

    NotificationRuleCreatedEventConverter converter = new NotificationRuleCreatedEventConverter();
    var activity = converter.convert(event);
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
