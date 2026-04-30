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

import static com.epam.reportportal.base.ws.rabbit.activity.util.ActivityDetailsUtil.processBoolean;

import com.epam.reportportal.base.core.events.domain.DashboardUpdatedEvent;
import com.epam.reportportal.base.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventSubject;
import org.springframework.stereotype.Component;

/**
 * Converter for DashboardUpdatedEvent to Activity.
 */
@Component
public class DashboardUpdatedStateEventConverter implements
    EventToActivityConverter<DashboardUpdatedEvent> {

  @Override
  public Activity convert(DashboardUpdatedEvent event) {
    return new ActivityBuilder()
        .addCreatedNow()
        .addAction(event.getAfter().isLocked() ? EventAction.LOCK : EventAction.UNLOCK)
        .addPriority(EventPriority.MEDIUM)
        .addEventName(ActivityAction.UPDATE_DASHBOARD_STATE.getValue())
        .addObjectType(EventObject.DASHBOARD)
        .addObjectId(event.getBefore().getId())
        .addObjectName(event.getBefore().getName())
        .addProjectId(event.getBefore().getProjectId())
        .addSubjectId(event.getUserId())
        .addSubjectName(event.getUserLogin())
        .addSubjectType(EventSubject.USER)
        .addHistoryField(processBoolean("locked", event.getBefore().isLocked(), event.getAfter().isLocked()))
        .get();
  }

  @Override
  public Class<DashboardUpdatedEvent> getEventClass() {
    return DashboardUpdatedEvent.class;
  }
}
