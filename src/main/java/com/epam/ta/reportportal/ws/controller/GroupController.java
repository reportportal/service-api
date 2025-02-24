package com.epam.ta.reportportal.ws.controller;

import com.epam.reportportal.api.GroupsApi;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling group-related requests.
 *
 * @author Reingold Shekhtel
 */
@RestController
public class GroupController implements GroupsApi {

  @Override
  public ResponseEntity<GroupPage> getGroups(
      Integer offset,
      Integer limit,
      Order order,
      String sort
  ) {
    return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
  }

  @Override
  public ResponseEntity<Group> createGroup(CreateGroupRequest createGroupRequest) {
    return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
  }

  @Override
  public ResponseEntity<Group> getGroupById(Long groupId) {
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
}
