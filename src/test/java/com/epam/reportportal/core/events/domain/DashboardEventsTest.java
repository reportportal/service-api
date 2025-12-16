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

import static com.epam.reportportal.ws.rabbit.activity.util.ActivityDetailsUtil.DESCRIPTION;
import static com.epam.reportportal.ws.rabbit.activity.util.ActivityDetailsUtil.NAME;
import static com.epam.reportportal.core.events.domain.ActivityTestHelper.checkActivity;

import com.epam.reportportal.ws.rabbit.activity.converter.DashboardCreatedEventConverter;
import com.epam.reportportal.ws.rabbit.activity.converter.DashboardDeletedEventConverter;
import com.epam.reportportal.ws.rabbit.activity.converter.DashboardUpdatedEventConverter;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityDetails;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventSubject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.HistoryField;
import com.epam.reportportal.model.activity.DashboardActivityResource;
import com.google.common.collect.Lists;
import java.time.Instant;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class DashboardEventsTest {

  private static Activity getExpectedDashboardActivity(EventAction action, String name) {
    Activity activity = new Activity();
    activity.setAction(action);
    activity.setEventName(action.getValue().concat("Dashboard"));
    activity.setPriority(EventPriority.LOW);
    activity.setObjectType(EventObject.DASHBOARD);
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

    DashboardCreatedEvent event = new DashboardCreatedEvent(getTestDashboard(name,
        "description"), 1L, "user", 1L);
    DashboardCreatedEventConverter converter = new DashboardCreatedEventConverter();
    final Activity actual = converter.convert(event);
    final Activity expected = getExpectedDashboardActivity(EventAction.CREATE, name);
    checkActivity(expected, actual);
  }

  @Test
  void deleted() {
    final String name = "name";

    DashboardDeletedEvent event = new DashboardDeletedEvent(getTestDashboard(name,
        "description"), 1L, "user", 1L);
    DashboardDeletedEventConverter converter = new DashboardDeletedEventConverter();
    final Activity actual = converter.convert(event);
    final Activity expected = getExpectedDashboardActivity(EventAction.DELETE, name);
    checkActivity(actual, expected);
  }

  private static DashboardActivityResource getTestDashboard(String name, String description) {
    DashboardActivityResource dashboard = new DashboardActivityResource();
    dashboard.setDescription(description);
    dashboard.setProjectId(3L);
    dashboard.setName(name);
    dashboard.setId(2L);
    return dashboard;
  }

  @Test
  void updated() {
    final String oldName = "oldName";
    final String oldDescription = "oldDescription";
    final String newName = "newName";
    final String newDescription = "newDescription";

    DashboardUpdatedEvent event = new DashboardUpdatedEvent(
        getTestDashboard(oldName, oldDescription),
        getTestDashboard(newName, newDescription),
        1L,
        "user", 1L);
    DashboardUpdatedEventConverter converter = new DashboardUpdatedEventConverter();
    final Activity actual = converter.convert(event);
    final Activity expected = getExpectedDashboardActivity(EventAction.UPDATE,
        newName);
    expected.getDetails()
        .setHistory(getExpectedHistory(Pair.of(oldName, newName),
            Pair.of(oldDescription, newDescription)
        ));
    checkActivity(actual, expected);
  }

  private static List<HistoryField> getExpectedHistory(Pair<String, String> name,
      Pair<String, String> description) {
    return Lists.newArrayList(HistoryField.of(NAME, name.getLeft(), name.getRight()),
        HistoryField.of(DESCRIPTION, description.getLeft(), description.getRight())
    );
  }

}
