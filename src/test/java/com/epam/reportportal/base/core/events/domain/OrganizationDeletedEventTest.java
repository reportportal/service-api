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

import static com.epam.reportportal.base.core.events.domain.ActivityTestHelper.checkActivity;

import com.epam.reportportal.base.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.ActivityDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventSubject;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.HistoryField;
import com.epam.reportportal.base.ws.rabbit.activity.converter.OrganizationDeletedEventConverter;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link OrganizationDeletedEvent}
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 **/
class OrganizationDeletedEventTest {

  private static final List<Long> USER_IDS = List.of(10L, 20L);

  @Test
  void toActivity() {
    OrganizationDeletedEvent event = new OrganizationDeletedEvent(
        1L, "user", 2L, "Test Organization", USER_IDS);

    OrganizationDeletedEventConverter converter = new OrganizationDeletedEventConverter();

    Activity actual = converter.convert(event);
    Activity expected = getExpectedActivity(event.getOccurredAt());

    checkActivity(expected, actual);
  }

  private static Activity getExpectedActivity(Instant createdAt) {
    Activity activity = new Activity();
    activity.setAction(EventAction.DELETE);
    activity.setEventName("deleteOrganization");
    activity.setPriority(EventPriority.CRITICAL);
    activity.setObjectType(EventObject.ORGANIZATION);
    activity.setSubjectId(1L);
    activity.setSubjectName("user");
    activity.setSubjectType(EventSubject.USER);
    activity.setObjectId(2L);
    activity.setCreatedAt(createdAt);
    activity.setObjectName("Test Organization");

    ActivityDetails details = new ActivityDetails();
    details.addHistoryField(HistoryField.of("users", "10, 20", ""));
    activity.setDetails(details);

    return activity;
  }
}
