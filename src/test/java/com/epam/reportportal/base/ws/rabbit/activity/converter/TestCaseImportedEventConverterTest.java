package com.epam.reportportal.base.ws.rabbit.activity.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.epam.reportportal.base.core.events.domain.tms.TestCaseImportedEvent;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventSubject;
import com.epam.reportportal.base.model.activity.TestCaseActivityResource;
import org.junit.jupiter.api.Test;

class TestCaseImportedEventConverterTest {

  private final TestCaseImportedEventConverter converter = new TestCaseImportedEventConverter();

  @Test
  void convert_WithUserSubject_ShouldCreateActivityWithUserType() {
    var resource = TestCaseActivityResource.builder()
        .id(100L)
        .name("Imported Case")
        .projectId(5L)
        .build();

    var event = new TestCaseImportedEvent(resource, 1L, "john_doe", 10L);

    Activity activity = converter.convert(event);

    assertNotNull(activity);
    assertEquals(EventAction.CREATE, activity.getAction());
    assertEquals(ActivityAction.IMPORT_TEST_CASE.getValue(), activity.getEventName());
    assertEquals(EventPriority.LOW, activity.getPriority());
    assertEquals(EventObject.TMS_TEST_CASE, activity.getObjectType());
    assertEquals(100L, activity.getObjectId());
    assertEquals("Imported Case", activity.getObjectName());
    assertEquals(5L, activity.getProjectId());
    assertEquals(10L, activity.getOrganizationId());
    assertEquals(1L, activity.getSubjectId());
    assertEquals("john_doe", activity.getSubjectName());
    assertEquals(EventSubject.USER, activity.getSubjectType());
    assertEquals(TestCaseImportedEvent.class, converter.getEventClass());
  }

  @Test
  void convert_WithSystemSubject_ShouldCreateActivityWithApplicationType() {
    var resource = TestCaseActivityResource.builder()
        .id(200L)
        .name("Sync Case")
        .projectId(5L)
        .build();

    var event = new TestCaseImportedEvent(resource, null, null, 10L);

    Activity activity = converter.convert(event);

    assertNotNull(activity);
    assertEquals(EventAction.CREATE, activity.getAction());
    assertEquals(ActivityAction.IMPORT_TEST_CASE.getValue(), activity.getEventName());
    assertEquals(EventPriority.LOW, activity.getPriority());
    assertEquals(EventObject.TMS_TEST_CASE, activity.getObjectType());
    assertEquals(200L, activity.getObjectId());
    assertEquals("Sync Case", activity.getObjectName());
    assertEquals(5L, activity.getProjectId());
    assertEquals(10L, activity.getOrganizationId());
    assertEquals(null, activity.getSubjectId());
    assertEquals("System", activity.getSubjectName());
    assertEquals(EventSubject.APPLICATION, activity.getSubjectType());
  }
}

