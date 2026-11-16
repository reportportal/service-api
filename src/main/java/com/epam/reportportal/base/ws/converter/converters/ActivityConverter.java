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

import static java.util.Optional.ofNullable;

import com.epam.reportportal.api.model.ProjectActivity;
import com.epam.reportportal.base.infrastructure.model.ActivityResource;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.Activity;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Maps activity entities to activity event resources.
 *
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public final class ActivityConverter {

  public static final Function<Activity, ActivityResource> TO_RESOURCE = activity -> {
    ActivityResource resource = new ActivityResource();
    resource.setId(activity.getId());
    resource.setLastModified(activity.getCreatedAt());
    resource.setObjectType(activity.getObjectType().toString());
    resource.setActionType(activity.getEventName());
    resource.setProjectId(activity.getProjectId());
    resource.setUser(activity.getSubjectName());
    ofNullable(activity.getObjectId()).ifPresent(resource::setLoggedObjectId);
    resource.setObjectName(activity.getObjectName());
    resource.setDetails(activity.getDetails());
    return resource;
  };
  public static final BiFunction<Activity, String, ActivityResource> TO_RESOURCE_WITH_USER = (activity, username) -> {
    ActivityResource resource = TO_RESOURCE.apply(activity);
    resource.setUser(username);
    return resource;
  };

  /**
   * Maps a legacy {@link ActivityResource} (used by the still-live {@code GET /v1/{projectKey}/activity/{activityId}}
   * endpoint) to the generated API-first {@link com.epam.reportportal.api.model.ProjectActivity} model, so the new
   * API-first endpoint can reuse {@link com.epam.reportportal.base.core.activity.ActivityHandler} without duplicating
   * its lookup/validation logic.
   */
  public static final Function<ActivityResource, ProjectActivity> TO_PROJECT_ACTIVITY_API_MODEL = resource -> new ProjectActivity()
      .id(resource.getId())
      .user(resource.getUser())
      .userId(resource.getUserId())
      .loggedObjectId(resource.getLoggedObjectId())
      .lastModified(resource.getLastModified())
      .actionType(resource.getActionType())
      .objectType(resource.getObjectType())
      .projectId(resource.getProjectId())
      .projectName(resource.getProjectName())
      .projectKey(resource.getProjectKey())
      .objectName(resource.getObjectName())
      .details(ActivityEventConverter.convertDetails(resource.getDetails()));

  private ActivityConverter() {
    //static only
  }
}
