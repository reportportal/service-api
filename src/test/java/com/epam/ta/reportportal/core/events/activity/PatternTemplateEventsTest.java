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
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.ENABLED;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.NAME;

import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityDetails;
import com.epam.ta.reportportal.entity.activity.EventAction;
import com.epam.ta.reportportal.entity.activity.EventObject;
import com.epam.ta.reportportal.entity.activity.EventPriority;
import com.epam.ta.reportportal.entity.activity.EventSubject;
import com.epam.ta.reportportal.entity.activity.HistoryField;
import com.epam.ta.reportportal.model.activity.PatternTemplateActivityResource;
import com.google.common.collect.Lists;
import java.time.Instant;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class PatternTemplateEventsTest {

  private static Activity getExpectedPatternTemplateActivity(EventAction action, String name) {
    Activity activity = new Activity();
    activity.setAction(action);
    activity.setEventName(action.getValue().concat("Pattern"));
    activity.setPriority(EventPriority.LOW);
    activity.setObjectType(EventObject.PATTERN);
    activity.setSubjectId(1L);
    activity.setSubjectName("user");
    activity.setSubjectType(EventSubject.USER);
    activity.setProjectId(3L);
    activity.setOrganizationId(1L);
    activity.setObjectId(2L);
    activity.setCreatedAt(Instant.now());
    activity.setObjectName(name);
    activity.setDetails(new ActivityDetails());
    return activity;
  }

  private static PatternTemplateActivityResource getTestPatternTemplate(String name,
      boolean enabled) {
    PatternTemplateActivityResource resource = new PatternTemplateActivityResource();
    resource.setEnabled(enabled);
    resource.setProjectId(3L);
    resource.setName(name);
    resource.setId(2L);
    return resource;
  }

  private static List<HistoryField> getExpectedHistory(Pair<String, String> name,
      Pair<Boolean, Boolean> enabled) {
    return Lists.newArrayList(
        HistoryField.of(NAME, name.getLeft(), name.getRight()),
        HistoryField.of(ENABLED, enabled.getLeft().toString(), enabled.getRight().toString())
    );
  }

  @Test
  void created() {
    final String name = "name";

    final Activity actual = new PatternCreatedEvent(1L, "user",
        getTestPatternTemplate(name, false), 1L).toActivity();
    final Activity expected = getExpectedPatternTemplateActivity(EventAction.CREATE, name);
    checkActivity(actual, expected);
  }

  @Test
  void deleted() {
    final String name = "name";

    final Activity actual = new PatternDeletedEvent(1L, "user",
        getTestPatternTemplate(name, false), 1L).toActivity();
    final Activity expected = getExpectedPatternTemplateActivity(EventAction.DELETE, name);
    checkActivity(actual, expected);
  }

  @Test
  void updated() {
    final String oldName = "oldName";
    final boolean oldEnabled = true;
    final String newName = "newName";
    final boolean newEnabled = false;

    final Activity actual = new PatternUpdatedEvent(1L,
        "user",
        getTestPatternTemplate(oldName, oldEnabled),
        getTestPatternTemplate(newName, newEnabled), 1L
    ).toActivity();
    final Activity expected = getExpectedPatternTemplateActivity(EventAction.UPDATE, newName);
    expected.getDetails()
        .setHistory(getExpectedHistory(Pair.of(oldName, newName), Pair.of(oldEnabled, newEnabled)));
    checkActivity(actual, expected);
  }
}
