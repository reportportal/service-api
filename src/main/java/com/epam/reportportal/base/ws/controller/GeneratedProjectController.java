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

package com.epam.reportportal.base.ws.controller;

import static com.epam.reportportal.base.auth.permissions.Permissions.ALLOWED_TO_EDIT_PROJECT;
import static com.epam.reportportal.base.auth.permissions.Permissions.ALLOWED_TO_VIEW_PROJECT;
import static com.epam.reportportal.base.infrastructure.persistence.commons.EntityUtils.normalizeId;

import com.epam.reportportal.api.ProjectsApi;
import com.epam.reportportal.api.model.AddProjectToGroupByIdRequest;
import com.epam.reportportal.api.model.GetLogTypes200Response;
import com.epam.reportportal.api.model.LogTypeRequest;
import com.epam.reportportal.api.model.LogTypeResponse;
import com.epam.reportportal.api.model.ProjectGroupInfo;
import com.epam.reportportal.api.model.ProjectGroupsPage;
import com.epam.reportportal.api.model.SuccessfulUpdate;
import com.epam.reportportal.base.core.group.GroupExtensionPoint;
import com.epam.reportportal.base.core.logtype.CreateLogTypeHandler;
import com.epam.reportportal.base.core.logtype.DeleteLogTypeHandler;
import com.epam.reportportal.base.core.logtype.GetLogTypeHandler;
import com.epam.reportportal.base.core.logtype.UpdateLogTypeHandler;
import com.epam.reportportal.base.core.plugin.PluginBox;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import lombok.RequiredArgsConstructor;
import org.pf4j.PluginManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling project collection-related requests.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 **/
@RestController
@RequiredArgsConstructor
public class GeneratedProjectController implements ProjectsApi {

  private final PluginManager pluginManager;
  private final PluginBox pluginBox;
  private final GetLogTypeHandler getLogTypeHandler;
  private final CreateLogTypeHandler createLogTypeHandler;
  private final UpdateLogTypeHandler updateLogTypeHandler;
  private final DeleteLogTypeHandler deleteLogTypeHandler;

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
        () -> new ReportPortalException(ErrorType.NOT_FOUND, groupId)
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
  public ResponseEntity<Void> deleteGroupFromProjectById(String projectKey, Long groupId) {
    getGroupExtension().deleteGroupFromProject(projectKey, groupId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @Override
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  public ResponseEntity<GetLogTypes200Response> getLogTypes(String projectKey) {
    return ResponseEntity.ok(getLogTypeHandler.getLogTypes(normalizeId(projectKey)));
  }

  @Override
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  public ResponseEntity<LogTypeResponse> createLogType(String projectKey, LogTypeRequest logType) {
    return new ResponseEntity<>(
        createLogTypeHandler.createLogType(projectKey, logType, getPrincipal()),
        HttpStatus.CREATED);
  }

  @Override
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  public ResponseEntity<SuccessfulUpdate> updateLogTypeById(String projectKey, Long logTypeId,
      LogTypeRequest logType) {
    return ResponseEntity.ok(
        updateLogTypeHandler.updateLogType(normalizeId(projectKey), logTypeId, logType,
            getPrincipal()));
  }

  @Override
  @PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
  public ResponseEntity<Void> deleteLogTypeById(String projectKey, Long logTypeId) {
    deleteLogTypeHandler.deleteLogType(normalizeId(projectKey), logTypeId, getPrincipal());
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  private ReportPortalUser getPrincipal() {
    return (ReportPortalUser) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();
  }

  private GroupExtensionPoint getGroupExtension() {
    return pluginBox.getInstance(GroupExtensionPoint.class)
        .orElseThrow(() -> new ReportPortalException(
            ErrorType.PAID_PLUGIN_REQUIRED,
            "Group", "Group management is not available"
        ));
  }
}
