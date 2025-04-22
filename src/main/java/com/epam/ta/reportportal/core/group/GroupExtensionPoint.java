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

package com.epam.ta.reportportal.core.group;

import com.epam.reportportal.api.model.AddProjectToGroupByIdRequest;
import com.epam.reportportal.api.model.CreateGroupRequest;
import com.epam.reportportal.api.model.GroupInfo;
import com.epam.reportportal.api.model.GroupPage;
import com.epam.reportportal.api.model.GroupProjectInfo;
import com.epam.reportportal.api.model.GroupProjectsPage;
import com.epam.reportportal.api.model.GroupUserInfo;
import com.epam.reportportal.api.model.GroupUsersPage;
import com.epam.reportportal.api.model.ProjectGroupInfo;
import com.epam.reportportal.api.model.ProjectGroupsPage;
import com.epam.reportportal.api.model.UpdateGroupRequest;
import com.epam.reportportal.extension.ReportPortalExtensionPoint;
import java.util.Optional;
import org.jclouds.rest.ResourceNotFoundException;

/**
 * Extension point for group-related operations.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 */
public interface GroupExtensionPoint extends ReportPortalExtensionPoint {

  /**
   * Returns a page of groups.
   *
   * @param offset The number of groups to skip before starting to collect the result set.
   * @param limit  The number of groups to return.
   * @param order  The order of sorting.
   * @param sort   The field to sort by.
   * @return {@link GroupPage} containing a page of groups.
   */
  GroupPage getGroups(Integer offset, Integer limit, String order, String sort);

  /**
   * Creates a group.
   *
   * @param createGroupRequest The request to create a group.
   * @param userId             The ID of the user who creates the group.
   * @return {@link GroupInfo} containing the created group.
   */
  GroupInfo createGroup(CreateGroupRequest createGroupRequest, Long userId);

  /**
   * Returns a group by its ID.
   *
   * @param groupId The ID of the group.
   * @return {@link GroupInfo} with the group information.
   */
  Optional<GroupInfo> getGroupById(Long groupId);

  /**
   * Updates a group.
   *
   * @param groupId            The ID of the group.
   * @param updateGroupRequest The request to update the group.
   */
  void updateGroup(Long groupId, UpdateGroupRequest updateGroupRequest);

  /**
   * Deletes a group.
   *
   * @param groupId The ID of the group.
   * @throws ResourceNotFoundException if the group is not found.
   */
  void deleteGroup(Long groupId) throws ResourceNotFoundException;

  /**
   * Returns a page of group users.
   *
   * @param groupId The ID of the group.
   * @param offset  The number of group users to skip before starting to collect the result set.
   * @param limit   The number of group users to return.
   * @return {@link GroupUsersPage} containing a page of group users.
   */
  GroupUsersPage getGroupUsers(Long groupId, Integer offset, Integer limit);

  /**
   * Returns a group user by group ID and user ID.
   *
   * @param groupId The ID of the group.
   * @param userId  The ID of the user.
   * @return {@link Optional} of {@link GroupUserInfo} containing the group user.
   */
  Optional<GroupUserInfo> getGroupUserById(Long groupId, Long userId);

  /**
   * Adds a user to a group by group ID and user ID.
   *
   * @param groupId The ID of the group.
   * @param userId  The ID of the user.
   */
  void addUserToGroupById(Long groupId, Long userId);

  /**
   * Deletes a user from a group by group ID and user ID.
   *
   * @param groupId The ID of the group.
   * @param userId  The ID of the user.
   */
  void deleteUserFromGroupById(Long groupId, Long userId);

  /**
   * Returns a page of group projects.
   *
   * @param groupId The ID of the group.
   * @param offset  The number of group projects to skip before starting to collect the result set.
   * @param limit   The number of group projects to return.
   * @return {@link Optional} of {@link GroupProjectsPage} containing a page of group projects.
   */
  GroupProjectsPage getGroupProjects(Long groupId, Integer offset, Integer limit);

  /**
   * Returns a group project by group ID and project ID.
   *
   * @param groupId   The ID of the group.
   * @param projectId The ID of the project.
   * @return {@link GroupProjectInfo} containing the group project information.
   */
  Optional<GroupProjectInfo> getGroupProjectById(Long groupId, Long projectId);

  /**
   * Adds a project to a group by group ID and project ID.
   *
   * @param groupId                    The ID of the group.
   * @param projectId                  The ID of the project.
   * @param addGroupProjectByIdRequest The request to add a project to a group.
   */
  void addProjectToGroupById(Long groupId, Long projectId,
      AddProjectToGroupByIdRequest addGroupProjectByIdRequest);

  /**
   * Deletes a project from a group by group ID and project ID.
   *
   * @param groupId   The ID of the group.
   * @param projectId The ID of the project.
   */
  void deleteProjectFromGroupById(Long groupId, Long projectId);

  /**
   * Returns a page of project groups.
   *
   * @param projectName The name of the project.
   * @param offset      The number of project groups to skip before starting to collect the result
   *                    set.
   * @param limit       The number of project groups to return.
   * @return {@link ProjectGroupsPage} containing a page of project groups.
   */
  ProjectGroupsPage getProjectGroups(String projectName, Integer offset, Integer limit);

  /**
   * Returns a group project by group ID and project name.
   *
   * @param projectName The name of the project.
   * @param groupId   The ID of the group.
   * @return {@link GroupProjectInfo} containing the group project information.
   */
  Optional<ProjectGroupInfo> getProjectGroupById(String projectName, Long groupId);

  /**
   * Adds a project to a group by group ID and project name.
   *
   * @param projectName                The Name of the project.
   * @param groupId                    The ID of the group.
   * @param addGroupProjectByIdRequest The request to add a project to a group.
   */
  void addGroupToProject(String projectName, Long groupId,
      AddProjectToGroupByIdRequest addGroupProjectByIdRequest);

  /**
   * Deletes a project from a group by group ID and project name.
   *
   * @param projectName The Name of the project.
   * @param groupId   The ID of the group.
   */
  void deleteGroupFromProject(String projectName, Long groupId);
}
