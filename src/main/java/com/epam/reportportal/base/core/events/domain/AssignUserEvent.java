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

package com.epam.reportportal.base.core.events.domain;

import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.base.model.activity.UserActivityResource;
import com.epam.reportportal.base.util.SecurityContextUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Event triggered when a user is assigned to a project. Uses {@code after} field to hold the assigned user resource
 * (before=null for CREATE events).
 */
@Getter
@NoArgsConstructor
public class AssignUserEvent extends AbstractEvent<UserActivityResource> {

  /**
   * Constructs an AssignUserEvent with the specified details.
   *
   * @param userActivityResource The user activity resource.
   * @param userId               The ID of the user who triggered the event.
   * @param userLogin            The login of the user who triggered the event.
   * @param orgId                The ID of the organization.
   */
  public AssignUserEvent(UserActivityResource userActivityResource, Long userId, String userLogin,
      Long orgId) {
    super(userId, userLogin);
    this.after = userActivityResource;
    this.organizationId = orgId;
  }

  /**
   * Constructs an AssignUserEvent with the specified details (without userId, userLogin - system event).
   *
   * @param userActivityResource The user activity resource.
   * @param orgId                The ID of the organization.
   */
  public AssignUserEvent(UserActivityResource userActivityResource, Long orgId) {
    super();
    this.after = userActivityResource;
    this.organizationId = orgId;
  }

  /**
   * Constructs an AssignUserEvent based on the user being assigned and the project they are assigned to. The user
   * triggering the event is retrieved from the security context.
   *
   * @param user    The user being assigned.
   * @param project The project to which the user is being assigned.
   */
  public AssignUserEvent(User user, Project project) {
    super();
    this.userId = SecurityContextUtils.getPrincipal().getUserId();
    this.userLogin = SecurityContextUtils.getPrincipal().getUsername();
    this.after = createUserActivityResource(user, project);
    this.organizationId = project.getOrganizationId();
  }

  /**
   * Convenience method to get the assigned user resource. Equivalent to {@code getAfter()}.
   */
  public UserActivityResource getUserActivityResource() {
    return getAfter();
  }

  /**
   * Creates a {@link UserActivityResource} from a {@link User} and a {@link Project}.
   *
   * @param user    The user.
   * @param project The project.
   * @return The created {@link UserActivityResource}.
   */
  private static UserActivityResource createUserActivityResource(User user, Project project) {
    UserActivityResource userActivityResource = new UserActivityResource();
    userActivityResource.setId(user.getId());
    userActivityResource.setDefaultProjectId(project.getId());
    userActivityResource.setFullName(user.getLogin());
    return userActivityResource;
  }

}
