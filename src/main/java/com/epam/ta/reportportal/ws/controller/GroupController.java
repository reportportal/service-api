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

package com.epam.ta.reportportal.ws.controller;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ALLOWED_TO_EDIT_GROUP;
import static com.epam.ta.reportportal.auth.permissions.Permissions.IS_ADMIN;
import static com.epam.ta.reportportal.util.SecurityContextUtils.getPrincipal;

import com.epam.reportportal.api.GroupsApi;
import com.epam.reportportal.api.model.AddProjectToGroupByIdRequest;
import com.epam.reportportal.api.model.CreateGroupRequest;
import com.epam.reportportal.api.model.GroupInfo;
import com.epam.reportportal.api.model.GroupPage;
import com.epam.reportportal.api.model.GroupProjectInfo;
import com.epam.reportportal.api.model.GroupProjectsPage;
import com.epam.reportportal.api.model.GroupUserInfo;
import com.epam.reportportal.api.model.GroupUsersPage;
import com.epam.reportportal.api.model.SuccessfulUpdate;
import com.epam.reportportal.api.model.UpdateGroupRequest;
import com.epam.ta.reportportal.core.group.GroupExtensionPoint;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controller for handling group-related requests.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 */
@RestController
public class GroupController implements GroupsApi {

  private final Pf4jPluginBox pluginBox;

  /**
   * Constructor for the {@link GroupController} class.
   *
   * @param pluginBox The {@link Pf4jPluginBox} instance used to access plugin extensions.
   */
  @Autowired
  public GroupController(Pf4jPluginBox pluginBox) {
    this.pluginBox = pluginBox;
  }

  @Override
  @PreAuthorize(IS_ADMIN)
  @Transactional(readOnly = true)
  public ResponseEntity<GroupPage> getGroups(
      Integer offset,
      Integer limit,
      String order,
      String sort,
      Long orgId
  ) {
    GroupPage groupPage = getGroupExtension().getGroups(offset, limit, order, sort, orgId);
    return ResponseEntity.ok(groupPage);
  }

  @Override
  @PreAuthorize(IS_ADMIN)
  @Transactional
  public ResponseEntity<GroupInfo> createGroup(CreateGroupRequest createGroupRequest) {
    GroupInfo group = getGroupExtension().createGroup(
        createGroupRequest,
        getPrincipal().getUserId()
    );
    return new ResponseEntity<>(group, HttpStatus.CREATED);
  }

  @Override
  @PreAuthorize(ALLOWED_TO_EDIT_GROUP)
  @Transactional(readOnly = true)
  public ResponseEntity<GroupInfo> getGroupById(Long groupId) {
    GroupInfo group = getGroupExtension().getGroupById(groupId).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
    );
    return ResponseEntity.ok(group);
  }

  @Override
  @PreAuthorize(ALLOWED_TO_EDIT_GROUP)
  @Transactional
  public ResponseEntity<SuccessfulUpdate> updateGroup(
      Long groupId,
      UpdateGroupRequest updateGroupRequest
  ) {
    getGroupExtension().updateGroup(groupId, updateGroupRequest);
    return ResponseEntity.ok(new SuccessfulUpdate("Group updated successfully"));
  }

  @Override
  @PreAuthorize(ALLOWED_TO_EDIT_GROUP)
  @Transactional
  public ResponseEntity<Void> deleteGroup(Long groupId) {
    getGroupExtension().deleteGroup(groupId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @Override
  @PreAuthorize(ALLOWED_TO_EDIT_GROUP)
  @Transactional(readOnly = true)
  public ResponseEntity<GroupUsersPage> getGroupUsers(
      Long groupId,
      Integer offset,
      Integer limit
  ) {
    var usersPage = getGroupExtension().getGroupUsers(groupId, offset, limit);
    return ResponseEntity.ok(usersPage);
  }

  @Override
  @PreAuthorize(ALLOWED_TO_EDIT_GROUP)
  @Transactional(readOnly = true)
  public ResponseEntity<GroupUserInfo> getGroupUserById(Long groupId, Long userId) {
    var groupUserInfo = getGroupExtension().getGroupUserById(groupId, userId).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
    );
    return ResponseEntity.ok(groupUserInfo);
  }

  @Override
  @PreAuthorize(ALLOWED_TO_EDIT_GROUP)
  @Transactional
  public ResponseEntity<SuccessfulUpdate> addUserToGroupById(Long groupId, Long userId) {
    getGroupExtension().addUserToGroupById(groupId, userId);
    return ResponseEntity.ok(new SuccessfulUpdate("Group updated successfully"));
  }

  @Override
  @PreAuthorize(ALLOWED_TO_EDIT_GROUP)
  @Transactional
  public ResponseEntity<Void> deleteUserFromGroupById(Long groupId, Long userId) {
    getGroupExtension().deleteUserFromGroupById(groupId, userId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @Override
  @PreAuthorize(ALLOWED_TO_EDIT_GROUP)
  @Transactional(readOnly = true)
  public ResponseEntity<GroupProjectsPage> getGroupProjects(
      Long groupId,
      Integer offset,
      Integer limit
  ) {
    var groupProjectsPage = getGroupExtension().getGroupProjects(groupId, offset, limit);
    return ResponseEntity.ok(groupProjectsPage);

  }

  @Override
  @PreAuthorize(ALLOWED_TO_EDIT_GROUP)
  @Transactional(readOnly = true)
  public ResponseEntity<GroupProjectInfo> getGroupProjectById(Long groupId, Long projectId) {
    var groupProjectInfo = getGroupExtension().getGroupProjectById(groupId, projectId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    return ResponseEntity.ok(groupProjectInfo);
  }

  @Override
  @PreAuthorize(ALLOWED_TO_EDIT_GROUP)
  @Transactional
  public ResponseEntity<SuccessfulUpdate> addProjectToGroupById(
      Long groupId,
      Long projectId,
      AddProjectToGroupByIdRequest addProjectToGroupByIdRequest
  ) {
    getGroupExtension().addProjectToGroupById(groupId, projectId, addProjectToGroupByIdRequest);
    return ResponseEntity.ok(new SuccessfulUpdate("Group updated successfully"));
  }

  @Override
  @PreAuthorize(ALLOWED_TO_EDIT_GROUP)
  @Transactional
  public ResponseEntity<Void> deleteProjectFromGroupById(Long groupId, Long projectId) {
    getGroupExtension().deleteProjectFromGroupById(groupId, projectId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  private GroupExtensionPoint getGroupExtension() {
    return pluginBox.getInstance(GroupExtensionPoint.class)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.PAYMENT_REQUIRED,
            "Group management is not available. Please install the 'group' plugin."
        ));
  }
}
