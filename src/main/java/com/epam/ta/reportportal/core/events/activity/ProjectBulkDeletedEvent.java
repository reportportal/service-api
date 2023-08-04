/*
 * Copyright 2023 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.epam.ta.reportportal.core.events.activity;

import com.epam.ta.reportportal.builder.ActivityBuilder;
import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityAction;
import com.epam.ta.reportportal.entity.activity.EventAction;
import com.epam.ta.reportportal.entity.activity.EventObject;
import com.epam.ta.reportportal.entity.activity.EventPriority;
import com.epam.ta.reportportal.entity.activity.EventSubject;
import java.util.Collection;
import org.apache.commons.lang3.StringUtils;

/**
 * Event publish when project is created.
 *
 * @author Ryhor_Kukharenka
 */
public class ProjectBulkDeletedEvent extends AbstractEvent implements ActivityEvent {

  private static final String COMMA_SEPARATOR = ",";
  private final Collection<String> projectNames;

  public ProjectBulkDeletedEvent(Long userId, String userLogin, Collection<String> projectNames) {
    super(userId, userLogin);
    this.projectNames = projectNames;
  }

  @Override
  public Activity toActivity() {
    return new ActivityBuilder()
        .addCreatedNow()
        .addAction(EventAction.BULK_DELETE)
        .addEventName(ActivityAction.BULK_DELETE_PROJECT.getValue())
        .addPriority(EventPriority.CRITICAL)
        .addObjectName(StringUtils.join(projectNames, COMMA_SEPARATOR))
        .addObjectType(EventObject.PROJECT)
        .addSubjectId(getUserId())
        .addSubjectName(getUserLogin())
        .addSubjectType(EventSubject.USER)
        .get();
  }

}
