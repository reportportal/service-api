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

package com.epam.ta.reportportal.core.events.activity;

import static com.epam.ta.reportportal.core.events.activity.ActivityTestHelper.checkActivity;

import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityDetails;
import com.epam.ta.reportportal.entity.activity.EventAction;
import com.epam.ta.reportportal.entity.activity.EventObject;
import com.epam.ta.reportportal.entity.activity.EventPriority;
import com.epam.ta.reportportal.entity.activity.EventSubject;
import com.epam.ta.reportportal.ws.model.activity.UserActivityResource;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class UserCreatedEventTest {

  private static Activity getExpectedActivity() {
    Activity activity = new Activity();
    activity.setAction(EventAction.CREATE);
    activity.setEventName("createUser");
    activity.setPriority(EventPriority.HIGH);
    activity.setObjectType(EventObject.USER);
    activity.setSubjectId(1L);
    activity.setSubjectName("user");
    activity.setSubjectType(EventSubject.USER);
    activity.setObjectId(2L);
    activity.setCreatedAt(LocalDateTime.now());
    activity.setObjectName("Jaja Juja");
    activity.setDetails(new ActivityDetails());
    return activity;
  }

  private static UserActivityResource getUser() {
    UserActivityResource user = new UserActivityResource();
    user.setId(2L);
    user.setFullName("Jaja Juja");
    user.setDefaultProjectId(3L);
    return user;
  }

  @Test
  void toActivity() {
    final Activity actual = new UserCreatedEvent(getUser(), 1L, "user").toActivity();
    final Activity expected = getExpectedActivity();
    checkActivity(expected, actual);

  }
}