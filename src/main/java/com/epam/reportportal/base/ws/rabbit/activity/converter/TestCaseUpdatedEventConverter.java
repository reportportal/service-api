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

package com.epam.reportportal.base.ws.rabbit.activity.converter;

import static com.epam.reportportal.base.ws.rabbit.activity.util.ActivityDetailsUtil.processField;
import static com.epam.reportportal.base.ws.rabbit.activity.util.ActivityDetailsUtil.processString;
import static com.epam.reportportal.base.ws.rabbit.activity.util.ActivityDetailsUtil.processList;
import static com.epam.reportportal.base.ws.rabbit.activity.util.ActivityDetailsUtil.processName;

import com.epam.reportportal.base.core.events.domain.tms.TestCaseUpdatedEvent;
import com.epam.reportportal.base.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventSubject;
import org.springframework.stereotype.Component;

@Component
public class TestCaseUpdatedEventConverter implements EventToActivityConverter<TestCaseUpdatedEvent> {

  @Override
  public Activity convert(TestCaseUpdatedEvent event) {
    return new ActivityBuilder()
        .addCreatedAt(event.getOccurredAt())
        .addAction(EventAction.UPDATE)
        .addEventName(ActivityAction.UPDATE_TEST_CASE.getValue())
        .addPriority(EventPriority.LOW)
        .addObjectId(event.getAfter().getId())
        .addObjectName(event.getAfter().getName())
        .addObjectType(EventObject.TMS_TEST_CASE)
        .addProjectId(event.getAfter().getProjectId())
        .addSubjectId(event.getUserId())
        .addSubjectName(event.getUserLogin())
        .addSubjectType(EventSubject.USER)
        .addOrganizationId(event.getOrganizationId())
        .addHistoryField(processName(event.getBefore().getName(), event.getAfter().getName()))
        .addHistoryField(processString("description", event.getBefore().getDescription(), event.getAfter().getDescription()))
        .addHistoryField(processString("priority", event.getBefore().getPriority(), event.getAfter().getPriority()))
        .addHistoryField(processString("externalId", event.getBefore().getExternalId(), event.getAfter().getExternalId()))
        .addHistoryField(processField("testFolderId", event.getBefore().getTestFolderId(), event.getAfter().getTestFolderId()))
        .addHistoryField(processList("tags", event.getBefore().getTags(), event.getAfter().getTags()))
        .addHistoryField(processField("executionEstimationTime", event.getBefore().getExecutionEstimationTime(), event.getAfter().getExecutionEstimationTime()))
        .addHistoryField(processString("type", event.getBefore().getType(), event.getAfter().getType()))
        .addHistoryField(processString("instructions", event.getBefore().getInstructions(), event.getAfter().getInstructions()))
        .addHistoryField(processString("expectedResult", event.getBefore().getExpectedResult(), event.getAfter().getExpectedResult()))
        .addHistoryField(processString("preconditions", event.getBefore().getPreconditions(), event.getAfter().getPreconditions()))
        .addHistoryField(processString("steps", event.getBefore().getSteps(), event.getAfter().getSteps()))
        .addHistoryField(processString("requirements", event.getBefore().getRequirements(), event.getAfter().getRequirements()))
        .get();
  }

  @Override
  public Class<TestCaseUpdatedEvent> getEventClass() {
    return TestCaseUpdatedEvent.class;
  }
}
