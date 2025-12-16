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

package com.epam.reportportal.ws.rabbit.activity.converter;

import com.epam.reportportal.core.events.domain.PluginDeletedEvent;
import com.epam.reportportal.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventSubject;
import org.springframework.stereotype.Component;

/**
 * Converter for PluginDeletedEvent to Activity. System events (plugin unload on shutdown/reload)
 * are not persisted to activity log.
 */
@Component
public class PluginDeletedEventConverter implements EventToActivityConverter<PluginDeletedEvent> {

  @Override
  public Activity convert(PluginDeletedEvent event) {
    ActivityBuilder builder = new ActivityBuilder()
        .addCreatedAt(event.getOccurredAt())
        .addAction(EventAction.DELETE)
        .addEventName(ActivityAction.DELETE_PLUGIN.getValue())
        .addObjectId(event.getBefore().getId())
        .addObjectName(event.getBefore().getName())
        .addObjectType(EventObject.PLUGIN)
        .addPriority(EventPriority.CRITICAL);

    if (event.isSystemEvent()) {
      builder.notSavedEvent();
    } else {
      builder.addSubjectId(event.getUserId());
      builder.addSubjectName(event.getUserLogin());
      builder.addSubjectType(EventSubject.USER);
    }

    return builder.get();
  }

  @Override
  public Class<PluginDeletedEvent> getEventClass() {
    return PluginDeletedEvent.class;
  }
}
