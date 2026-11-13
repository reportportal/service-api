/*
 * Copyright 2026 EPAM Systems
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
import com.epam.reportportal.base.ws.rabbit.activity.converter.OrganizationUsersUpdatedEventConverter;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrganizationUsersUpdatedEventTest {

  @Test
  void toActivity() {
    OrganizationUsersUpdatedEvent event = new OrganizationUsersUpdatedEvent(
        1L, "user", 2L, "My Org", List.of(10L, 20L), List.of(20L), EventAction.UNASSIGN
    );
    OrganizationUsersUpdatedEventConverter converter = new OrganizationUsersUpdatedEventConverter();

    Activity actual = converter.convert(event);
    Activity expected = expectedActivity(event.getOccurredAt());
    checkActivity(expected, actual);
  }

  private static Activity expectedActivity(Instant createdAt) {
    Activity activity = new Activity();
    activity.setAction(EventAction.UNASSIGN);
    activity.setEventName("updateOrganizationUsers");
    activity.setPriority(EventPriority.MEDIUM);
    activity.setObjectType(EventObject.ORGANIZATION);
    activity.setSubjectId(1L);
    activity.setSubjectName("user");
    activity.setSubjectType(EventSubject.USER);
    activity.setObjectId(2L);
    activity.setCreatedAt(createdAt);
    activity.setObjectName("My Org");
    activity.setOrganizationId(2L);
    ActivityDetails details = new ActivityDetails();
    details.addHistoryField(HistoryField.of("users", "10, 20", "20"));
    activity.setDetails(details);
    return activity;
  }
}
