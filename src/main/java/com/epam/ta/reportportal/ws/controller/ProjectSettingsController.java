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
import com.epam.ta.reportportal.model.EntryCreatedRS;
import com.epam.ta.reportportal.model.project.config.CreateIssueSubTypeRQ;
import com.epam.ta.reportportal.model.project.config.IssueSubTypeCreatedRS;
import com.epam.ta.reportportal.model.project.config.ProjectSettingsResource;
import com.epam.ta.reportportal.model.project.config.UpdateIssueSubTypeRQ;
import com.epam.ta.reportportal.model.project.config.pattern.CreatePatternTemplateRQ;
import com.epam.ta.reportportal.model.project.config.pattern.UpdatePatternTemplateRQ;
import com.epam.ta.reportportal.model.project.email.SenderCaseDTO;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/v1/{projectKey}/settings")
@PreAuthorize(ASSIGNED_TO_PROJECT)
@Tag(name = "project-settings-controller", description = "Project Settings Controller")
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
      UpdateProjectSettingsHandler updateHandler, DeleteProjectSettingsHandler deleteHandler,
      GetProjectSettingsHandler getHandler, GetProjectHandler getProjectHandler,
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
  @Operation(summary = "Creation of custom project specific issue sub-type")
  public IssueSubTypeCreatedRS createProjectIssueSubType(@PathVariable String projectKey,
      @RequestBody @Validated CreateIssueSubTypeRQ request,
      @AuthenticationPrincipal ReportPortalUser user) {
    return createHandler.createProjectIssueSubType(normalizeId(projectKey), user, request);
  }

  @PutMapping("/sub-type")
  @ResponseStatus(OK)
  @PreAuthorize(PROJECT_MANAGER)
  @Operation(summary = "Update of custom project specific issue sub-type")
  public OperationCompletionRS updateProjectIssueSubType(@PathVariable String projectKey,
      @RequestBody @Validated UpdateIssueSubTypeRQ request,
      @AuthenticationPrincipal ReportPortalUser user) {
    return updateHandler.updateProjectIssueSubType(normalizeId(projectKey), user, request);
  }

  @DeleteMapping("/sub-type/{id}")
  @ResponseStatus(OK)
  @PreAuthorize(PROJECT_MANAGER)
  @Operation(summary = "Delete custom project specific issue sub-type")
  public OperationCompletionRS deleteProjectIssueSubType(@PathVariable String projectKey,
      @PathVariable Long id, @AuthenticationPrincipal ReportPortalUser user) {
    return deleteHandler.deleteProjectIssueSubType(normalizeId(projectKey), user, id);
  }

  @GetMapping
  @ResponseStatus(OK)
  @PreAuthorize(ASSIGNED_TO_PROJECT)
  @Operation(summary =  "Get project specific issue sub-types", description = "Only for users that are assigned to the project")
  public ProjectSettingsResource getProjectSettings(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user) {
    return getHandler.getProjectSettings(normalizeId(projectKey));
  }

  @PostMapping("/pattern")
  @ResponseStatus(CREATED)
  @PreAuthorize(PROJECT_MANAGER)
  @Operation(summary = "Create pattern template for items' log messages pattern analysis")
  public EntryCreatedRS createPatternTemplate(@PathVariable String projectKey,
      @RequestBody @Validated CreatePatternTemplateRQ createPatternTemplateRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    return createHandler.createPatternTemplate(normalizeId(projectKey), createPatternTemplateRQ,
        user
    );
  }

  @PutMapping("/pattern/{id}")
  @ResponseStatus(OK)
  @PreAuthorize(PROJECT_MANAGER)
  @Operation(summary = "Update pattern template for items' log messages pattern analysis")
  public OperationCompletionRS updatePatternTemplate(@PathVariable String projectKey,
      @PathVariable Long id,
      @RequestBody @Validated UpdatePatternTemplateRQ updatePatternTemplateRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    return updateHandler.updatePatternTemplate(id, normalizeId(projectKey),
        updatePatternTemplateRQ, user
    );
  }

  @DeleteMapping("/pattern/{id}")
  @ResponseStatus(OK)
  @PreAuthorize(PROJECT_MANAGER)
  @Operation(summary = "Delete pattern template for items' log messages pattern analysis")
  public OperationCompletionRS deletePatternTemplate(@PathVariable String projectKey,
      @PathVariable Long id, @AuthenticationPrincipal ReportPortalUser user) {
    return deleteHandler.deletePatternTemplate(normalizeId(projectKey), user, id);
  }

  @Transactional(readOnly = true)
  @GetMapping("/notification")
  @ResponseStatus(OK)
  @PreAuthorize(ASSIGNED_TO_PROJECT)
  @Operation(summary =  "Returns notifications config of specified project", description = "Only for users assigned to specified project")
  public List<SenderCaseDTO> getNotifications(@PathVariable String projectKey) {
    return getProjectNotificationsHandler.getProjectNotifications(
        getProjectHandler.get(normalizeId(projectKey)).getId());
  }

  @Transactional
  @PostMapping("/notification")
  @ResponseStatus(CREATED)
  @PreAuthorize(PROJECT_MANAGER_OR_ADMIN)
  @Operation(summary =  "Creates notification for specified project", description = "Only for users with PROJECT_MANAGER or ADMIN roles")
  public EntryCreatedRS createNotification(@PathVariable String projectKey,
      @RequestBody @Validated SenderCaseDTO createNotificationRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    return createProjectNotificationHandler.createNotification(
        getProjectHandler.get(normalizeId(projectKey)), createNotificationRQ, user);
  }

  @Transactional
  @PutMapping("/notification")
  @ResponseStatus(CREATED)
  @PreAuthorize(PROJECT_MANAGER_OR_ADMIN)
  @Operation(summary =  "Updates notification for specified project", description = "Only for users with PROJECT_MANAGER or ADMIN roles")
  public OperationCompletionRS updateNotification(@PathVariable String projectKey,
      @RequestBody @Validated SenderCaseDTO updateNotificationRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    return updateProjectNotificationHandler.updateNotification(
        getProjectHandler.get(normalizeId(projectKey)), updateNotificationRQ, user);
  }

  @Transactional
  @DeleteMapping("/notification/{notificationId:\\d+}")
  @ResponseStatus(OK)
  @PreAuthorize(PROJECT_MANAGER_OR_ADMIN)
  @Operation(summary =  "Deletes notification for specified project", description = "Only for users with PROJECT_MANAGER or ADMIN roles")
  public OperationCompletionRS deleteNotification(@PathVariable String projectKey,
      @PathVariable Long notificationId, @AuthenticationPrincipal ReportPortalUser user) {
    return deleteNotificationHandler.deleteNotification(
        getProjectHandler.get(normalizeId(projectKey)), notificationId, user);
  }
}
