/*
 * Copyright 2023 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.epam.reportportal.base.infrastructure.persistence.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.epam.reportportal.base.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.ActivityDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventSubject;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.HistoryField;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class ActivityBuilderTest {

  @Test
  void activityBuilderTest() {
    final EventAction action = EventAction.FINISH;
    final EventObject entity = EventObject.LAUNCH;
    final String objectName = "objectName";
    final Long projectId = 1L;
    final Long userId = 2L;
    final String username = "username";
    final Instant dateTime = LocalDateTime.of(2019, 2, 6, 18, 25)
        .toInstant(ZoneOffset.UTC);
    final Long objectId = 3L;
    Activity activity = new ActivityBuilder()
        .addAction(action)
        .addEventName("finishLaunch")
        .addObjectId(objectId)
        .addObjectName(objectName)
        .addObjectType(entity)
        .addProjectId(projectId)
        .addSubjectId(userId)
        .addSubjectName(username)
        .addSubjectType(EventSubject.USER)
        .addCreatedAt(dateTime)
        .addHistoryField("field", "before", "after")
        .get();

    assertEquals(action.getValue(), activity.getAction().getValue());
    assertEquals(entity.toString(), activity.getObjectType().toString());
    assertEquals(objectName, activity.getObjectName());
    assertEquals(projectId, activity.getProjectId());
    assertEquals(userId, activity.getSubjectId());
    assertEquals(objectId, activity.getObjectId());
    assertEquals(username, activity.getSubjectName());
    assertEquals(dateTime, activity.getCreatedAt());
  }

  @Test
  void addDetailsTest() {
    ActivityDetails details = new ActivityDetails();
    final HistoryField historyFiled = HistoryField.of("field", "before", "after");
    details.addHistoryField(historyFiled);

    final Activity activity = new ActivityBuilder()
        .addObjectName("name")
        .addDetails(details)
        .addCreatedNow()
        .get();

    assertEquals("name", activity.getObjectName());
    assertEquals(1, activity.getDetails().getHistory().size());
    assertEquals(historyFiled, activity.getDetails().getHistory().get(0));
    assertNotNull(activity.getCreatedAt());
  }
}
