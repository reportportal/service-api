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

import static com.epam.ta.reportportal.auth.permissions.Permissions.ADMIN_ONLY;
import static com.epam.ta.reportportal.auth.permissions.Permissions.ALLOWED_TO_EDIT_USER;
import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;
import static com.epam.ta.reportportal.auth.permissions.Permissions.NOT_CUSTOMER;
import static com.epam.ta.reportportal.auth.permissions.Permissions.PROJECT_MANAGER;
import static com.epam.ta.reportportal.auth.permissions.Permissions.PROJECT_MANAGER_OR_ADMIN;
import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;
import static com.google.common.net.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.CompositeFilter;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.jasper.GetJasperReportHandler;
import com.epam.ta.reportportal.core.preference.GetPreferenceHandler;
import com.epam.ta.reportportal.core.preference.UpdatePreferenceHandler;
import com.epam.ta.reportportal.core.project.CreateProjectHandler;
import com.epam.ta.reportportal.core.project.DeleteProjectHandler;
import com.epam.ta.reportportal.core.project.GetProjectHandler;
import com.epam.ta.reportportal.core.project.GetProjectInfoHandler;
import com.epam.ta.reportportal.core.project.UpdateProjectHandler;
import com.epam.ta.reportportal.core.user.GetUserHandler;
import com.epam.ta.reportportal.entity.jasper.ReportFormat;
import com.epam.ta.reportportal.entity.project.ProjectInfo;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.model.DeleteBulkRS;
import com.epam.ta.reportportal.model.EntryCreatedRS;
import com.epam.ta.reportportal.model.preference.PreferenceResource;
import com.epam.ta.reportportal.model.project.AssignUsersRQ;
import com.epam.ta.reportportal.model.project.CreateProjectRQ;
import com.epam.ta.reportportal.model.project.ProjectInfoResource;
import com.epam.ta.reportportal.model.project.ProjectResource;
import com.epam.ta.reportportal.model.project.UnassignUsersRQ;
import com.epam.ta.reportportal.model.project.UpdateProjectRQ;
import com.epam.ta.reportportal.model.project.email.ProjectNotificationConfigDTO;
import com.epam.ta.reportportal.model.user.SearchUserResource;
import com.epam.ta.reportportal.model.user.UserResource;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import com.epam.ta.reportportal.ws.resolver.FilterCriteriaResolver;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.jooq.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Pavel Bortnik
 */
@RestController
@RequestMapping("/v1/project")
@Tag(name = "project-controller", description = "Project Controller")
public class ProjectController {

  private final ProjectExtractor projectExtractor;
  private final GetProjectHandler getProjectHandler;
  private final GetProjectInfoHandler projectInfoHandler;
  private final CreateProjectHandler createProjectHandler;
  private final UpdateProjectHandler updateProjectHandler;
  private final DeleteProjectHandler deleteProjectHandler;
  private final GetUserHandler getUserHandler;
  private final GetPreferenceHandler getPreference;
  private final UpdatePreferenceHandler updatePreference;
  private final GetJasperReportHandler<ProjectInfo> jasperReportHandler;

  @Autowired
  public ProjectController(ProjectExtractor projectExtractor, GetProjectHandler getProjectHandler,
      GetProjectInfoHandler projectInfoHandler, CreateProjectHandler createProjectHandler,
      UpdateProjectHandler updateProjectHandler, DeleteProjectHandler deleteProjectHandler,
      GetUserHandler getUserHandler, GetPreferenceHandler getPreference,
      UpdatePreferenceHandler updatePreference, @Qualifier("projectJasperReportHandler")
  GetJasperReportHandler<ProjectInfo> jasperReportHandler) {
    this.projectExtractor = projectExtractor;
    this.getProjectHandler = getProjectHandler;
    this.projectInfoHandler = projectInfoHandler;
    this.createProjectHandler = createProjectHandler;
    this.updateProjectHandler = updateProjectHandler;
    this.deleteProjectHandler = deleteProjectHandler;
    this.getUserHandler = getUserHandler;
    this.getPreference = getPreference;
    this.updatePreference = updatePreference;
    this.jasperReportHandler = jasperReportHandler;
  }

  @Transactional
  @PostMapping
  @ResponseStatus(CREATED)
  @PreAuthorize(ADMIN_ONLY)
  @Operation(summary = "Create new project")
  public EntryCreatedRS createProject(@RequestBody @Validated CreateProjectRQ createProjectRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    return createProjectHandler.createProject(createProjectRQ, user);
  }

  @Transactional
  @PutMapping("/{projectKey}")
  @ResponseStatus(OK)
  @PreAuthorize(PROJECT_MANAGER_OR_ADMIN)
  @Operation(summary =  "Update project")
  public OperationCompletionRS updateProject(@PathVariable String projectKey,
      @RequestBody @Validated UpdateProjectRQ updateProjectRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    return updateProjectHandler.updateProject(normalizeId(projectKey), updateProjectRQ, user);
  }

  @Transactional
  @PutMapping("/{projectKey}/notification")
  @ResponseStatus(OK)
  @PreAuthorize(PROJECT_MANAGER)
  @Operation(summary = "Update project notifications configuration")
  public OperationCompletionRS updateProjectNotificationConfig(@PathVariable String projectKey,
      @RequestBody @Validated ProjectNotificationConfigDTO updateProjectNotificationConfigRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    return updateProjectHandler.updateProjectNotificationConfig(normalizeId(projectKey), user,
        updateProjectNotificationConfigRQ
    );
  }

  @DeleteMapping
  @ResponseStatus(OK)
  @PreAuthorize(ADMIN_ONLY)
  @Operation(summary =  "Delete multiple projects", description = "Could be deleted only by users with administrator role")
  public DeleteBulkRS deleteProject(@RequestParam(value = "ids") List<Long> ids,
      @AuthenticationPrincipal ReportPortalUser user) {
    return deleteProjectHandler.bulkDeleteProjects(ids, user);
  }

  @DeleteMapping("/{projectId}")
  @ResponseStatus(OK)
  @PreAuthorize(ADMIN_ONLY)
  @Operation(summary =  "Delete project", description = "Could be deleted only by users with administrator role")
  public OperationCompletionRS deleteProject(@PathVariable Long projectId,
      @AuthenticationPrincipal ReportPortalUser user) {
    return deleteProjectHandler.deleteProject(projectId, user);
  }

  @DeleteMapping("/{projectKey}/index")
  @ResponseStatus(OK)
  @PreAuthorize(PROJECT_MANAGER_OR_ADMIN)
  @Operation(summary = "Delete project index from ML")
  public OperationCompletionRS deleteProjectIndex(@PathVariable String projectKey,
      Principal principal) {
    return deleteProjectHandler.deleteProjectIndex(normalizeId(projectKey), principal.getName());
  }

  @Transactional
  @PutMapping("/{projectKey}/index")
  @ResponseStatus(OK)
  @PreAuthorize(PROJECT_MANAGER_OR_ADMIN)
  @Operation(summary =  "Starts reindex all project data in ML")
  public OperationCompletionRS indexProjectData(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user) {
    return updateProjectHandler.indexProjectData(normalizeId(projectKey), user);
  }

  @Transactional(readOnly = true)
  @GetMapping("/{projectKey}/users")
  @PreAuthorize(NOT_CUSTOMER)
  @Operation(summary = "Get users assigned on current project")
  public Iterable<UserResource> getProjectUsers(@PathVariable String projectKey,
      @FilterFor(User.class) Filter filter, @SortFor(User.class) Pageable pageable,
      @AuthenticationPrincipal ReportPortalUser user) {
    return getProjectHandler.getProjectUsers(normalizeId(projectKey), filter, pageable);
  }

  @Transactional(readOnly = true)
  @GetMapping("/{projectKey}")
  @PreAuthorize(ASSIGNED_TO_PROJECT)
  @Operation(summary =  "Get information about project", description = "Only for users that are assigned to the project")
  public ProjectResource getProject(@PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user) {
    return getProjectHandler.getResource(normalizeId(projectKey), user);
  }

  @Transactional
  @PutMapping("/{projectKey}/unassign")
  @ResponseStatus(OK)
  @PreAuthorize(PROJECT_MANAGER)
  @Operation(summary = "Unassign users")
  public OperationCompletionRS unassignProjectUsers(@PathVariable String projectKey,
      @RequestBody @Validated UnassignUsersRQ unassignUsersRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    return updateProjectHandler.unassignUsers(normalizeId(projectKey), unassignUsersRQ, user);
  }

  @Transactional
  @PutMapping("/{projectKey}/assign")
  @ResponseStatus(OK)
  @PreAuthorize(PROJECT_MANAGER)
  @Operation(summary = "Assign users")
  public OperationCompletionRS assignProjectUsers(@PathVariable String projectKey,
      @RequestBody @Validated AssignUsersRQ assignUsersRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    return updateProjectHandler.assignUsers(projectKey, assignUsersRQ, user);
  }

  @Transactional(readOnly = true)
  @GetMapping("/{projectKey}/assignable")
  @ResponseStatus(OK)
  @PreAuthorize(PROJECT_MANAGER)
  @Operation(summary =  "Load users which can be assigned to specified project", description = "Only for users with project manager permissions")
  public Iterable<UserResource> getUsersForAssign(@FilterFor(User.class) Filter filter,
      @SortFor(User.class) Pageable pageable, @PathVariable String projectKey,
      @AuthenticationPrincipal ReportPortalUser user) {
    return getUserHandler.getUsers(filter, pageable,
        projectExtractor.extractProjectDetails(user, projectKey)
    );
  }

  @Transactional(readOnly = true)
  @GetMapping("/{projectKey}/usernames")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(NOT_CUSTOMER)
  @Operation(summary =  "Load project users by filter", description = "Only for users that are members of the project")
  public List<String> getProjectUsers(@PathVariable String projectKey,
      @RequestParam(value = FilterCriteriaResolver.DEFAULT_FILTER_PREFIX + Condition.CNT + "users")
      String value, @AuthenticationPrincipal ReportPortalUser user) {
    return getProjectHandler.getUserNames(projectExtractor.extractProjectDetails(user, projectKey),
        normalizeId(value)
    );
  }

  @Transactional(readOnly = true)
  @GetMapping("/{projectKey}/usernames/search")
  @ResponseStatus(OK)
  @PreAuthorize(PROJECT_MANAGER)
  public Iterable<SearchUserResource> searchForUser(@PathVariable String projectKey,
      @RequestParam(value = "term") String term, Pageable pageable,
      @AuthenticationPrincipal ReportPortalUser user) {
    return getProjectHandler.getUserNames(term,
        projectExtractor.extractProjectDetails(user, projectKey), pageable
    );
  }

  @Transactional
  @PutMapping("/{projectKey}/preference/{login}/{filterId}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(ALLOWED_TO_EDIT_USER)
  public OperationCompletionRS addUserPreference(@PathVariable String projectKey,
      @PathVariable String login, @PathVariable Long filterId,
      @AuthenticationPrincipal ReportPortalUser user) {
    return updatePreference.addPreference(projectExtractor.extractProjectDetails(user, projectKey),
        user, filterId
    );
  }

  @Transactional
  @DeleteMapping("/{projectKey}/preference/{login}/{filterId}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(ALLOWED_TO_EDIT_USER)
  public OperationCompletionRS removeUserPreference(@PathVariable String projectKey,
      @PathVariable String login, @PathVariable Long filterId,
      @AuthenticationPrincipal ReportPortalUser user) {
    return updatePreference.removePreference(
        projectExtractor.extractProjectDetails(user, projectKey), user, filterId);
  }

  @Transactional(readOnly = true)
  @GetMapping("/{projectKey}/preference/{login}")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize(ALLOWED_TO_EDIT_USER)
  @Operation(summary =  "Load user preferences", description = "Only for users that allowed to edit other users")
  public PreferenceResource getUserPreference(@PathVariable String projectKey,
      @PathVariable String login, @AuthenticationPrincipal ReportPortalUser user) {
    return getPreference.getPreference(projectExtractor.extractProjectDetails(user, projectKey),
        user
    );
  }

  @Transactional(readOnly = true)
  @PreAuthorize(ADMIN_ONLY)
  @GetMapping(value = "/list")
  @ResponseStatus(HttpStatus.OK)
  public Iterable<ProjectInfoResource> getAllProjectsInfo(
      @FilterFor(ProjectInfo.class) Filter filter,
      @FilterFor(ProjectInfo.class) Queryable predefinedFilter,
      @SortFor(ProjectInfo.class) Pageable pageable,
      @AuthenticationPrincipal ReportPortalUser user) {
    return projectInfoHandler.getAllProjectsInfo(
        new CompositeFilter(Operator.AND, filter, predefinedFilter), pageable);
  }

  @Transactional(readOnly = true)
  @PreAuthorize(ADMIN_ONLY)
  @GetMapping(value = "/export")
  @ResponseStatus(HttpStatus.OK)
  @Operation(summary =  "Exports information about all projects", description = "Allowable only for users with administrator role")
  public void exportProjects(@Parameter(schema = @Schema(allowableValues = "csv"))
  @RequestParam(value = "view", required = false, defaultValue = "csv") String view,
      @FilterFor(ProjectInfo.class) Filter filter,
      @FilterFor(ProjectInfo.class) Queryable predefinedFilter,
      @AuthenticationPrincipal ReportPortalUser user, HttpServletResponse response) {

    ReportFormat format = jasperReportHandler.getReportFormat(view);
    response.setContentType(format.getContentType());

    response.setHeader(CONTENT_DISPOSITION,
        String.format("attachment; filename=\"RP_PROJECTS_%s_Report.%s\"", format.name(),
            format.getValue()
        )
    );

    try (OutputStream outputStream = response.getOutputStream()) {
      getProjectHandler.exportProjects(format,
          new CompositeFilter(Operator.AND, filter, predefinedFilter), outputStream
      );
    } catch (IOException e) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "Unable to write data to the response."
      );
    }

  }

  @Transactional(readOnly = true)
  @PreAuthorize(ASSIGNED_TO_PROJECT)
  @GetMapping("/list/{projectKey}")
  @ResponseStatus(HttpStatus.OK)
  public ProjectInfoResource getProjectInfo(@PathVariable String projectKey,
      @RequestParam(value = "interval", required = false, defaultValue = "3M") String interval,
      @AuthenticationPrincipal ReportPortalUser user) {
    return projectInfoHandler.getProjectInfo(projectKey, interval);
  }

  @Transactional(readOnly = true)
  @PreAuthorize(ASSIGNED_TO_PROJECT)
  @GetMapping("/{projectKey}/widget/{widgetCode}")
  @ResponseStatus(HttpStatus.OK)
  public Map<String, ?> getProjectWidget(@PathVariable String projectKey,
      @RequestParam(value = "interval", required = false, defaultValue = "3M") String interval,
      @PathVariable String widgetCode, @AuthenticationPrincipal ReportPortalUser user) {
    return projectInfoHandler.getProjectInfoWidgetContent(projectKey, interval, widgetCode);
  }

  @Transactional(readOnly = true)
  @PreAuthorize(ADMIN_ONLY)
  @GetMapping(value = "/names")
  @ResponseStatus(HttpStatus.OK)
  public Iterable<String> getAllProjectNames(@AuthenticationPrincipal ReportPortalUser user) {
    return getProjectHandler.getAllProjectNames();
  }

  @Transactional(readOnly = true)
  @PreAuthorize(ADMIN_ONLY)
  @GetMapping(value = "/names/search")
  @ResponseStatus(HttpStatus.OK)
  public Iterable<String> searchProjectNames(@RequestParam("term") String term,
      @AuthenticationPrincipal ReportPortalUser user) {
    return getProjectHandler.getAllProjectNamesByTerm(term);
  }

  @Transactional(readOnly = true)
  @PreAuthorize(ADMIN_ONLY)
  @GetMapping("analyzer/status")
  @ResponseBody
  @ResponseStatus(HttpStatus.OK)
  public Map<String, Boolean> getAnalyzerIndexingStatus(
      @AuthenticationPrincipal ReportPortalUser user) {
    return getProjectHandler.getAnalyzerIndexingStatus();
  }

}
