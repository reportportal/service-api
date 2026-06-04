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

package com.epam.reportportal.base.core.tms.mapper.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.core.events.domain.tms.TestCaseFieldChangedEvent;
import com.epam.reportportal.base.core.events.domain.tms.TmsTestCaseHistoryOfActionsField;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.base.model.activity.TestCaseActivityResource;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TmsTmsTestCaseFieldProcessorImplTest {

  private final TmsTmsTestCaseFieldProcessorImpl processor = new TmsTmsTestCaseFieldProcessorImpl(
      TmsTestCaseHistoryOfActionsField.DESCRIPTION,
      TestCaseActivityResource::getDescription,
      ActivityAction.CREATE_TEST_CASE_DESCRIPTION,
      ActivityAction.UPDATE_TEST_CASE_DESCRIPTION,
      ActivityAction.DELETE_TEST_CASE_DESCRIPTION
  );

  @Test
  void shouldReturnEmptyWhenValuesAreEqual() {
    var before = TestCaseActivityResource.builder().description("same").build();
    var after = TestCaseActivityResource.builder().description("same").build();

    Optional<TestCaseFieldChangedEvent> event = processor.process(before, after);

    assertTrue(event.isEmpty());
  }

  @Test
  void shouldReturnCreateEventWhenBeforeIsEmptyAndAfterIsPresent() {
    var before = TestCaseActivityResource.builder().description(null).build();
    var after = TestCaseActivityResource.builder().description("new desc").build();

    Optional<TestCaseFieldChangedEvent> eventOpt = processor.process(before, after);

    assertTrue(eventOpt.isPresent());
    var event = eventOpt.get();
    assertEquals(EventAction.CREATE, event.getAction());
    assertEquals(ActivityAction.CREATE_TEST_CASE_DESCRIPTION, event.getActivityAction());
    assertNull(event.getOldValue());
    assertEquals("new desc", event.getNewValue());
  }

  @Test
  void shouldReturnDeleteEventWhenBeforeIsPresentAndAfterIsEmpty() {
    var before = TestCaseActivityResource.builder().description("old desc").build();
    var after = TestCaseActivityResource.builder().description(null).build();

    Optional<TestCaseFieldChangedEvent> eventOpt = processor.process(before, after);

    assertTrue(eventOpt.isPresent());
    var event = eventOpt.get();
    assertEquals(EventAction.DELETE, event.getAction());
    assertEquals(ActivityAction.DELETE_TEST_CASE_DESCRIPTION, event.getActivityAction());
    assertEquals("old desc", event.getOldValue());
    assertNull(event.getNewValue());
  }

  @Test
  void shouldReturnDeleteEventWhenBeforeIsPresentAndAfterIsEmptyString() {
    var before = TestCaseActivityResource.builder().description("old desc").build();
    var after = TestCaseActivityResource.builder().description("").build();

    Optional<TestCaseFieldChangedEvent> eventOpt = processor.process(before, after);

    assertTrue(eventOpt.isPresent());
    var event = eventOpt.get();
    assertEquals(EventAction.DELETE, event.getAction());
    assertEquals(ActivityAction.DELETE_TEST_CASE_DESCRIPTION, event.getActivityAction());
    assertEquals("old desc", event.getOldValue());
    assertEquals("", event.getNewValue());
  }

  @Test
  void shouldReturnUpdateEventWhenBothArePresentButDifferent() {
    var before = TestCaseActivityResource.builder().description("old desc").build();
    var after = TestCaseActivityResource.builder().description("new desc").build();

    Optional<TestCaseFieldChangedEvent> eventOpt = processor.process(before, after);

    assertTrue(eventOpt.isPresent());
    var event = eventOpt.get();
    assertEquals(EventAction.UPDATE, event.getAction());
    assertEquals(ActivityAction.UPDATE_TEST_CASE_DESCRIPTION, event.getActivityAction());
    assertEquals("old desc", event.getOldValue());
    assertEquals("new desc", event.getNewValue());
  }

  @Test
  void shouldHandleCollectionsCorrectly() {
    var collectionProcessor = new TmsTmsTestCaseFieldProcessorImpl(
        TmsTestCaseHistoryOfActionsField.TAGS,
        TestCaseActivityResource::getTags,
        ActivityAction.CREATE_TEST_CASE_TAGS,
        ActivityAction.UPDATE_TEST_CASE_TAGS,
        ActivityAction.DELETE_TEST_CASE_TAGS
    );

    // Empty to Empty -> No Event
    var beforeEmpty = TestCaseActivityResource.builder().tags(Collections.emptyList()).build();
    var afterEmpty = TestCaseActivityResource.builder().tags(Collections.emptyList()).build();
    assertTrue(collectionProcessor.process(beforeEmpty, afterEmpty).isEmpty());

    // Null to Present -> Create Event
    var beforeNull = TestCaseActivityResource.builder().tags(null).build();
    var afterPresent = TestCaseActivityResource.builder().tags(List.of("tag1")).build();
    var createEventOpt = collectionProcessor.process(beforeNull, afterPresent);
    assertTrue(createEventOpt.isPresent());
    assertEquals(EventAction.CREATE, createEventOpt.get().getAction());

    // Present to Empty -> Delete Event
    var deleteEventOpt = collectionProcessor.process(afterPresent, beforeEmpty);
    assertTrue(deleteEventOpt.isPresent());
    assertEquals(EventAction.DELETE, deleteEventOpt.get().getAction());

    // Present to Present -> Update Event
    var afterDifferent = TestCaseActivityResource.builder().tags(List.of("tag2")).build();
    var updateEventOpt = collectionProcessor.process(afterPresent, afterDifferent);
    assertTrue(updateEventOpt.isPresent());
    assertEquals(EventAction.UPDATE, updateEventOpt.get().getAction());
  }

  @Test
  void shouldFallbackToUpdateActionIfCreateOrDeleteIsNull() {
    var fallbackProcessor = new TmsTmsTestCaseFieldProcessorImpl(
        TmsTestCaseHistoryOfActionsField.NAME,
        TestCaseActivityResource::getName,
        null,
        ActivityAction.UPDATE_TEST_CASE_NAME,
        null
    );

    var beforeNull = TestCaseActivityResource.builder().name(null).build();
    var afterPresent = TestCaseActivityResource.builder().name("name").build();

    // CREATE action is null, so it falls back to UPDATE_TEST_CASE_NAME
    var createEventOpt = fallbackProcessor.process(beforeNull, afterPresent);
    assertTrue(createEventOpt.isPresent());
    assertEquals(EventAction.CREATE, createEventOpt.get().getAction());
    assertEquals(ActivityAction.UPDATE_TEST_CASE_NAME, createEventOpt.get().getActivityAction());

    var deleteEventOpt = fallbackProcessor.process(afterPresent, beforeNull);
    assertTrue(deleteEventOpt.isPresent());
    assertEquals(EventAction.DELETE, deleteEventOpt.get().getAction());
    assertEquals(ActivityAction.UPDATE_TEST_CASE_NAME, deleteEventOpt.get().getActivityAction());
  }
}
