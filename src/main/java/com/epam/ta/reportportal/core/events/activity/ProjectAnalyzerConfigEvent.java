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

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.configEquals;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.processParameter;
import static com.epam.ta.reportportal.entity.activity.Activity.ActivityEntityType.PROJECT;
import static com.epam.ta.reportportal.entity.activity.ActivityAction.UPDATE_ANALYZER;
import static com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum.ALL_MESSAGES_SHOULD_MATCH;
import static com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum.AUTO_ANALYZER_ENABLED;
import static com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum.AUTO_ANALYZER_MODE;
import static com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum.AUTO_UNIQUE_ERROR_ANALYZER_ENABLED;
import static com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum.MIN_SHOULD_MATCH;
import static com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum.NUMBER_OF_LOG_LINES;
import static com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum.Prefix;
import static com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum.SEARCH_LOGS_MIN_SHOULD_MATCH;
import static com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum.UNIQUE_ERROR_ANALYZER_REMOVE_NUMBERS;

import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import com.epam.ta.reportportal.ws.model.activity.ProjectAttributesActivityResource;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Pavel Bortnik
 */
public class ProjectAnalyzerConfigEvent extends
    AroundEvent<ProjectAttributesActivityResource> implements ActivityEvent {

  public ProjectAnalyzerConfigEvent() {
  }

  public ProjectAnalyzerConfigEvent(ProjectAttributesActivityResource before,
      ProjectAttributesActivityResource after, Long userId,
      String userLogin) {
    super(userId, userLogin, before, after);
  }

  @Override
  public Activity toActivity() {
    return configEquals(getBefore().getConfig(), getAfter().getConfig(), Prefix.ANALYZER) ? null
        : convert();
  }

  private Activity convert() {
    final ProjectAttributesActivityResource before = getBefore();
    final ProjectAttributesActivityResource after = getAfter();

    final Map<String, String> oldConfig = before.getConfig();
    final Map<String, String> newConfig = after.getConfig();

    final ActivityBuilder activityBuilder = new ActivityBuilder().addCreatedNow()
        .addAction(UPDATE_ANALYZER)
        .addActivityEntityType(PROJECT)
        .addUserId(getUserId())
        .addUserName(getUserLogin())
        .addObjectId(before.getProjectId())
        .addObjectName(before.getProjectName())
        .addProjectId(before.getProjectId());

    Stream.of(AUTO_ANALYZER_MODE,
            MIN_SHOULD_MATCH,
            SEARCH_LOGS_MIN_SHOULD_MATCH,
            NUMBER_OF_LOG_LINES,
            AUTO_ANALYZER_ENABLED,
            AUTO_UNIQUE_ERROR_ANALYZER_ENABLED,
            UNIQUE_ERROR_ANALYZER_REMOVE_NUMBERS,
            ALL_MESSAGES_SHOULD_MATCH
        ).map(type -> processParameter(oldConfig, newConfig, type.getAttribute()))
        .forEach(activityBuilder::addHistoryField);

    return activityBuilder.get();
  }

}
