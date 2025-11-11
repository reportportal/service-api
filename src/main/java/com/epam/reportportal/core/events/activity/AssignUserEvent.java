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

package com.epam.reportportal.core.events.activity;

import static com.epam.reportportal.core.events.activity.util.ActivityDetailsUtil.RP_SUBJECT_NAME;
import static com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityAction.ASSIGN_USER;

import com.epam.reportportal.core.events.ActivityEvent;
import com.epam.reportportal.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventSubject;
import com.epam.reportportal.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.model.activity.UserActivityResource;
import com.epam.reportportal.util.SecurityContextUtils;

/**
 * An event that is triggered when a user is assigned to a project.
 */
public class AssignUserEvent extends AbstractEvent implements ActivityEvent {


  private final UserActivityResource userActivityResource;

  private final boolean isSystemEvent;

  private final Long orgId;


  /**
   * Constructs an AssignUserEvent with the specified details.
   *
   * @param userActivityResource The user activity resource.
   * @param userId               The ID of the user who triggered the event.
   * @param userLogin            The login of the user who triggered the event.
   * @param isSystemEvent        A flag indicating if this is a system event.
   * @param orgId                The ID of the organization.
   */
  public AssignUserEvent(UserActivityResource userActivityResource, Long userId, String userLogin,
      boolean isSystemEvent, Long orgId) {
    super(userId, userLogin);
    this.userActivityResource = userActivityResource;
    this.isSystemEvent = isSystemEvent;
    this.orgId = orgId;
  }

  /**
   * Constructs an AssignUserEvent based on the user being assigned and the project they are assigned to. The user
   * triggering the event is retrieved from the security context.
   *
   * @param user    The user being assigned.
   * @param project The project to which the user is being assigned.
   */
  public AssignUserEvent(User user, Project project) {
    super(SecurityContextUtils.getPrincipal().getUserId(),
        SecurityContextUtils.getPrincipal().getUsername());
    this.isSystemEvent = false;
    this.userActivityResource = getUserActivityResource(user, project);
    this.orgId = project.getOrganizationId();
  }

  @Override
  public Activity toActivity() {
    return new ActivityBuilder()
        .addCreatedNow()
        .addAction(EventAction.ASSIGN)
        .addEventName(ASSIGN_USER.getValue())
        .addPriority(EventPriority.HIGH)
        .addObjectId(userActivityResource.getId())
        .addObjectName(userActivityResource.getFullName())
        .addObjectType(EventObject.USER)
        .addProjectId(userActivityResource.getDefaultProjectId())
        .addOrganizationId(orgId)
        .addSubjectId(isSystemEvent ? null : getUserId())
        .addSubjectName(isSystemEvent ? RP_SUBJECT_NAME : getUserLogin())
        .addSubjectType(isSystemEvent ? EventSubject.APPLICATION : EventSubject.USER).get();
  }

  /**
   * Creates a {@link UserActivityResource} from a {@link User} and a {@link Project}.
   *
   * @param user    The user.
   * @param project The project.
   * @return The created {@link UserActivityResource}.
   */
  private UserActivityResource getUserActivityResource(User user, Project project) {
    UserActivityResource userActivityResource = new UserActivityResource();
    userActivityResource.setId(user.getId());
    userActivityResource.setDefaultProjectId(project.getId());
    userActivityResource.setFullName(user.getLogin());
    return userActivityResource;
  }

}
