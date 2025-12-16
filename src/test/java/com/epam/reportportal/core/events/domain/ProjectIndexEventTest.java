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

package com.epam.reportportal.core.events.domain;

import static com.epam.reportportal.OrganizationUtil.TEST_PROJECT_KEY;
import static com.epam.reportportal.core.events.domain.ActivityTestHelper.checkActivity;

import com.epam.reportportal.ws.rabbit.activity.converter.ProjectIndexEventConverter;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityDetails;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventSubject;
import java.time.Instant;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class ProjectIndexEventTest {

  private static Activity getExpectedActivity(EventAction action, boolean indexing) {
    Activity activity = new Activity();
    activity.setAction(action);
    activity.setEventName(action.getValue().concat("Index"));
    activity.setPriority(indexing ? EventPriority.LOW : EventPriority.MEDIUM);
    activity.setObjectType(EventObject.INDEX);
    activity.setSubjectId(1L);
    activity.setSubjectName("user");
    activity.setSubjectType(EventSubject.USER);
    activity.setProjectId(3L);
    activity.setObjectId(3L);
    activity.setOrganizationId(1L);
    activity.setCreatedAt(Instant.now());
    activity.setObjectName(StringUtils.EMPTY);
    activity.setDetails(new ActivityDetails());
    return activity;
  }

  @Test
  void generate() {
    final boolean indexing = true;
    ProjectIndexEvent event = new ProjectIndexEvent(1L, "user", 3L, TEST_PROJECT_KEY,
        indexing, 1L);
    ProjectIndexEventConverter converter = new ProjectIndexEventConverter();
    final Activity actual = converter.convert(event);
    final Activity expected = getExpectedActivity(EventAction.GENERATE, indexing);
    checkActivity(expected, actual);
  }

  @Test
  void delete() {
    final boolean indexing = false;
    ProjectIndexEvent event = new ProjectIndexEvent(1L, "user", 3L, TEST_PROJECT_KEY,
        indexing, 1L);
    ProjectIndexEventConverter converter = new ProjectIndexEventConverter();
    final Activity actual = converter.convert(event);
    final Activity expected = getExpectedActivity(EventAction.DELETE, indexing);
    checkActivity(expected, actual);
  }
}
