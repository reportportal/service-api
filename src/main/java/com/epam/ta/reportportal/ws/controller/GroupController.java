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

import static com.epam.ta.reportportal.auth.permissions.Permissions.ADMIN_ONLY;

import com.epam.reportportal.api.GroupsApi;
import com.epam.reportportal.api.model.AddGroupProjectByIdRequest;
import com.epam.reportportal.api.model.CreateGroupRequest;
import com.epam.reportportal.api.model.GroupInfo;
import com.epam.reportportal.api.model.GroupPage;
import com.epam.reportportal.api.model.GroupProjectInfo;
import com.epam.reportportal.api.model.GroupProjectsPage;
import com.epam.reportportal.api.model.GroupUserInfo;
import com.epam.reportportal.api.model.GroupUsersPage;
import com.epam.reportportal.api.model.Order;
import com.epam.reportportal.api.model.SuccessfulUpdate;
import com.epam.reportportal.api.model.UpdateGroupRequest;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.group.GroupExtensionPoint;
import org.pf4j.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controller for handling group-related requests.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 */
@RestController
public class GroupController implements GroupsApi {

  private final PluginManager pluginManager;

  @Autowired
  public GroupController(PluginManager pluginManager) {
    this.pluginManager = pluginManager;
  }

  @Override
  @PreAuthorize(ADMIN_ONLY)
  public ResponseEntity<GroupPage> getGroups(
      Integer offset,
      Integer limit,
      Order order,
      String sort
  ) {
    GroupPage groupPage = getGroupExtension().getGroups(offset, limit, order, sort);
    return ResponseEntity.ok(groupPage);
  }

  @Override
  @PreAuthorize(ADMIN_ONLY)
  public ResponseEntity<GroupInfo> createGroup(CreateGroupRequest createGroupRequest) {
    GroupInfo group = getGroupExtension().createGroup(
        createGroupRequest,
        getPrincipal().getUserId()
    );
    return new ResponseEntity<>(group, HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<GroupInfo> getGroupById(Long groupId) {
    GroupInfo group = getGroupExtension().getGroupById(groupId).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
    );
    return ResponseEntity.ok(group);
  }

  @Override
  public ResponseEntity<SuccessfulUpdate> updateGroup(
      Long groupId,
      UpdateGroupRequest updateGroupRequest
  ) {
    getGroupExtension().updateGroup(groupId, updateGroupRequest);
    return ResponseEntity.ok(new SuccessfulUpdate("Group updated successfully"));
  }

  @Override
  public ResponseEntity<Void> deleteGroup(Long groupId) {
    getGroupExtension().deleteGroup(groupId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @Override
  public ResponseEntity<GroupUsersPage> getGroupUsers(
      Long groupId,
      Integer offset,
      Integer limit
  ) {
    var usersPage = getGroupExtension().getGroupUsers(groupId, offset, limit);
    return ResponseEntity.ok(usersPage);
  }

  @Override
  public ResponseEntity<GroupUserInfo> getGroupUserById(Long groupId, Long userId) {
    var groupUserInfo = getGroupExtension().getGroupUserById(groupId, userId).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
    );
    return ResponseEntity.ok(groupUserInfo);
  }

  @Override
  public ResponseEntity<SuccessfulUpdate> addUserToGroupById(Long groupId, Long userId) {
    getGroupExtension().addUserToGroupById(groupId, userId);
    return ResponseEntity.ok(new SuccessfulUpdate("Group updated successfully"));
  }

  @Override
  public ResponseEntity<Void> deleteUserFromGroupById(Long groupId, Long userId) {
    getGroupExtension().deleteUserFromGroupById(groupId, userId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @Override
  public ResponseEntity<GroupProjectsPage> getGroupProjects(
      Long groupId,
      Integer offset,
      Integer limit
  ) {
    var groupProjectsPage = getGroupExtension().getGroupProjects(groupId, offset, limit);
    return ResponseEntity.ok(groupProjectsPage);

  }

  @Override
  public ResponseEntity<GroupProjectInfo> getGroupProjectById(Long groupId, Long projectId) {
    var groupProjectInfo = getGroupExtension().getGroupProjectById(groupId, projectId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    return ResponseEntity.ok(groupProjectInfo);
  }

  @Override
  public ResponseEntity<SuccessfulUpdate> addGroupProjectById(
      Long groupId,
      Long projectId,
      AddGroupProjectByIdRequest addGroupProjectByIdRequest
  ) {
    getGroupExtension().addGroupProjectById(groupId, projectId, addGroupProjectByIdRequest);
    return ResponseEntity.ok(new SuccessfulUpdate("Group updated successfully"));
  }

  @Override
  public ResponseEntity<Void> deleteProjectFromGroupById(Long groupId, Long projectId) {
    getGroupExtension().deleteProjectFromGroupById(groupId, projectId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  private GroupExtensionPoint getGroupExtension() {
    return pluginManager.getExtensions(GroupExtensionPoint.class)
        .stream()
        .findFirst()
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED)
        );
  }

  private ReportPortalUser getPrincipal() {
    return (ReportPortalUser) SecurityContextHolder
        .getContext()
        .getAuthentication()
        .getPrincipal();
  }
}
