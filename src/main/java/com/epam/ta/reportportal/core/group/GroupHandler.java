package com.epam.ta.reportportal.core.group;

import com.epam.reportportal.api.model.AddGroupProjectByIdRequest;
import com.epam.reportportal.api.model.CreateGroupRequest;
import com.epam.reportportal.api.model.Group;
import com.epam.reportportal.api.model.GroupPage;
import com.epam.reportportal.api.model.GroupProject;
import com.epam.reportportal.api.model.GroupProjectsPage;
import com.epam.reportportal.api.model.GroupUser;
import com.epam.reportportal.api.model.GroupUsersPage;
import com.epam.reportportal.api.model.Order;
import com.epam.reportportal.api.model.SuccessfulUpdate;
import com.epam.reportportal.api.model.UpdateGroupRequest;
import com.epam.reportportal.extension.ReportPortalExtensionPoint;
import java.util.Optional;

/**
 * Handler for group operations.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 */
public interface GroupHandler extends ReportPortalExtensionPoint {

  /**
   * Returns a page of groups.
   *
   * @param offset The number of groups to skip before starting to collect the result set.
   * @param limit The number of groups to return.
   * @param order The order of sorting.
   * @param sort The field to sort by.
   * @return A page of groups.
   */
  Optional<GroupPage> getGroups(Integer offset, Integer limit, Order order, String sort);

  /**
   * Creates a group.
   *
   * @param createGroupRequest The request to create a group.
   * @return The created group.
   */
  Optional<Group> createGroup(CreateGroupRequest createGroupRequest);

  /**
   * Returns a group by its ID.
   *
   * @param groupId The ID of the group.
   * @return The group.
   */
  Optional<Group> getGroupById(Long groupId);

  /**
   * Updates a group.
   *
   * @param groupId The ID of the group.
   * @param updateGroupRequest The request to update the group.
   * @return The updated group.
   */
  Optional<SuccessfulUpdate> updateGroup(Long groupId, UpdateGroupRequest updateGroupRequest);

  /**
   * Deletes a group.
   *
   * @param groupId The ID of the group.
   */
  void deleteGroup(Long groupId);

  /**
   * Returns a page of group users.
   *
   * @param groupId The ID of the group.
   * @param offset The number of group users to skip before starting to collect the result set.
   * @param limit The number of group users to return.
   * @param order The order of sorting.
   * @return A page of group users.
   */
  Optional<GroupUsersPage> getGroupUsers(Long groupId, Integer offset, Integer limit, Order order);

  /**
   * Returns a group user by group ID and user ID.
   *
   * @param groupId The ID of the group.
   * @param userId The ID of the user.
   * @return The group user.
   */
  Optional<GroupUser> getGroupUserById(Long groupId, Long userId);

  /**
   * Adds a user to a group by group ID and user ID.
   *
   * @param groupId The ID of the group.
   * @param userId The ID of the user.
   */
  void addUserToGroupById(Long groupId, Long userId);

  /**
   * Deletes a user from a group by group ID and user ID.
   *
   * @param groupId The ID of the group.
   * @param userId The ID of the user.
   */
  void deleteUserFromGroupById(Long groupId, Long userId);

  /**
   * Returns a page of group projects.
   *
   * @param groupId The ID of the group.
   * @param offset The number of group projects to skip before starting to collect the result set.
   * @param limit The number of group projects to return.
   * @param order The order of sorting.
   * @return A page of group projects.
   */
  Optional<GroupProjectsPage> getGroupProjects(Long groupId, Integer offset, Integer limit, Order order);

  /**
   * Returns a group project by group ID and project ID.
   *
   * @param groupId The ID of the group.
   * @param projectId The ID of the project.
   * @return The group project.
   */
  Optional<GroupProject> getGroupProjectById(Long groupId, Long projectId);

  /**
   * Adds a project to a group by group ID and project ID.
   *
   * @param groupId The ID of the group.
   * @param projectId The ID of the project.
   * @param addGroupProjectByIdRequest The request to add a project to a group.
   */
  void addGroupProjectById(Long groupId, Long projectId,
      AddGroupProjectByIdRequest addGroupProjectByIdRequest);

  /**
   * Deletes a project from a group by group ID and project ID.
   *
   * @param groupId The ID of the group.
   * @param projectId The ID of the project.
   */
  void deleteProjectFromGroupById(Long groupId, Long projectId);
}
