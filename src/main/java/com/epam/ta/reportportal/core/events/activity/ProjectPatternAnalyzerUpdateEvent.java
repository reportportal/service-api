/*
 * Copyright 2023 EPAM Systems
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
import static com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum.AUTO_PATTERN_ANALYZER_ENABLED;

import com.epam.ta.reportportal.builder.ActivityBuilder;
import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityAction;
import com.epam.ta.reportportal.entity.activity.EventAction;
import com.epam.ta.reportportal.entity.activity.EventObject;
import com.epam.ta.reportportal.entity.activity.EventPriority;
import com.epam.ta.reportportal.entity.activity.EventSubject;
import com.epam.ta.reportportal.ws.model.activity.ProjectAttributesActivityResource;
import java.util.Map;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class ProjectPatternAnalyzerUpdateEvent extends
    AroundEvent<ProjectAttributesActivityResource> implements
    ActivityEvent {

  public ProjectPatternAnalyzerUpdateEvent() {
  }

  public ProjectPatternAnalyzerUpdateEvent(ProjectAttributesActivityResource before,
      ProjectAttributesActivityResource after, Long userId,
      String userLogin) {
    super(userId, userLogin, before, after);
  }

  @Override
  public Activity toActivity() {
    final ProjectAttributesActivityResource before = getBefore();
    final ProjectAttributesActivityResource after = getAfter();
    final Map<String, String> oldConfig = before.getConfig();
    final Map<String, String> newConfig = after.getConfig();

    final ActivityBuilder activityBuilder = new ActivityBuilder()
        .addCreatedNow()
        .addAction(EventAction.UPDATE)
        .addEventName(ActivityAction.UPDATE_PATTERN_ANALYZER.getValue())
        .addPriority(EventPriority.LOW)
        .addObjectId(before.getProjectId())
        .addObjectName("pattern")
        .addObjectType(EventObject.PROJECT)
        .addProjectId(before.getProjectId())
        .addSubjectId(getUserId())
        .addSubjectName(getUserLogin())
        .addSubjectType(EventSubject.USER)
        .addHistoryField(
            processParameter(oldConfig, newConfig, AUTO_PATTERN_ANALYZER_ENABLED.getAttribute()));
    return activityBuilder.get();
  }
}
