package com.epam.ta.reportportal.ws.controller;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ALLOWED_TO_EDIT_PROJECT;
import static com.epam.ta.reportportal.auth.permissions.Permissions.ALLOWED_TO_VIEW_PROJECT;

import com.epam.reportportal.api.ProjectsApi;
import com.epam.reportportal.api.model.AddProjectToGroupByIdRequest;
import com.epam.reportportal.api.model.ProjectGroupInfo;
import com.epam.reportportal.api.model.ProjectGroupsPage;
import com.epam.reportportal.api.model.SuccessfulUpdate;
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
 * Controller for handling project collection-related requests.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 **/
@RestController
public class GeneratedProjectController implements ProjectsApi {

  private final Pf4jPluginBox pluginBox;

  /**
   * Constructor for the controller.
   *
   * @param pluginBox The {@link Pf4jPluginBox} instance used to access plugin extensions.
   */
  @Autowired
  public GeneratedProjectController(Pf4jPluginBox pluginBox) {
    this.pluginBox = pluginBox;
  }

  @Override
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  @Transactional(readOnly = true)
  public ResponseEntity<ProjectGroupsPage> getGroupsOfProject(
      String projectKey,
      Integer offset,
      Integer limit
  ) {
    var page = getGroupExtension().getProjectGroups(projectKey, offset, limit);
    return ResponseEntity.ok(page);
  }

  @Override
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  @Transactional(readOnly = true)
  public ResponseEntity<ProjectGroupInfo> getProjectGroupById(String projectKey, Long groupId) {
    var group = getGroupExtension().getProjectGroupById(projectKey, groupId).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
    );
    return ResponseEntity.ok(group);
  }

  @Override
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  @Transactional
  public ResponseEntity<SuccessfulUpdate> addGroupToProjectById(
      String projectKey,
      Long groupId,
      AddProjectToGroupByIdRequest addProjectToGroupByIdRequest
  ) {
    getGroupExtension().addGroupToProject(projectKey, groupId, addProjectToGroupByIdRequest);
    return ResponseEntity.ok(new SuccessfulUpdate("Group updated successfully"));
  }

  @Override
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  @Transactional
  public ResponseEntity<Void> deleteGroupFromProjectById(String projectName, Long groupId) {
    getGroupExtension().deleteGroupFromProject(projectName, groupId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  private GroupExtensionPoint getGroupExtension() {
    return pluginBox.getInstance(GroupExtensionPoint.class)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.PAYMENT_REQUIRED,
            "Group management is not available. Please install the 'group' plugin."));
  }
}
