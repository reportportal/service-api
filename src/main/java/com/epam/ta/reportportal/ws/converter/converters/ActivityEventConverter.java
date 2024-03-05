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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.model.ActivityEventResource;
import java.util.Objects;
import java.util.function.Function;

/**
 * Activity to ActivityEventResource Converter
 *
 * @author Ryhor_Kukharenka
 */
public final class ActivityEventConverter {

  private ActivityEventConverter() {
  }

  public static final Function<Activity, ActivityEventResource> TO_RESOURCE =
      activity -> ActivityEventResource.builder().id(activity.getId())
          .createdAt(EntityUtils.TO_DATE.apply(activity.getCreatedAt()))
          .eventName(activity.getEventName()).objectId(activity.getObjectId())
          .objectName(activity.getObjectName()).objectType(activity.getObjectType().getValue())
          .projectId(activity.getProjectId()).projectName(activity.getProjectName())
          .subjectName(activity.getSubjectName()).subjectType(activity.getSubjectType().getValue())
          .subjectId(Objects.toString(activity.getSubjectId(), null)).details(activity.getDetails())
          .build();

}
