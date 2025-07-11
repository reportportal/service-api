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

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.processParameter;
import static com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum.ALL_MESSAGES_SHOULD_MATCH;
import static com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum.AUTO_ANALYZER_ENABLED;
import static com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum.AUTO_ANALYZER_MODE;
import static com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum.AUTO_UNIQUE_ERROR_ANALYZER_ENABLED;
import static com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum.MIN_SHOULD_MATCH;
import static com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum.NUMBER_OF_LOG_LINES;
import static com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum.SEARCH_LOGS_MIN_SHOULD_MATCH;
import static com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum.UNIQUE_ERROR_ANALYZER_REMOVE_NUMBERS;

import com.epam.ta.reportportal.builder.ActivityBuilder;
import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityAction;
import com.epam.ta.reportportal.entity.activity.EventAction;
import com.epam.ta.reportportal.entity.activity.EventObject;
import com.epam.ta.reportportal.entity.activity.EventPriority;
import com.epam.ta.reportportal.entity.activity.EventSubject;
import com.epam.ta.reportportal.model.activity.ProjectAttributesActivityResource;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Pavel Bortnik
 */
public class ProjectAnalyzerConfigEvent extends AroundEvent<ProjectAttributesActivityResource>
    implements ActivityEvent {
  private Long orgId;

  public ProjectAnalyzerConfigEvent() {
  }

  public ProjectAnalyzerConfigEvent(ProjectAttributesActivityResource before,
      ProjectAttributesActivityResource after, Long userId, String userLogin, Long orgId) {
    super(userId, userLogin, before, after);
    this.orgId = orgId;
  }

  @Override
  public Activity toActivity() {
    final ProjectAttributesActivityResource before = getBefore();
    final ProjectAttributesActivityResource after = getAfter();

    final Map<String, String> oldConfig = before.getConfig();
    final Map<String, String> newConfig = after.getConfig();

    final ActivityBuilder activityBuilder =
        new ActivityBuilder()
            .addCreatedNow()
            .addAction(EventAction.UPDATE)
            .addEventName(ActivityAction.UPDATE_ANALYZER.getValue())
            .addPriority(EventPriority.LOW)
            .addObjectId(before.getProjectId())
            .addObjectName("analyzer")
            .addObjectType(EventObject.PROJECT)
            .addProjectId(before.getProjectId())
            .addOrganizationId(orgId)
            .addSubjectId(getUserId())
            .addSubjectName(getUserLogin())
            .addSubjectType(EventSubject.USER);

    Stream.of(AUTO_ANALYZER_MODE, MIN_SHOULD_MATCH, SEARCH_LOGS_MIN_SHOULD_MATCH,
            NUMBER_OF_LOG_LINES, AUTO_ANALYZER_ENABLED, AUTO_UNIQUE_ERROR_ANALYZER_ENABLED,
            UNIQUE_ERROR_ANALYZER_REMOVE_NUMBERS, ALL_MESSAGES_SHOULD_MATCH
        ).map(type -> processParameter(oldConfig, newConfig, type.getAttribute()))
        .forEach(activityBuilder::addHistoryField);

    return activityBuilder.get();
  }
}
