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

package com.epam.ta.reportportal.core.events.activity;

import static com.epam.ta.reportportal.core.events.activity.ActivityTestHelper.checkActivity;

import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityDetails;
import com.epam.ta.reportportal.entity.activity.EventAction;
import com.epam.ta.reportportal.entity.activity.EventObject;
import com.epam.ta.reportportal.entity.activity.EventPriority;
import com.epam.ta.reportportal.entity.activity.EventSubject;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link OrganizationCreatedEvent}
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 **/
class OrganizationCreatedEventTest {

  private static Activity getExpectedActivity() {
    Activity activity = new Activity();
    activity.setAction(EventAction.CREATE);
    activity.setEventName("createOrganization");
    activity.setPriority(EventPriority.MEDIUM);
    activity.setObjectType(EventObject.ORGANIZATION);
    activity.setSubjectId(1L);
    activity.setSubjectName("user");
    activity.setSubjectType(EventSubject.USER);
    activity.setObjectId(2L);
    activity.setOrganizationId(2L);
    activity.setCreatedAt(Instant.now());
    activity.setObjectName("Test Organization");
    activity.setDetails(new ActivityDetails());
    return activity;
  }

  @Test
  void toActivity() {
    final Activity actual = new OrganizationCreatedEvent(1L, "user", 2L, "Test Organization").toActivity();
    final Activity expected = getExpectedActivity();
    checkActivity(expected, actual);
  }
}