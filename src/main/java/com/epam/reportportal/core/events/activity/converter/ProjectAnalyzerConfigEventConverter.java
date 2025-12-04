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

import static com.epam.reportportal.core.events.activity.util.ActivityDetailsUtil.processParameter;
import static com.epam.reportportal.infrastructure.persistence.entity.enums.ProjectAttributeEnum.ALL_MESSAGES_SHOULD_MATCH;
import static com.epam.reportportal.infrastructure.persistence.entity.enums.ProjectAttributeEnum.AUTO_ANALYZER_ENABLED;
import static com.epam.reportportal.infrastructure.persistence.entity.enums.ProjectAttributeEnum.AUTO_ANALYZER_MODE;
import static com.epam.reportportal.infrastructure.persistence.entity.enums.ProjectAttributeEnum.AUTO_UNIQUE_ERROR_ANALYZER_ENABLED;
import static com.epam.reportportal.infrastructure.persistence.entity.enums.ProjectAttributeEnum.LARGEST_RETRY_PRIORITY;
import static com.epam.reportportal.infrastructure.persistence.entity.enums.ProjectAttributeEnum.MIN_SHOULD_MATCH;
import static com.epam.reportportal.infrastructure.persistence.entity.enums.ProjectAttributeEnum.NUMBER_OF_LOG_LINES;
import static com.epam.reportportal.infrastructure.persistence.entity.enums.ProjectAttributeEnum.SEARCH_LOGS_MIN_SHOULD_MATCH;
import static com.epam.reportportal.infrastructure.persistence.entity.enums.ProjectAttributeEnum.UNIQUE_ERROR_ANALYZER_REMOVE_NUMBERS;

import com.epam.reportportal.core.events.domain.ProjectAnalyzerConfigEvent;
import com.epam.reportportal.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventSubject;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

/**
 * Converter for ProjectAnalyzerConfigEvent to Activity.
 *
 */
@Component
public class ProjectAnalyzerConfigEventConverter implements
    EventToActivityConverter<ProjectAnalyzerConfigEvent> {

  @Override
  public Activity convert(ProjectAnalyzerConfigEvent event) {
    final Map<String, String> oldConfig = event.getBefore().getConfig();
    final Map<String, String> newConfig = event.getAfter().getConfig();

    final ActivityBuilder activityBuilder = new ActivityBuilder()
        .addCreatedNow()
        .addAction(EventAction.UPDATE)
        .addEventName(ActivityAction.UPDATE_ANALYZER.getValue())
        .addPriority(EventPriority.LOW)
        .addObjectId(event.getBefore().getProjectId())
        .addObjectName("analyzer")
        .addObjectType(EventObject.PROJECT)
        .addProjectId(event.getBefore().getProjectId())
        .addOrganizationId(event.getOrganizationId())
        .addSubjectId(event.getUserId())
        .addSubjectName(event.getUserLogin())
        .addSubjectType(EventSubject.USER);

    Stream.of(AUTO_ANALYZER_MODE, MIN_SHOULD_MATCH, SEARCH_LOGS_MIN_SHOULD_MATCH,
            NUMBER_OF_LOG_LINES, AUTO_ANALYZER_ENABLED, AUTO_UNIQUE_ERROR_ANALYZER_ENABLED,
            UNIQUE_ERROR_ANALYZER_REMOVE_NUMBERS, ALL_MESSAGES_SHOULD_MATCH, LARGEST_RETRY_PRIORITY
        ).map(type -> processParameter(oldConfig, newConfig, type.getAttribute()))
        .forEach(activityBuilder::addHistoryField);

    return activityBuilder.get();
  }

  @Override
  public Class<ProjectAnalyzerConfigEvent> getEventClass() {
    return ProjectAnalyzerConfigEvent.class;
  }
}

