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

package com.epam.reportportal.base.core.events.domain;

import static com.epam.reportportal.base.ws.rabbit.activity.util.ActivityDetailsUtil.DESCRIPTION;
import static com.epam.reportportal.base.ws.rabbit.activity.util.ActivityDetailsUtil.NAME;
import static com.epam.reportportal.base.core.events.domain.ActivityTestHelper.checkActivity;

import com.epam.reportportal.base.ws.rabbit.activity.converter.FilterCreatedEventConverter;
import com.epam.reportportal.base.ws.rabbit.activity.converter.FilterDeletedEventConverter;
import com.epam.reportportal.base.ws.rabbit.activity.converter.FilterUpdatedEventConverter;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.ActivityDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventSubject;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.HistoryField;
import com.epam.reportportal.base.model.activity.UserFilterActivityResource;
import com.google.common.collect.Lists;
import java.time.Instant;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class FilterEventsTest {

  private static Activity getExpectedActivity(EventAction action, String name) {
    Activity activity = new Activity();
    activity.setAction(action);
    activity.setEventName(action.getValue().concat("Filter"));
    activity.setPriority(EventPriority.LOW);
    activity.setObjectType(EventObject.FILTER);
    activity.setSubjectId(1L);
    activity.setSubjectName("user");
    activity.setSubjectType(EventSubject.USER);
    activity.setProjectId(3L);
    activity.setObjectId(2L);
    activity.setOrganizationId(1L);
    activity.setCreatedAt(Instant.now());
    activity.setObjectName(name);
    activity.setDetails(new ActivityDetails());
    return activity;
  }

  @Test
  void created() {
    final String name = "name";
    FilterCreatedEvent event = new FilterCreatedEvent(getUserFilter(name, "description"), 1L,
        "user", 1L);
    FilterCreatedEventConverter converter = new FilterCreatedEventConverter();
    final Activity actual = converter.convert(event);
    final Activity expected = getExpectedActivity(EventAction.CREATE, name);
    checkActivity(expected, actual);
  }

  @Test
  void deleted() {
    final String name = "name";
    FilterDeletedEvent event = new FilterDeletedEvent(getUserFilter(name, "description"), 1L,
        "user", 1L);
    FilterDeletedEventConverter converter = new FilterDeletedEventConverter();
    final Activity actual = converter.convert(event);
    final Activity expected = getExpectedActivity(EventAction.DELETE, name);
    checkActivity(expected, actual);
  }

  private static UserFilterActivityResource getUserFilter(String name, String description) {
    UserFilterActivityResource userFilter = new UserFilterActivityResource();
    userFilter.setId(2L);
    userFilter.setProjectId(3L);
    userFilter.setName(name);
    userFilter.setDescription(description);
    return userFilter;
  }

  @Test
  void updated() {
    final String oldName = "oldName";
    final String oldDescription = "oldDescription";
    final String newName = "newName";
    final String newDescription = "newDescription";
    FilterUpdatedEvent event = new FilterUpdatedEvent(getUserFilter(oldName,
        oldDescription), getUserFilter(newName, newDescription), 1L, "user", 1L);
    FilterUpdatedEventConverter converter = new FilterUpdatedEventConverter();
    final Activity actual = converter.convert(event);
    final Activity expected = getExpectedActivity(EventAction.UPDATE, newName);
    expected.getDetails().setHistory(
        getExpectedHistory(Pair.of(oldName, newName),
            Pair.of(oldDescription, newDescription)
        ));
    checkActivity(expected, actual);
  }

  private static List<HistoryField> getExpectedHistory(Pair<String, String> name,
      Pair<String, String> description) {
    return Lists.newArrayList(HistoryField.of(NAME, name.getLeft(), name.getRight()),
        HistoryField.of(DESCRIPTION, description.getLeft(), description.getRight())
    );
  }
}
