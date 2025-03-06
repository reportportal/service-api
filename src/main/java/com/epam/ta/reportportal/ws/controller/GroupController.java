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
import com.epam.reportportal.api.model.GroupProject;
import com.epam.reportportal.api.model.GroupProjectsPage;
import com.epam.reportportal.api.model.GroupUser;
import com.epam.reportportal.api.model.GroupUsersPage;
import com.epam.reportportal.api.model.Order;
import com.epam.reportportal.api.model.SuccessfulUpdate;
import com.epam.reportportal.api.model.UpdateGroupRequest;
import com.epam.ta.reportportal.core.group.GroupExtensionPoint;
import org.pf4j.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controller for handling group-related requests.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 */
@RestController
public class GroupController implements GroupsApi {

  @Autowired
  private PluginManager pluginManager;

  @Override
  @PreAuthorize(ADMIN_ONLY)
  public ResponseEntity<GroupPage> getGroups(
      Integer offset,
      Integer limit,
      Order order,
      String sort
  ) {
    GroupPage groupPage = getGroupExtension().getGroups(offset, limit, order, sort)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED)
        );
    return new ResponseEntity<>(groupPage, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<GroupInfo> createGroup(CreateGroupRequest createGroupRequest) {
    return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
  }

  @Override
  public ResponseEntity<GroupInfo> getGroupById(Long groupId) {
    return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
  }

  @Override
  public ResponseEntity<SuccessfulUpdate> updateGroup(
      Long groupId,
      UpdateGroupRequest updateGroupRequest
  ) {
    return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
  }

  @Override
  public ResponseEntity<Void> deleteGroup(Long groupId) {
    return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
  }

  @Override
  public ResponseEntity<GroupUsersPage> getGroupUsers(
      Long groupId,
      Integer offset,
      Integer limit,
      Order order
  ) {
    return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
  }

  @Override
  public ResponseEntity<GroupUser> getGroupUserById(Long groupId, Long userId) {
    return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
  }

  @Override
  public ResponseEntity<Void> addUserToGroupById(Long groupId, Long userId) {
    return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
  }

  @Override
  public ResponseEntity<Void> deleteUserFromGroupById(Long groupId, Long userId) {
    return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
  }

  @Override
  public ResponseEntity<GroupProjectsPage> getGroupProjects(
      Long groupId,
      Integer offset,
      Integer limit,
      Order order
  ) {
    return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
  }

  @Override
  public ResponseEntity<GroupProject> getGroupProjectById(Long groupId, Long projectId) {
    return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
  }

  @Override
  public ResponseEntity<Void> addGroupProjectById(
      Long groupId,
      Long projectId,
      AddGroupProjectByIdRequest addGroupProjectByIdRequest
  ) {
    return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
  }

  @Override
  public ResponseEntity<Void> deleteProjectFromGroupById(Long groupId, Long projectId) {
    return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
  }

  private GroupExtensionPoint getGroupExtension() {
    return pluginManager.getExtensions(GroupExtensionPoint.class)
        .stream()
        .findFirst()
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED)
        );
  }
}
