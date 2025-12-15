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
import static com.epam.reportportal.core.events.activity.util.ActivityDetailsUtil.NAME;
import static com.epam.reportportal.core.events.domain.ActivityTestHelper.checkActivity;

import com.epam.reportportal.core.events.activity.converter.IntegrationCreatedEventConverter;
import com.epam.reportportal.core.events.activity.converter.IntegrationDeletedEventConverter;
import com.epam.reportportal.core.events.activity.converter.IntegrationUpdatedEventConverter;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityDetails;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventSubject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.HistoryField;
import com.epam.reportportal.model.activity.IntegrationActivityResource;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class IntegrationEventsTest {

  private static Activity getExpectedActivity(EventAction action) {
    Activity activity = new Activity();
    activity.setAction(action);
    activity.setEventName(action.getValue().concat("Integration"));
    activity.setPriority(EventPriority.MEDIUM);
    activity.setObjectType(EventObject.INTEGRATION);
    activity.setSubjectId(1L);
    activity.setSubjectName("user");
    activity.setSubjectType(EventSubject.USER);
    activity.setProjectId(3L);
    activity.setObjectId(2L);
    activity.setOrganizationId(1L);
    activity.setCreatedAt(Instant.now());
    activity.setObjectName("type");
    ActivityDetails expected = new ActivityDetails();
    HistoryField historyField = new HistoryField();
    historyField.setField(NAME);
    switch (action) {
      case CREATE, UPDATE:
        historyField.setNewValue("name");
        break;
      case DELETE:
        historyField.setOldValue("name");
        break;
    }
    expected.addHistoryField(historyField);
    activity.setDetails(expected);
    return activity;
  }

  @Test
  void created() {
    IntegrationCreatedEvent event = new IntegrationCreatedEvent(getIntegration(), 1L, "user",
        1L);
    IntegrationCreatedEventConverter converter = new IntegrationCreatedEventConverter();
    final Activity actual = converter.convert(event);
    final Activity expected = getExpectedActivity(EventAction.CREATE);
    checkActivity(expected, actual);
  }

  @Test
  void deleted() {
    IntegrationDeletedEvent event = new IntegrationDeletedEvent(getIntegration(), 1L, "user",
        1L);
    IntegrationDeletedEventConverter converter = new IntegrationDeletedEventConverter();
    final Activity actual = converter.convert(event);
    final Activity expected = getExpectedActivity(EventAction.DELETE);
    checkActivity(expected, actual);
  }

  private static IntegrationActivityResource getIntegration() {
    IntegrationActivityResource integration = new IntegrationActivityResource();
    integration.setId(2L);
    integration.setName("name");
    integration.setProjectId(3L);
    integration.setTypeName("type");
    integration.setProjectName(TEST_PROJECT_KEY);
    return integration;
  }

  @Test
  void updated() {
    IntegrationUpdatedEvent event = new IntegrationUpdatedEvent(1L, "user", getIntegration(),
        getIntegration(), 1L);
    IntegrationUpdatedEventConverter converter = new IntegrationUpdatedEventConverter();
    final Activity actual = converter.convert(event);
    final Activity expected = getExpectedActivity(EventAction.UPDATE);
    checkActivity(expected, actual);
  }
}
