/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.CompositeFilter;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.jasper.GetJasperReportHandler;
import com.epam.ta.reportportal.core.preference.GetPreferenceHandler;
import com.epam.ta.reportportal.core.preference.UpdatePreferenceHandler;
import com.epam.ta.reportportal.core.project.*;
import com.epam.ta.reportportal.core.user.GetUserHandler;
import com.epam.ta.reportportal.entity.jasper.ReportFormat;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectInfo;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.model.BulkRQ;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.preference.PreferenceResource;
import com.epam.ta.reportportal.ws.model.project.*;
import com.epam.ta.reportportal.ws.model.project.email.ProjectNotificationConfigDTO;
import com.epam.ta.reportportal.ws.model.user.UserResource;
import com.epam.ta.reportportal.ws.resolver.FilterCriteriaResolver;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.auth.permissions.Permissions.*;
import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

/**
 * @author Pavel Bortnik
 */
@RestController
@RequestMapping("/project")
public class ProjectController {

	private final GetProjectHandler getProjectHandler;
	private final GetProjectInfoHandler projectInfoHandler;
	private final CreateProjectHandler createProjectHandler;
	private final UpdateProjectHandler updateProjectHandler;
	private final DeleteProjectHandler deleteProjectHandler;
	private final GetUserHandler getUserHandler;
	private final GetPreferenceHandler getPreference;
	private final UpdatePreferenceHandler updatePreference;
	private final GetJasperReportHandler<Project> jasperReportHandler;

	@Autowired
	public ProjectController(GetProjectHandler getProjectHandler, GetProjectInfoHandler projectInfoHandler,
			CreateProjectHandler createProjectHandler, UpdateProjectHandler updateProjectHandler, DeleteProjectHandler deleteProjectHandler,
			GetUserHandler getUserHandler, GetPreferenceHandler getPreference, UpdatePreferenceHandler updatePreference,
			@Qualifier("projectJasperReportHandler") GetJasperReportHandler<Project> jasperReportHandler) {
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
	@ApiOperation("Create new project")
	public EntryCreatedRS createProject(@RequestBody @Validated CreateProjectRQ createProjectRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		return createProjectHandler.createProject(createProjectRQ, user);
	}

	@Transactional
	@PutMapping("/{projectName}")
	@ResponseStatus(OK)
	@PreAuthorize(PROJECT_MANAGER_OR_ADMIN)
	@ApiOperation(value = "Update project")
	public OperationCompletionRS updateProject(@PathVariable String projectName, @RequestBody @Validated UpdateProjectRQ updateProjectRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		return updateProjectHandler.updateProject(ProjectExtractor.extractProjectDetails(user, projectName), updateProjectRQ, user);
	}

	@Transactional
	@PutMapping("/{projectName}/notification")
	@ResponseStatus(OK)
	@PreAuthorize(PROJECT_MANAGER)
	@ApiOperation("Update project notifications configuration")
	public OperationCompletionRS updateProjectNotificationConfig(@PathVariable String projectName,
			@RequestBody @Validated ProjectNotificationConfigDTO updateProjectNotificationConfigRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		return updateProjectHandler.updateProjectNotificationConfig(ProjectExtractor.extractProjectDetails(user, projectName),
				user,
				updateProjectNotificationConfigRQ
		);
	}

	@Transactional
	@DeleteMapping
	@ResponseStatus(OK)
	@PreAuthorize(ADMIN_ONLY)
	@ApiOperation(value = "Delete multiple projects", notes = "Could be deleted only by users with administrator role")
	public Iterable<OperationCompletionRS> deleteProject(@RequestBody @Validated BulkRQ<DeleteProjectRQ> deleteProjectsBulkRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		return deleteProjectHandler.deleteProjects(deleteProjectsBulkRQ);
	}

	@Transactional
	@DeleteMapping("/{projectName}")
	@ResponseStatus(OK)
	@PreAuthorize(ADMIN_ONLY)
	@ApiOperation(value = "Delete project", notes = "Could be deleted only by users with administrator role")
	public OperationCompletionRS deleteProject(@PathVariable String projectName, @AuthenticationPrincipal ReportPortalUser user) {
		return deleteProjectHandler.deleteProject(normalizeId(projectName));
	}

	@Transactional
	@DeleteMapping("/{projectName}/index")
	@ResponseStatus(OK)
	@PreAuthorize(PROJECT_MANAGER_OR_ADMIN)
	@ApiOperation("Delete project index from ML")
	public OperationCompletionRS deleteProjectIndex(@PathVariable String projectName, Principal principal) {
		return deleteProjectHandler.deleteProjectIndex(normalizeId(projectName), principal.getName());
	}

	@Transactional
	@PutMapping("/{projectName}/index")
	@ResponseStatus(OK)
	@PreAuthorize(PROJECT_MANAGER_OR_ADMIN)
	@ApiOperation(value = "Starts reindex all project data in ML")
	public OperationCompletionRS indexProjectData(@PathVariable String projectName, @AuthenticationPrincipal ReportPortalUser user) {
		return updateProjectHandler.indexProjectData(ProjectExtractor.extractProjectDetails(user, projectName), user);
	}

	@Transactional(readOnly = true)
	@GetMapping("/{projectName}/users")
	@PreAuthorize(NOT_CUSTOMER)
	@ApiOperation("Get users assigned on current project")
	public Iterable<UserResource> getProjectUsers(@PathVariable String projectName, @FilterFor(User.class) Filter filter,
			@SortFor(User.class) Pageable pageable, @AuthenticationPrincipal ReportPortalUser user) {
		return getProjectHandler.getProjectUsers(ProjectExtractor.extractProjectDetails(user, projectName), filter, pageable);
	}

	@Transactional(readOnly = true)
	@GetMapping("/{projectName}")
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	@ApiOperation(value = "Get information about project", notes = "Only for users that are assigned to the project")
	public ProjectResource getProject(@PathVariable String projectName, @AuthenticationPrincipal ReportPortalUser user) {
		return getProjectHandler.getProject(EntityUtils.normalizeId(projectName), user);
	}

	@Transactional
	@PutMapping("/{projectName}/unassign")
	@ResponseStatus(OK)
	@PreAuthorize(PROJECT_MANAGER)
	@ApiOperation("Un assign users")
	public OperationCompletionRS unassignProjectUsers(@PathVariable String projectName,
			@RequestBody @Validated UnassignUsersRQ unassignUsersRQ, @AuthenticationPrincipal ReportPortalUser user) {
		return updateProjectHandler.unassignUsers(ProjectExtractor.extractProjectDetails(user, projectName), unassignUsersRQ, user);
	}

	@Transactional
	@PutMapping("/{projectName}/assign")
	@ResponseStatus(OK)
	@PreAuthorize(PROJECT_MANAGER)
	@ApiOperation("Assign users")
	public OperationCompletionRS assignProjectUsers(@PathVariable String projectName, @RequestBody @Validated AssignUsersRQ assignUsersRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		return updateProjectHandler.assignUsers(projectName, assignUsersRQ, user);
	}

	@Transactional(readOnly = true)
	@GetMapping("/{projectName}/assignable")
	@ResponseStatus(OK)
	@PreAuthorize(PROJECT_MANAGER)
	@ApiOperation(value = "Load users which can be assigned to specified project", notes = "Only for users with project manager permissions")
	public Iterable<UserResource> getUsersForAssign(@FilterFor(User.class) Filter filter, @SortFor(User.class) Pageable pageable,
			@PathVariable String projectName, @AuthenticationPrincipal ReportPortalUser user) {
		return getUserHandler.getUsers(filter, pageable, ProjectExtractor.extractProjectDetails(user, projectName));
	}

	@Transactional(readOnly = true)
	@GetMapping("/{projectName}/usernames")
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize(NOT_CUSTOMER)
	@ApiOperation(value = "Load project users by filter", notes = "Only for users that are members of the project")
	public List<String> getProjectUsers(@PathVariable String projectName,
			@RequestParam(value = FilterCriteriaResolver.DEFAULT_FILTER_PREFIX + Condition.CNT + "users") String value,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getProjectHandler.getUserNames(ProjectExtractor.extractProjectDetails(user, projectName), normalizeId(value));
	}

	@Transactional(readOnly = true)
	@GetMapping("/{projectName}/usernames/search")
	@ResponseStatus(OK)
	@ApiIgnore
	@PreAuthorize(PROJECT_MANAGER)
	public Iterable<UserResource> searchForUser(@PathVariable String projectName, @RequestParam(value = "term") String term,
			Pageable pageable, @AuthenticationPrincipal ReportPortalUser user) {
		ProjectExtractor.extractProjectDetails(user, projectName);
		return getProjectHandler.getUserNames(term, pageable);
	}

	@Transactional
	@PutMapping("/{projectName}/preference/{login}/{filterId}")
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize(ALLOWED_TO_EDIT_USER)
	public OperationCompletionRS addUserPreference(@PathVariable String projectName, @PathVariable String login,
			@PathVariable Long filterId, @AuthenticationPrincipal ReportPortalUser user) {
		return updatePreference.addPreference(ProjectExtractor.extractProjectDetails(user, projectName), user, filterId);
	}

	@Transactional
	@DeleteMapping("/{projectName}/preference/{login}/{filterId}")
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize(ALLOWED_TO_EDIT_USER)
	public OperationCompletionRS removeUserPreference(@PathVariable String projectName, @PathVariable String login,
			@PathVariable Long filterId, @AuthenticationPrincipal ReportPortalUser user) {
		return updatePreference.removePreference(ProjectExtractor.extractProjectDetails(user, projectName), user, filterId);
	}

	@Transactional(readOnly = true)
	@GetMapping("/{projectName}/preference/{login}")
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize(ALLOWED_TO_EDIT_USER)
	@ApiOperation(value = "Load user preferences", notes = "Only for users that allowed to edit other users")
	public PreferenceResource getUserPreference(@PathVariable String projectName, @PathVariable String login,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getPreference.getPreference(ProjectExtractor.extractProjectDetails(user, projectName), user);
	}

	@Transactional(readOnly = true)
	@PreAuthorize(ADMIN_ONLY)
	@GetMapping(value = "/list")
	@ResponseStatus(HttpStatus.OK)
	@ApiIgnore
	public Iterable<ProjectInfoResource> getAllProjectsInfo(@FilterFor(ProjectInfo.class) Filter filter,
			@FilterFor(ProjectInfo.class) Queryable predefinedFilter, @SortFor(ProjectInfo.class) Pageable pageable,
			@AuthenticationPrincipal ReportPortalUser user) {
		return projectInfoHandler.getAllProjectsInfo(new CompositeFilter(filter, predefinedFilter), pageable);
	}

	@Transactional(readOnly = true)
	@PreAuthorize(ADMIN_ONLY)
	@GetMapping(value = "/export")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Exports information about all projects", notes = "Allowable only for users with administrator role")
	public void exportProjects(
			@ApiParam(allowableValues = "csv") @RequestParam(value = "view", required = false, defaultValue = "csv") String view,
			@FilterFor(Project.class) Filter filter, @AuthenticationPrincipal ReportPortalUser user, HttpServletResponse response) {

		ReportFormat format = jasperReportHandler.getReportFormat(view);
		response.setContentType(format.getContentType());

		response.setHeader(com.google.common.net.HttpHeaders.CONTENT_DISPOSITION,
				String.format("attachment; filename=RP_PROJECTS_%s_Report.%s", format.name(), format.getValue())
		);

		try (OutputStream outputStream = response.getOutputStream()) {
			getProjectHandler.exportProjects(format, filter, outputStream);
		} catch (IOException e) {
			throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Unable to write data to the response.");
		}

	}

	@Transactional(readOnly = true)
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	@GetMapping("/list/{projectName}")
	@ResponseStatus(HttpStatus.OK)
	@ApiIgnore
	public ProjectInfoResource getProjectInfo(@PathVariable String projectName,
			@RequestParam(value = "interval", required = false, defaultValue = "3M") String interval,
			@AuthenticationPrincipal ReportPortalUser user) {
		return projectInfoHandler.getProjectInfo(projectName, interval);
	}

	@Transactional(readOnly = true)
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	@GetMapping("/{projectName}/widget/{widgetCode}")
	@ResponseStatus(HttpStatus.OK)
	@ApiIgnore
	public Map<String, ?> getProjectWidget(@PathVariable String projectName,
			@RequestParam(value = "interval", required = false, defaultValue = "3M") String interval, @PathVariable String widgetCode,
			@AuthenticationPrincipal ReportPortalUser user) {
		return projectInfoHandler.getProjectInfoWidgetContent(projectName, interval, widgetCode);
	}

	@Transactional(readOnly = true)
	@PreAuthorize(ADMIN_ONLY)
	@GetMapping(value = "/names")
	@ResponseStatus(HttpStatus.OK)
	@ApiIgnore
	public Iterable<String> getAllProjectNames(@AuthenticationPrincipal ReportPortalUser user) {
		return getProjectHandler.getAllProjectNames();
	}

	@Transactional(readOnly = true)
	@PreAuthorize(ADMIN_ONLY)
	@GetMapping(value = "/names/search")
	@ResponseStatus(HttpStatus.OK)
	public Iterable<String> searchProjectNames(@RequestParam("term") String term, @AuthenticationPrincipal ReportPortalUser user) {
		return getProjectHandler.getAllProjectNamesByTerm(term);
	}

	@Transactional(readOnly = true)
	@PreAuthorize(ADMIN_ONLY)
	@GetMapping("analyzer/status")
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiIgnore
	public Map<String, Boolean> getAnalyzerIndexingStatus(@AuthenticationPrincipal ReportPortalUser user) {
		return getProjectHandler.getAnalyzerIndexingStatus();
	}

}
