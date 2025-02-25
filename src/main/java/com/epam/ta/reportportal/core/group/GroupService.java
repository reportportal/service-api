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
import org.pf4j.ExtensionPoint;

public interface GroupService extends ExtensionPoint {
  GroupPage getGroups(Integer offset, Integer limit, Order order, String sort);
  Group createGroup(CreateGroupRequest createGroupRequest);
  Group getGroupById(Long groupId);
  SuccessfulUpdate updateGroup(Long groupId, UpdateGroupRequest updateGroupRequest);
  void deleteGroup(Long groupId);
  GroupUsersPage getGroupUsers(Long groupId, Integer offset, Integer limit, Order order);
  GroupUser getGroupUserById(Long groupId, Long userId);
  void addUserToGroupById(Long groupId, Long userId);
  void deleteUserFromGroupById(Long groupId, Long userId);
  GroupProjectsPage getGroupProjects(Long groupId, Integer offset, Integer limit, Order order);
  GroupProject getGroupProjectById(Long groupId, Long projectId);
  void addGroupProjectById(Long groupId, Long projectId, AddGroupProjectByIdRequest addGroupProjectByIdRequest);
  void deleteProjectFromGroupById(Long groupId, Long projectId);
}
