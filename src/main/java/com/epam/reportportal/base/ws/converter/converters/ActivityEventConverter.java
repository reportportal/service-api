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

package com.epam.reportportal.base.ws.converter.converters;

import com.epam.reportportal.base.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.ActivityDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.HistoryField;
import com.epam.reportportal.base.model.ActivityEventResource;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import org.apache.commons.collections4.CollectionUtils;

/**
 * Activity to ActivityEventResource Converter.
 *
 * @author Ryhor_Kukharenka
 */
public final class ActivityEventConverter {

  private ActivityEventConverter() {
  }

  public static final Function<Activity, ActivityEventResource> TO_RESOURCE =
      activity -> ActivityEventResource.builder()
          .id(activity.getId())
          .createdAt(activity.getCreatedAt())
          .eventName(activity.getEventName()).objectId(activity.getObjectId())
          .objectName(activity.getObjectName()).objectType(activity.getObjectType().getValue())
          .projectId(activity.getProjectId()).projectName(activity.getProjectName())
          .subjectName(activity.getSubjectName()).subjectType(activity.getSubjectType().getValue())
          .subjectId(Objects.toString(activity.getSubjectId(), null)).details(activity.getDetails())
          .build();

  public static final Function<Activity, com.epam.reportportal.api.model.Activity> TO_ACTIVITY_RESOURCE =
      activity -> new com.epam.reportportal.api.model.Activity()
          .id(activity.getId())
          .createdAt(activity.getCreatedAt())
          .eventName(activity.getEventName())
          .objectId(activity.getObjectId())
          .objectName(activity.getObjectName())
          .objectType(activity.getObjectType().getValue())
          .projectId(activity.getProjectId())
          .projectName(activity.getProjectName())
          .subjectName(activity.getSubjectName())
          .subjectType(activity.getSubjectType().getValue())
          .details(convertDetails(activity.getDetails()));

  /**
   * Maps a legacy {@link com.epam.reportportal.base.model.ActivityEventResource} (used by the still-live
   * {@code /v1/{projectKey}/activity/**} endpoints) to the generated API-first
   * {@link com.epam.reportportal.api.model.Activity} model, so the new API-first endpoints can reuse
   * {@link com.epam.reportportal.base.core.activity.ActivityHandler} without duplicating its query logic.
   */
  public static final Function<ActivityEventResource, com.epam.reportportal.api.model.Activity> TO_API_MODEL =
      resource -> new com.epam.reportportal.api.model.Activity()
          .id(resource.getId())
          .createdAt(resource.getCreatedAt())
          .eventName(resource.getEventName())
          .objectId(resource.getObjectId())
          .objectName(resource.getObjectName())
          .objectType(resource.getObjectType())
          .projectId(resource.getProjectId())
          .projectName(resource.getProjectName())
          .subjectName(resource.getSubjectName())
          .subjectType(resource.getSubjectType())
          .subjectId(resource.getSubjectId())
          .details(convertDetails(resource.getDetails()));

  /**
   * Converts the raw activity details payload (always a
   * {@link com.epam.reportportal.base.infrastructure.persistence.entity.activity.ActivityDetails} at runtime) into the
   * generated {@link ActivityDetails} model. Always returns a non-null {@link ActivityDetails} with a (possibly empty)
   * {@code history} list, matching the legacy {@code /v1/{projectKey}/activity/**} endpoints' contract of always
   * including a {@code details} object.
   */
  public static com.epam.reportportal.api.model.ActivityDetails convertDetails(Object details) {
    Collection<HistoryField> history = details instanceof ActivityDetails detailsEntity
        ? CollectionUtils.emptyIfNull(detailsEntity.getHistory())
        : List.of();

    return new com.epam.reportportal.api.model.ActivityDetails()
        .history(history.stream()
            .map(historyField -> new com.epam.reportportal.api.model.HistoryField()
                .field(historyField.getField())
                .newValue(historyField.getNewValue())
                .oldValue(historyField.getOldValue()))
            .toList());
  }


}
