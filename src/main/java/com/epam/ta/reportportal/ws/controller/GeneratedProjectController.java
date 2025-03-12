package com.epam.ta.reportportal.ws.controller;

import static com.epam.ta.reportportal.auth.permissions.Permissions.PROJECT_MANAGER;

import com.epam.reportportal.api.ProjectsApi;
import com.epam.reportportal.api.model.ProjectGroupsPage;
import com.epam.ta.reportportal.core.group.GroupExtensionPoint;
import org.pf4j.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
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
public class GeneratedProjectController implements ProjectsApi {

  private final PluginManager pluginManager;

  /**
   * Constructor for the controller.
   *
   * @param pluginManager Plugin manager
   */
  @Autowired
  public GeneratedProjectController(PluginManager pluginManager) {
    this.pluginManager = pluginManager;
  }

  @Override
  @PreAuthorize(PROJECT_MANAGER)
  public ResponseEntity<ProjectGroupsPage> getGroupsOfProject(
      Long projectId,
      Integer offset,
      Integer limit
  ) {
    var extension = pluginManager.getExtensions(GroupExtensionPoint.class)
        .stream()
        .findFirst()
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED)
        );
    return ResponseEntity.ok(extension.getProjectGroups(projectId, offset, limit));
  }
}
