package com.epam.ta.reportportal.ws.controller;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;
import static com.epam.ta.reportportal.auth.permissions.Permissions.NOT_CUSTOMER;
import static com.epam.ta.reportportal.auth.permissions.Permissions.PROJECT_MANAGER;
import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;

import com.epam.reportportal.api.ProjectsApi;
import com.epam.reportportal.api.model.AddProjectToGroupByIdRequest;
import com.epam.reportportal.api.model.GetLogTypes200Response;
import com.epam.reportportal.api.model.LogType;
import com.epam.reportportal.api.model.ProjectGroupInfo;
import com.epam.reportportal.api.model.ProjectGroupsPage;
import com.epam.reportportal.api.model.SuccessfulUpdate;
import com.epam.ta.reportportal.core.group.GroupExtensionPoint;
import com.epam.ta.reportportal.core.logtype.CreateLogTypeHandler;
import com.epam.ta.reportportal.core.logtype.GetLogTypeHandler;
import lombok.RequiredArgsConstructor;
import org.pf4j.PluginManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controller for handling project collection-related requests.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 **/
@RestController
@RequiredArgsConstructor
public class GeneratedProjectController implements ProjectsApi {

  private final PluginManager pluginManager;
  private final GetLogTypeHandler getLogTypeHandler;
  private final CreateLogTypeHandler createLogTypeHandler;

  @Override
  @PreAuthorize(NOT_CUSTOMER)
  public ResponseEntity<ProjectGroupsPage> getGroupsOfProject(
      String projectName,
      Integer offset,
      Integer limit
  ) {
    var page = getGroupExtension().getProjectGroups(projectName, offset, limit);
    return ResponseEntity.ok(page);
  }

  @Override
  @PreAuthorize(NOT_CUSTOMER)
  public ResponseEntity<ProjectGroupInfo> getProjectGroupById(String projectName, Long groupId) {
    var group = getGroupExtension().getProjectGroupById(projectName, groupId).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
    );
    return ResponseEntity.ok(group);
  }

  @Override
  @PreAuthorize(PROJECT_MANAGER)
  public ResponseEntity<SuccessfulUpdate> addGroupToProjectById(
      String projectName,
      Long groupId,
      AddProjectToGroupByIdRequest addProjectToGroupByIdRequest
  ) {
    getGroupExtension().addGroupToProject(projectName, groupId, addProjectToGroupByIdRequest);
    return ResponseEntity.ok(new SuccessfulUpdate("Group updated successfully"));
  }

  @Override
  @PreAuthorize(PROJECT_MANAGER)
  public ResponseEntity<Void> deleteGroupFromProjectById(String projectName, Long groupId) {
    getGroupExtension().deleteGroupFromProject(projectName, groupId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @Override
  @PreAuthorize(ASSIGNED_TO_PROJECT)
  public ResponseEntity<GetLogTypes200Response> getLogTypes(String projectName) {
    return ResponseEntity.ok(getLogTypeHandler.getLogTypes(normalizeId(projectName)));
  }

  @Override
  @PreAuthorize(PROJECT_MANAGER)
  public ResponseEntity<LogType> createLogType(String projectName, LogType logType) {
    return new ResponseEntity<>(createLogTypeHandler.createLogType(projectName, logType),
        HttpStatus.CREATED);
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
