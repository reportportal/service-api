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
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.STATUS;

import com.epam.ta.reportportal.core.events.activity.item.TestItemStatusChangedEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityDetails;
import com.epam.ta.reportportal.entity.activity.EventAction;
import com.epam.ta.reportportal.entity.activity.EventObject;
import com.epam.ta.reportportal.entity.activity.EventPriority;
import com.epam.ta.reportportal.entity.activity.EventSubject;
import com.epam.ta.reportportal.entity.activity.HistoryField;
import com.epam.ta.reportportal.model.activity.TestItemActivityResource;
import com.google.common.collect.Lists;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class TestItemStatusChangedEventTest {

  private static Activity getExpectedActivity() {
    Activity activity = new Activity();
    activity.setAction(EventAction.UPDATE);
    activity.setEventName("updateItem");
    activity.setPriority(EventPriority.LOW);
    activity.setObjectType(EventObject.ITEM_ISSUE);
    activity.setSubjectId(1L);
    activity.setSubjectName("user");
    activity.setSubjectType(EventSubject.USER);
    activity.setProjectId(3L);
    activity.setObjectId(2L);
    activity.setCreatedAt(LocalDateTime.now());
    activity.setObjectName("name");
    activity.setDetails(new ActivityDetails());
    return activity;
  }

  private static TestItemActivityResource getTestItem(String status) {
    TestItemActivityResource testItem = new TestItemActivityResource();
    testItem.setProjectId(3L);
    testItem.setStatus(status);
    testItem.setIssueTypeLongName("Product Bug");
    testItem.setIssueDescription("Description");
    testItem.setIgnoreAnalyzer(false);
    testItem.setAutoAnalyzed(true);
    testItem.setName("name");
    testItem.setId(2L);
    testItem.setTickets("1:http:/example.com/ticket/1,2:http:/example.com/ticket/2");
    return testItem;
  }

  @Test
  void toActivity() {

    final String beforeStatus = "PASSED";
    final String afterStatus = "FAILED";
    final Activity actual =
        new TestItemStatusChangedEvent(getTestItem(beforeStatus), getTestItem(afterStatus), 1L,
            "user"
        ).toActivity();
    final Activity expected = getExpectedActivity();
    expected.getDetails().setHistory(getExpectedHistory(Pair.of(beforeStatus, afterStatus)));
    checkActivity(expected, actual);
  }

  private static List<HistoryField> getExpectedHistory(Pair<String, String> status) {
    return Lists.newArrayList(HistoryField.of(STATUS, status.getLeft(), status.getRight()));
  }
}