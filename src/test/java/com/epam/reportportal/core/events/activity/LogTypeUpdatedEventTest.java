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

package com.epam.reportportal.core.events.activity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventSubject;
import com.epam.reportportal.model.activity.LogTypeActivityResource;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link LogTypeUpdatedEvent}.
 *
 */
class LogTypeUpdatedEventTest {

  @Test
  void toActivityWhenLogTypeProvidedShouldBuildActivityWithExpectedFields() {
    // given
    var before = buildActivityResource("#00ff00", "bold", true);
    var after = buildActivityResource("#ff0000", "normal", false);
    LogTypeUpdatedEvent event = new LogTypeUpdatedEvent(before, after, 100L, "user");

    // when
    var activity = event.toActivity();

    // then
    assertNotNull(activity);
    assertEquals(EventAction.UPDATE, activity.getAction());
    assertEquals("updateLogType", activity.getEventName());
    assertEquals(EventPriority.LOW, activity.getPriority());
    assertEquals(11L, activity.getObjectId());
    assertEquals("ERROR", activity.getObjectName());
    assertEquals(22L, activity.getProjectId());
    assertEquals(100L, activity.getSubjectId());
    assertEquals("user", activity.getSubjectName());
    assertEquals(EventSubject.USER, activity.getSubjectType());

    assertNotNull(activity.getDetails());
    assertEquals(3, activity.getDetails().getHistory().size());

    var history = activity.getDetails().getHistory();

    var labelColor = history.stream()
        .filter(h -> "labelColor".equals(h.getField()))
        .findFirst();
    assertTrue(labelColor.isPresent());
    assertEquals("#00ff00", labelColor.get().getOldValue());
    assertEquals("#ff0000", labelColor.get().getNewValue());

    var isFilterable = history.stream()
        .filter(h -> "isFilterable".equals(h.getField()))
        .findFirst();
    assertTrue(isFilterable.isPresent());
    assertEquals("true", isFilterable.get().getOldValue());
    assertEquals("false", isFilterable.get().getNewValue());

    var textStyle = history.stream()
        .filter(h -> "textStyle".equals(h.getField()))
        .findFirst();
    assertTrue(textStyle.isPresent());
    assertEquals("bold", textStyle.get().getOldValue());
    assertEquals("normal", textStyle.get().getNewValue());
  }

  private static LogTypeActivityResource buildActivityResource(String labelColor, String textStyle,
      boolean isFilterable) {
    var after = new LogTypeActivityResource();
    after.setId(11L);
    after.setProjectId(22L);
    after.setName("ERROR");
    after.setLevel(40000);
    after.setLabelColor(labelColor);
    after.setBackgroundColor("#000000");
    after.setTextColor("#ffffff");
    after.setTextStyle(textStyle);
    after.setIsFilterable(isFilterable);
    return after;
  }
}
