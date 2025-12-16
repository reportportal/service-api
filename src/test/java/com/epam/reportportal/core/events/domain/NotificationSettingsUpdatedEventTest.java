/*
 * Copyright 2025 EPAM Systems
 */

package com.epam.reportportal.core.events.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.epam.reportportal.ws.rabbit.activity.converter.NotificationSettingsUpdatedEventConverter;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventSubject;
import com.epam.reportportal.model.activity.ProjectAttributesActivityResource;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NotificationSettingsUpdatedEventTest {

  @Test
  void toActivityWhenSettingsChangedShouldBuildExpectedActivity() {
    // given
    ProjectAttributesActivityResource before = new ProjectAttributesActivityResource();
    before.setProjectId(10L);
    before.setProjectName("p1");
    Map<String, String> cfgBefore = new HashMap<>();
    cfgBefore.put("notifications.enabled", "false");
    cfgBefore.put("notifications.email.enabled", "false");
    cfgBefore.put("notifications.telegram.enabled", "false");
    cfgBefore.put("notifications.slack.enabled", "false");
    before.setConfig(cfgBefore);

    ProjectAttributesActivityResource after = new ProjectAttributesActivityResource();
    after.setProjectId(10L);
    after.setProjectName("p1");
    Map<String, String> cfgAfter = new HashMap<>(cfgBefore);
    cfgAfter.put("notifications.enabled", "true");
    cfgAfter.put("notifications.email.enabled", "true");
    after.setConfig(cfgAfter);

    var event = new NotificationSettingsUpdatedEvent(before, after, 5L, "user", 77L);

    // when
    NotificationSettingsUpdatedEventConverter converter = new NotificationSettingsUpdatedEventConverter();
    var activity = converter.convert(event);

    // then
    assertNotNull(activity);
    assertEquals(EventAction.UPDATE, activity.getAction());
    assertEquals("updateNotificationSettings", activity.getEventName());
    assertEquals(EventPriority.MEDIUM, activity.getPriority());
    assertEquals(10L, activity.getObjectId());
    assertEquals("notifications_settings", activity.getObjectName());
    assertEquals(EventObject.NOTIFICATION_RULE, activity.getObjectType());
    assertEquals(10L, activity.getProjectId());
    assertEquals(77L, activity.getOrganizationId());
    assertEquals(5L, activity.getSubjectId());
    assertEquals("user", activity.getSubjectName());
    assertEquals(EventSubject.USER, activity.getSubjectType());
  }
}


