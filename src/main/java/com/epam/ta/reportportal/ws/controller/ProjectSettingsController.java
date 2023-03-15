/*
 * Copyright 2019 EPAM Systems
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

import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;
import static com.epam.ta.reportportal.auth.permissions.Permissions.PROJECT_MANAGER;
import static com.epam.ta.reportportal.auth.permissions.Permissions.PROJECT_MANAGER_OR_ADMIN;
import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.project.GetProjectHandler;
import com.epam.ta.reportportal.core.project.settings.CreateProjectSettingsHandler;
import com.epam.ta.reportportal.core.project.settings.DeleteProjectSettingsHandler;
import com.epam.ta.reportportal.core.project.settings.GetProjectSettingsHandler;
import com.epam.ta.reportportal.core.project.settings.UpdateProjectSettingsHandler;
import com.epam.ta.reportportal.core.project.settings.notification.CreateProjectNotificationHandler;
import com.epam.ta.reportportal.core.project.settings.notification.DeleteProjectNotificationHandler;
import com.epam.ta.reportportal.core.project.settings.notification.GetProjectNotificationsHandler;
import com.epam.ta.reportportal.core.project.settings.notification.UpdateProjectNotificationHandler;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.project.config.CreateIssueSubTypeRQ;
import com.epam.ta.reportportal.ws.model.project.config.IssueSubTypeCreatedRS;
import com.epam.ta.reportportal.ws.model.project.config.ProjectSettingsResource;
import com.epam.ta.reportportal.ws.model.project.config.UpdateIssueSubTypeRQ;
import com.epam.ta.reportportal.ws.model.project.config.pattern.CreatePatternTemplateRQ;
import com.epam.ta.reportportal.ws.model.project.config.pattern.UpdatePatternTemplateRQ;
import com.epam.ta.reportportal.ws.model.project.email.SenderCaseDTO;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Projects settings controller. Provides resources for manipulation of various project settings
 * items.
 *
 * @author Andrei_Ramanchuk
 */
@RestController
@RequestMapping("/v1/{projectName}/settings")
@PreAuthorize(ASSIGNED_TO_PROJECT)
public class ProjectSettingsController {

  private final CreateProjectSettingsHandler createHandler;

  private final UpdateProjectSettingsHandler updateHandler;

  private final DeleteProjectSettingsHandler deleteHandler;

  private final GetProjectSettingsHandler getHandler;

  private final GetProjectHandler getProjectHandler;

  private final GetProjectNotificationsHandler getProjectNotificationsHandler;

  private final CreateProjectNotificationHandler createProjectNotificationHandler;

  private final UpdateProjectNotificationHandler updateProjectNotificationHandler;

  private final DeleteProjectNotificationHandler deleteNotificationHandler;

  @Autowired
  public ProjectSettingsController(CreateProjectSettingsHandler createHandler,
      UpdateProjectSettingsHandler updateHandler,
      DeleteProjectSettingsHandler deleteHandler, GetProjectSettingsHandler getHandler,
      GetProjectHandler getProjectHandler,
      GetProjectNotificationsHandler getProjectNotificationsHandler,
      CreateProjectNotificationHandler createProjectNotificationHandler,
      UpdateProjectNotificationHandler updateProjectNotificationHandler,
      DeleteProjectNotificationHandler deleteNotificationHandler) {
    this.createHandler = createHandler;
    this.updateHandler = updateHandler;
    this.deleteHandler = deleteHandler;
    this.getHandler = getHandler;
    this.getProjectHandler = getProjectHandler;
    this.getProjectNotificationsHandler = getProjectNotificationsHandler;
    this.createProjectNotificationHandler = createProjectNotificationHandler;
    this.updateProjectNotificationHandler = updateProjectNotificationHandler;
    this.deleteNotificationHandler = deleteNotificationHandler;
  }

  @PostMapping("/sub-type")
  @ResponseStatus(CREATED)
  @PreAuthorize(PROJECT_MANAGER)
  @ApiOperation("Creation of custom project specific issue sub-type")
  public IssueSubTypeCreatedRS createProjectIssueSubType(@PathVariable String projectName,
      @RequestBody @Validated CreateIssueSubTypeRQ request,
      @AuthenticationPrincipal ReportPortalUser user) {
    return createHandler.createProjectIssueSubType(normalizeId(projectName), user, request);
  }

  @PutMapping("/sub-type")
  @ResponseStatus(OK)
  @PreAuthorize(PROJECT_MANAGER)
  @ApiOperation("Update of custom project specific issue sub-type")
  public OperationCompletionRS updateProjectIssueSubType(@PathVariable String projectName,
      @RequestBody @Validated UpdateIssueSubTypeRQ request,
      @AuthenticationPrincipal ReportPortalUser user) {
    return updateHandler.updateProjectIssueSubType(normalizeId(projectName), user, request);
  }

  @DeleteMapping("/sub-type/{id}")
  @ResponseStatus(OK)
  @PreAuthorize(PROJECT_MANAGER)
  @ApiOperation("Delete custom project specific issue sub-type")
  public OperationCompletionRS deleteProjectIssueSubType(@PathVariable String projectName,
      @PathVariable Long id,
      @AuthenticationPrincipal ReportPortalUser user) {
    return deleteHandler.deleteProjectIssueSubType(normalizeId(projectName), user, id);
  }

  @GetMapping
  @ResponseStatus(OK)
  @PreAuthorize(ASSIGNED_TO_PROJECT)
  @ApiOperation(value = "Get project specific issue sub-types", notes = "Only for users that are assigned to the project")
  public ProjectSettingsResource getProjectSettings(@PathVariable String projectName,
      @AuthenticationPrincipal ReportPortalUser user) {
    return getHandler.getProjectSettings(normalizeId(projectName));
  }

  @PostMapping("/pattern")
  @ResponseStatus(CREATED)
  @PreAuthorize(PROJECT_MANAGER)
  @ApiOperation("Create pattern template for items' log messages pattern analysis")
  public EntryCreatedRS createPatternTemplate(@PathVariable String projectName,
      @RequestBody @Validated CreatePatternTemplateRQ createPatternTemplateRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    return createHandler.createPatternTemplate(normalizeId(projectName), createPatternTemplateRQ,
        user);
  }

  @PutMapping("/pattern/{id}")
  @ResponseStatus(OK)
  @PreAuthorize(PROJECT_MANAGER)
  @ApiOperation("Update pattern template for items' log messages pattern analysis")
  public OperationCompletionRS updatePatternTemplate(@PathVariable String projectName,
      @PathVariable Long id,
      @RequestBody @Validated UpdatePatternTemplateRQ updatePatternTemplateRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    return updateHandler.updatePatternTemplate(id, normalizeId(projectName),
        updatePatternTemplateRQ, user);
  }

  @DeleteMapping("/pattern/{id}")
  @ResponseStatus(OK)
  @PreAuthorize(PROJECT_MANAGER)
  @ApiOperation("Delete pattern template for items' log messages pattern analysis")
  public OperationCompletionRS deletePatternTemplate(@PathVariable String projectName,
      @PathVariable Long id,
      @AuthenticationPrincipal ReportPortalUser user) {
    return deleteHandler.deletePatternTemplate(normalizeId(projectName), user, id);
  }

  @Transactional(readOnly = true)
  @GetMapping("/notification")
  @ResponseStatus(OK)
  @PreAuthorize(ASSIGNED_TO_PROJECT)
  @ApiOperation(value = "Returns notifications config of specified project", notes = "Only for users assigned to specified project")
  public List<SenderCaseDTO> getNotifications(@PathVariable String projectName) {
    return getProjectNotificationsHandler.getProjectNotifications(
        getProjectHandler.get(normalizeId(projectName)).getId());
  }

  @Transactional
  @PostMapping("/notification")
  @ResponseStatus(CREATED)
  @PreAuthorize(PROJECT_MANAGER_OR_ADMIN)
  @ApiOperation(value = "Creates notification for specified project", notes = "Only for users with PROJECT_MANAGER or ADMIN roles")
  public EntryCreatedRS createNotification(@PathVariable String projectName,
      @RequestBody @Validated SenderCaseDTO createNotificationRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    return createProjectNotificationHandler.createNotification(
        getProjectHandler.get(normalizeId(projectName)),
        createNotificationRQ,
        user
    );
  }

  @Transactional
  @PutMapping("/notification")
  @ResponseStatus(CREATED)
  @PreAuthorize(PROJECT_MANAGER_OR_ADMIN)
  @ApiOperation(value = "Updates notification for specified project", notes = "Only for users with PROJECT_MANAGER or ADMIN roles")
  public OperationCompletionRS updateNotification(@PathVariable String projectName,
      @RequestBody @Validated SenderCaseDTO updateNotificationRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    return updateProjectNotificationHandler.updateNotification(
        getProjectHandler.get(normalizeId(projectName)),
        updateNotificationRQ,
        user
    );
  }

  @Transactional
  @DeleteMapping("/notification/{notificationId:\\d+}")
  @ResponseStatus(OK)
  @PreAuthorize(PROJECT_MANAGER_OR_ADMIN)
  @ApiOperation(value = "Deletes notification for specified project", notes = "Only for users with PROJECT_MANAGER or ADMIN roles")
  public OperationCompletionRS deleteNotification(@PathVariable String projectName,
      @PathVariable Long notificationId,
      @AuthenticationPrincipal ReportPortalUser user) {
    return deleteNotificationHandler.deleteNotification(
        getProjectHandler.get(normalizeId(projectName)), notificationId, user);
  }
}