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

package com.epam.reportportal.core.events.activity.converter;

import static com.epam.reportportal.core.events.activity.util.ActivityDetailsUtil.ENABLED;
import static com.epam.reportportal.core.events.activity.util.ActivityDetailsUtil.processBoolean;
import static com.epam.reportportal.core.events.activity.util.ActivityDetailsUtil.processName;

import com.epam.reportportal.core.events.domain.PluginUpdatedEvent;
import com.epam.reportportal.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventSubject;
import org.springframework.stereotype.Component;

/**
 * Converter for PluginUpdatedEvent to Activity.
 */
@Component
public class PluginUpdatedEventConverter implements EventToActivityConverter<PluginUpdatedEvent> {

  @Override
  public Activity convert(PluginUpdatedEvent event) {
    return new ActivityBuilder()
        .addCreatedNow()
        .addAction(EventAction.UPDATE)
        .addEventName(ActivityAction.UPDATE_PLUGIN.getValue())
        .addPriority(EventPriority.MEDIUM)
        .addObjectId(event.getAfter().getId())
        .addObjectName(event.getAfter().getName())
        .addObjectType(EventObject.PLUGIN)
        .addSubjectId(event.getUserId())
        .addSubjectName(event.getUserLogin())
        .addSubjectType(EventSubject.USER)
        .addHistoryField(processName(event.getBefore().getName(), event.getAfter().getName()))
        .addHistoryField(
            processBoolean(ENABLED, event.getBefore().isEnabled(), event.getAfter().isEnabled()))
        .get();
  }

  @Override
  public Class<PluginUpdatedEvent> getEventClass() {
    return PluginUpdatedEvent.class;
  }
}
