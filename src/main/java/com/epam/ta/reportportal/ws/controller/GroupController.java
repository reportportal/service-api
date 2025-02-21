package com.epam.ta.reportportal.ws.controller;

import com.epam.reportportal.api.GroupsApi;
import com.epam.reportportal.api.model.CreateGroupRequest;
import com.epam.reportportal.api.model.Group;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Reing
 */
@RestController
public class GroupController implements GroupsApi {

  @Override
  public ResponseEntity<Group> createGroup(CreateGroupRequest createGroupRequest) {
    return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
  }

}
