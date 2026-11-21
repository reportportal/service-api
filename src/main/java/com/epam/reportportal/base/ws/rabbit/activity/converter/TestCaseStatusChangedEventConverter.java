package com.epam.reportportal.base.ws.rabbit.activity.converter;

import com.epam.reportportal.base.core.events.domain.tms.TestCaseStatusChangedEvent;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.base.infrastructure.persistence.builder.ActivityBuilder;
import org.springframework.stereotype.Component;

/**
 * Turns a status change of a test case into an activity row: {@code status} old to new, and the reason as a second
 * history field, with the user, the rule (Auto-Ready) or the application (agent, pipeline) as the subject.
 * <p>
 * Example: {@code status: Draft -> Ready} and {@code reason: -> "Auto-Ready (93 >= 90)"} by the rule "Auto-Ready".
 */
@Component
public class TestCaseStatusChangedEventConverter implements EventToActivityConverter<TestCaseStatusChangedEvent> {

  @Override
  public Activity convert(TestCaseStatusChangedEvent event) {
    var builder = new ActivityBuilder()
        .addCreatedAt(event.getOccurredAt())
        .addAction(EventAction.UPDATE)
        .addEventName(ActivityAction.UPDATE_TEST_CASE_STATUS.getValue())
        .addPriority(EventPriority.LOW)
        .addObjectId(event.getAfter().getId())
        .addObjectName(event.getAfter().getName())
        .addObjectType(EventObject.TMS_TEST_CASE)
        .addProjectId(event.getAfter().getProjectId())
        .addSubjectId(event.getSubjectId())
        .addSubjectName(event.getSubjectName())
        .addSubjectType(event.getSubjectType())
        .addOrganizationId(event.getOrganizationId())
        .addHistoryField("status", event.getOldStatus(), event.getNewStatus());
    if (event.getReason() != null) {
      builder.addHistoryField("reason", null, event.getReason());
    }
    return builder.get();
  }

  @Override
  public Class<TestCaseStatusChangedEvent> getEventClass() {
    return TestCaseStatusChangedEvent.class;
  }
}
