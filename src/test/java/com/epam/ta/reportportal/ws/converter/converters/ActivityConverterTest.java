/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.ws.converter.converters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityDetails;
import com.epam.ta.reportportal.entity.activity.EventAction;
import com.epam.ta.reportportal.entity.activity.EventObject;
import com.epam.ta.reportportal.entity.activity.EventPriority;
import com.epam.ta.reportportal.entity.activity.EventSubject;
import com.epam.ta.reportportal.entity.activity.HistoryField;
import com.epam.ta.reportportal.ws.model.ActivityResource;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class ActivityConverterTest {

  @Test
  void testNull() {
    assertThrows(NullPointerException.class, () -> ActivityConverter.TO_RESOURCE.apply(null));
  }

  @Test
  void testConvert() {
    Activity activity = new Activity();
    activity.setId(1L);
    activity.setPriority(EventPriority.MEDIUM);
    activity.setEventName("startLaunch");
    activity.setAction(EventAction.START);
    activity.setObjectType(EventObject.LAUNCH);
    activity.setCreatedAt(Instant.now());
    activity.setObjectName("objectName");
    final ActivityDetails details = new ActivityDetails();
    details.setHistory(Collections.singletonList(HistoryField.of("filed", "old", "new")));
    activity.setDetails(details);
    activity.setSubjectName("username");
    activity.setProjectId(2L);
    activity.setSubjectId(3L);
    activity.setSubjectType(EventSubject.USER);
    validate(activity, ActivityConverter.TO_RESOURCE.apply(activity));
  }

  @Test
  void toResourceWithUser() {
    Activity activity = new Activity();
    activity.setId(1L);
    activity.setAction(EventAction.START);
    activity.setObjectType(EventObject.LAUNCH);
    activity.setPriority(EventPriority.MEDIUM);
    activity.setCreatedAt(Instant.now());
    activity.setObjectName("objectName");
    final ActivityDetails details = new ActivityDetails();
    details.setHistory(Collections.singletonList(HistoryField.of("filed", "old", "new")));
    activity.setDetails(details);
    activity.setProjectId(2L);
    activity.setSubjectId(3L);
    activity.setSubjectType(EventSubject.USER);
    final ActivityResource resource = ActivityConverter.TO_RESOURCE_WITH_USER.apply(activity,
        "username");
    assertEquals("username", resource.getUser());
  }

  private void validate(Activity db, ActivityResource resource) {
    assertEquals(Instant.now().truncatedTo(ChronoUnit.SECONDS),
        resource.getLastModified().truncatedTo(ChronoUnit.SECONDS));
    assertEquals(db.getId(), resource.getId());
    assertEquals(db.getObjectType(),
        EventObject.valueOf(resource.getObjectType()));
    assertEquals(db.getSubjectName(), resource.getUser());
    assertEquals(db.getProjectId(), resource.getProjectId());
    assertEquals(db.getEventName(), resource.getActionType());
  }
}
