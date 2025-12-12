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

package com.epam.reportportal.core.events.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.epam.reportportal.core.events.activity.converter.LogTypeCreatedEventConverter;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventSubject;
import com.epam.reportportal.model.activity.LogTypeActivityResource;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link LogTypeCreatedEvent}.
 *
 */
class LogTypeCreatedEventTest {

  @Test
  void toActivityWhenLogTypeProvidedShouldBuildActivityWithExpectedFields() {
    // given
    var resource = new LogTypeActivityResource();
    resource.setId(11L);
    resource.setProjectId(22L);
    resource.setName("ERROR");
    resource.setLevel(40000);
    resource.setLabelColor("#ff0000");
    resource.setBackgroundColor("#ffffff");
    resource.setTextColor("#000000");
    resource.setTextStyle("normal");
    resource.setIsFilterable(true);

    var event = new LogTypeCreatedEvent(resource, 100L, "user");

    // when
    LogTypeCreatedEventConverter converter = new LogTypeCreatedEventConverter();
    Activity activity = converter.convert(event);

    // then
    assertNotNull(activity);
    assertEquals(EventAction.CREATE, activity.getAction());
    assertEquals("createLogType", activity.getEventName());
    assertEquals(EventPriority.LOW, activity.getPriority());
    assertEquals(11L, activity.getObjectId());
    assertEquals("ERROR", activity.getObjectName());
    assertEquals(22L, activity.getProjectId());
    assertEquals(100L, activity.getSubjectId());
    assertEquals("user", activity.getSubjectName());
    assertEquals(EventSubject.USER, activity.getSubjectType());
  }
}
