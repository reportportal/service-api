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

package com.epam.reportportal.base.core.events.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.epam.reportportal.base.ws.rabbit.activity.converter.LogTypeDeletedEventConverter;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventSubject;
import com.epam.reportportal.base.model.activity.LogTypeActivityResource;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link LogTypeDeletedEvent}.
 *
 */
class LogTypeDeletedEventTest {

  @Test
  void toActivityWhenLogTypeProvidedShouldBuildActivityWithExpectedFields() {
    // given
    var resource = new LogTypeActivityResource();
    resource.setId(11L);
    resource.setProjectId(22L);
    resource.setName("CUSTOM_ERROR");
    resource.setLevel(45000);
    resource.setLabelColor("#ff0000");
    resource.setBackgroundColor("#ffffff");
    resource.setTextColor("#000000");
    resource.setTextStyle("bold");
    resource.setIsFilterable(false);

    var event = new LogTypeDeletedEvent(resource, 100L, "user");

    // when
    LogTypeDeletedEventConverter converter = new LogTypeDeletedEventConverter();
    var activity = converter.convert(event);

    // then
    assertNotNull(activity);
    assertEquals(EventAction.DELETE, activity.getAction());
    assertEquals("deleteLogType", activity.getEventName());
    assertEquals(EventPriority.MEDIUM, activity.getPriority());
    assertEquals(11L, activity.getObjectId());
    assertEquals("CUSTOM_ERROR", activity.getObjectName());
    assertEquals(22L, activity.getProjectId());
    assertEquals(100L, activity.getSubjectId());
    assertEquals("user", activity.getSubjectName());
    assertEquals(EventSubject.USER, activity.getSubjectType());
  }
}
