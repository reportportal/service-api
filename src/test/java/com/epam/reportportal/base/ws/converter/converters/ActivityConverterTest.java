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

package com.epam.reportportal.base.ws.converter.converters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.epam.reportportal.base.infrastructure.model.ActivityResource;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.ActivityDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventSubject;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.HistoryField;
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

  @Test
  void toProjectActivityApiModelWhenResourceHasHistoryShouldMapAllFieldsToSnakeCaseModel() {
    ActivityResource resource = new ActivityResource();
    resource.setId(1L);
    resource.setUser("superadmin");
    resource.setUserId(2L);
    resource.setLoggedObjectId(3L);
    resource.setLastModified(Instant.now());
    resource.setActionType("finishLaunch");
    resource.setObjectType("LAUNCH");
    resource.setProjectId(4L);
    resource.setProjectName("default_personal");
    resource.setProjectKey("default_personal");
    resource.setObjectName("launch name");
    final com.epam.reportportal.base.infrastructure.persistence.entity.activity.ActivityDetails details =
        new com.epam.reportportal.base.infrastructure.persistence.entity.activity.ActivityDetails();
    details.setHistory(Collections.singletonList(HistoryField.of("status", "PASSED", "FAILED")));
    resource.setDetails(details);

    var apiModel = ActivityConverter.TO_PROJECT_ACTIVITY_API_MODEL.apply(resource);

    assertThat(apiModel.getId()).isEqualTo(resource.getId());
    assertThat(apiModel.getUser()).isEqualTo(resource.getUser());
    assertThat(apiModel.getUserId()).isEqualTo(resource.getUserId());
    assertThat(apiModel.getLoggedObjectId()).isEqualTo(resource.getLoggedObjectId());
    assertThat(apiModel.getLastModified()).isEqualTo(resource.getLastModified());
    assertThat(apiModel.getActionType()).isEqualTo(resource.getActionType());
    assertThat(apiModel.getObjectType()).isEqualTo(resource.getObjectType());
    assertThat(apiModel.getProjectId()).isEqualTo(resource.getProjectId());
    assertThat(apiModel.getProjectName()).isEqualTo(resource.getProjectName());
    assertThat(apiModel.getProjectKey()).isEqualTo(resource.getProjectKey());
    assertThat(apiModel.getObjectName()).isEqualTo(resource.getObjectName());
    assertThat(apiModel.getDetails().getHistory()).hasSize(1);
    assertThat(apiModel.getDetails().getHistory().get(0).getField()).isEqualTo("status");
  }

  @Test
  void toProjectActivityApiModelWhenDetailsHaveNoHistoryShouldReturnDetailsWithEmptyHistory() {
    ActivityResource resource = new ActivityResource();
    resource.setId(1L);
    resource.setDetails(new com.epam.reportportal.base.infrastructure.persistence.entity.activity.ActivityDetails());

    var apiModel = ActivityConverter.TO_PROJECT_ACTIVITY_API_MODEL.apply(resource);

    assertThat(apiModel.getDetails()).isNotNull();
    assertThat(apiModel.getDetails().getHistory()).isEmpty();
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
