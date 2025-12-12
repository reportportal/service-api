/*
 * Copyright 2025 EPAM Systems
 */

package com.epam.reportportal.core.events.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.epam.reportportal.core.events.activity.converter.NotificationRuleUpdatedEventConverter;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventSubject;
import com.epam.reportportal.model.activity.NotificationRuleActivityResource;
import java.util.List;
import org.junit.jupiter.api.Test;

class NotificationRuleUpdatedEventTest {

  @Test
  void toActivityWhenRuleUpdatedShouldBuildUpdateActivity() {
    // given
    NotificationRuleActivityResource before = new NotificationRuleActivityResource();
    before.setId(11L);
    before.setProjectId(22L);
    before.setName("rule-A");
    before.setRecipients(List.of("a@a"));
    before.setEnabled(true);
    before.setSendCase("always");

    NotificationRuleActivityResource after = new NotificationRuleActivityResource();
    after.setId(11L);
    after.setProjectId(22L);
    after.setName("rule-B");
    after.setRecipients(List.of("a@a", "b@b"));
    after.setEnabled(false);
    after.setSendCase("failed");

    var event = new NotificationRuleUpdatedEvent(before, after, 100L, "user", 200L);

    // when
    NotificationRuleUpdatedEventConverter converter = new NotificationRuleUpdatedEventConverter();
    var activity = converter.convert(event);

    // then
    assertNotNull(activity);
    assertEquals(EventAction.UPDATE, activity.getAction());
    assertEquals("updateNotificationRule", activity.getEventName());
    assertEquals(EventPriority.MEDIUM, activity.getPriority());
    assertEquals(11L, activity.getObjectId());
    assertEquals("rule-B", activity.getObjectName());
    assertEquals(22L, activity.getProjectId());
    assertEquals(200L, activity.getOrganizationId());
    assertEquals(100L, activity.getSubjectId());
    assertEquals("user", activity.getSubjectName());
    assertEquals(EventSubject.USER, activity.getSubjectType());
  }
}


