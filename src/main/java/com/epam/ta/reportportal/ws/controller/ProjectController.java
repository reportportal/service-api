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
import com.epam.ta.reportportal.entity.project.ProjectInfo;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.model.*;
import com.epam.ta.reportportal.ws.model.preference.PreferenceResource;
import com.epam.ta.reportportal.ws.model.project.*;
import com.epam.ta.reportportal.ws.model.project.email.ProjectNotificationConfigDTO;
import com.epam.ta.reportportal.ws.model.user.SearchUserResource;
import com.epam.ta.reportportal.ws.model.user.UserResource;
import com.epam.ta.reportportal.ws.resolver.FilterCriteriaResolver;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.jooq.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.auth.permissions.Permissions.*;
import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;
import static com.epam.ta.reportportal.ws.converter.converters.ExceptionConverter.TO_ERROR_RS;
import static com.google.common.net.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

/**
 * @author Pavel Bortnik
 */
@RestController
@RequestMapping("/v1/project")
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
	public ProjectController(ProjectExtractor projectExtractor, GetProjectHandler getProjectHandler, GetProjectInfoHandler projectInfoHandler,
			CreateProjectHandler createProjectHandler, UpdateProjectHandler updateProjectHandler, DeleteProjectHandler deleteProjectHandler,
			GetUserHandler getUserHandler, GetPreferenceHandler getPreference, UpdatePreferenceHandler updatePreference,
			@Qualifier("projectJasperReportHandler") GetJasperReportHandler<ProjectInfo> jasperReportHandler) {
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
		return updateProjectHandler.updateProject(normalizeId(projectName), updateProjectRQ, user);
	}

	@Transactional
	@PutMapping("/{projectName}/notification")
	@ResponseStatus(OK)
	@PreAuthorize(PROJECT_MANAGER)
	@ApiOperation("Update project notifications configuration")
	public OperationCompletionRS updateProjectNotificationConfig(@PathVariable String projectName,
			@RequestBody @Validated ProjectNotificationConfigDTO updateProjectNotificationConfigRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		return updateProjectHandler.updateProjectNotificationConfig(normalizeId(projectName), user, updateProjectNotificationConfigRQ);
	}

	@DeleteMapping
	@ResponseStatus(OK)
	@PreAuthorize(ADMIN_ONLY)
	@ApiOperation(value = "Delete multiple projects", notes = "Could be deleted only by users with administrator role")
	public DeleteBulkRS deleteProject(@RequestBody @Valid DeleteBulkRQ deleteBulkRQ, @AuthenticationPrincipal ReportPortalUser user) {
		final List<ReportPortalException> exceptions = Lists.newArrayList();
		final List<Long> deleted = Lists.newArrayList();
		deleteBulkRQ.getIds().forEach(projectId -> {
			try {
				deleteProjectHandler.deleteProject(projectId);
				deleted.add(projectId);
			} catch (ReportPortalException ex) {
				exceptions.add(ex);
			}
		});
		return new DeleteBulkRS(deleted, Collections.emptyList(), exceptions.stream().map(TO_ERROR_RS).collect(Collectors.toList()));
	}

	@DeleteMapping("/{projectId}")
	@ResponseStatus(OK)
	@PreAuthorize(ADMIN_ONLY)
	@ApiOperation(value = "Delete project", notes = "Could be deleted only by users with administrator role")
	public OperationCompletionRS deleteProject(@PathVariable Long projectId, @AuthenticationPrincipal ReportPortalUser user) {
		return deleteProjectHandler.deleteProject(projectId);
	}

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
		return updateProjectHandler.indexProjectData(normalizeId(projectName), user);
	}

	@Transactional(readOnly = true)
	@GetMapping("/{projectName}/users")
	@PreAuthorize(NOT_CUSTOMER)
	@ApiOperation("Get users assigned on current project")
	public Iterable<UserResource> getProjectUsers(@PathVariable String projectName, @FilterFor(User.class) Filter filter,
			@SortFor(User.class) Pageable pageable, @AuthenticationPrincipal ReportPortalUser user) {
		return getProjectHandler.getProjectUsers(normalizeId(projectName), filter, pageable);
	}

	@Transactional(readOnly = true)
	@GetMapping("/{projectName}")
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	@ApiOperation(value = "Get information about project", notes = "Only for users that are assigned to the project")
	public ProjectResource getProject(@PathVariable String projectName, @AuthenticationPrincipal ReportPortalUser user) {
		return getProjectHandler.getProject(normalizeId(projectName), user);
	}

	@Transactional
	@PutMapping("/{projectName}/unassign")
	@ResponseStatus(OK)
	@PreAuthorize(PROJECT_MANAGER)
	@ApiOperation("Un assign users")
	public OperationCompletionRS unassignProjectUsers(@PathVariable String projectName,
			@RequestBody @Validated UnassignUsersRQ unassignUsersRQ, @AuthenticationPrincipal ReportPortalUser user) {
		return updateProjectHandler.unassignUsers(normalizeId(projectName), unassignUsersRQ, user);
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
		return getUserHandler.getUsers(filter, pageable, projectExtractor.extractProjectDetails(user, projectName));
	}

	@Transactional(readOnly = true)
	@GetMapping("/{projectName}/usernames")
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize(NOT_CUSTOMER)
	@ApiOperation(value = "Load project users by filter", notes = "Only for users that are members of the project")
	public List<String> getProjectUsers(@PathVariable String projectName,
			@RequestParam(value = FilterCriteriaResolver.DEFAULT_FILTER_PREFIX + Condition.CNT + "users") String value,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getProjectHandler.getUserNames(projectExtractor.extractProjectDetails(user, projectName), normalizeId(value));
	}

	@Transactional(readOnly = true)
	@GetMapping("/{projectName}/usernames/search")
	@ResponseStatus(OK)
	@PreAuthorize(PROJECT_MANAGER)
	public Iterable<SearchUserResource> searchForUser(@PathVariable String projectName, @RequestParam(value = "term") String term,
			Pageable pageable, @AuthenticationPrincipal ReportPortalUser user) {
		return getProjectHandler.getUserNames(term, projectExtractor.extractProjectDetails(user, projectName), pageable);
	}

	@Transactional
	@PutMapping("/{projectName}/preference/{login}/{filterId}")
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize(ALLOWED_TO_EDIT_USER)
	public OperationCompletionRS addUserPreference(@PathVariable String projectName, @PathVariable String login,
			@PathVariable Long filterId, @AuthenticationPrincipal ReportPortalUser user) {
		return updatePreference.addPreference(projectExtractor.extractProjectDetails(user, projectName), user, filterId);
	}

	@Transactional
	@DeleteMapping("/{projectName}/preference/{login}/{filterId}")
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize(ALLOWED_TO_EDIT_USER)
	public OperationCompletionRS removeUserPreference(@PathVariable String projectName, @PathVariable String login,
			@PathVariable Long filterId, @AuthenticationPrincipal ReportPortalUser user) {
		return updatePreference.removePreference(projectExtractor.extractProjectDetails(user, projectName), user, filterId);
	}

	@Transactional(readOnly = true)
	@GetMapping("/{projectName}/preference/{login}")
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize(ALLOWED_TO_EDIT_USER)
	@ApiOperation(value = "Load user preferences", notes = "Only for users that allowed to edit other users")
	public PreferenceResource getUserPreference(@PathVariable String projectName, @PathVariable String login,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getPreference.getPreference(projectExtractor.extractProjectDetails(user, projectName), user);
	}

	@Transactional(readOnly = true)
	@PreAuthorize(ADMIN_ONLY)
	@GetMapping(value = "/list")
	@ResponseStatus(HttpStatus.OK)
	public Iterable<ProjectInfoResource> getAllProjectsInfo(@FilterFor(ProjectInfo.class) Filter filter,
			@FilterFor(ProjectInfo.class) Queryable predefinedFilter, @SortFor(ProjectInfo.class) Pageable pageable,
			@AuthenticationPrincipal ReportPortalUser user) {
		return projectInfoHandler.getAllProjectsInfo(new CompositeFilter(Operator.AND, filter, predefinedFilter), pageable);
	}

	@Transactional(readOnly = true)
	@PreAuthorize(ADMIN_ONLY)
	@GetMapping(value = "/export")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(value = "Exports information about all projects", notes = "Allowable only for users with administrator role")
	public void exportProjects(
			@ApiParam(allowableValues = "csv") @RequestParam(value = "view", required = false, defaultValue = "csv") String view,
			@FilterFor(ProjectInfo.class) Filter filter, @FilterFor(ProjectInfo.class) Queryable predefinedFilter,
			@AuthenticationPrincipal ReportPortalUser user, HttpServletResponse response) {

		ReportFormat format = jasperReportHandler.getReportFormat(view);
		response.setContentType(format.getContentType());

		response.setHeader(CONTENT_DISPOSITION,
				String.format("attachment; filename=RP_PROJECTS_%s_Report.%s", format.name(), format.getValue())
		);

		try (OutputStream outputStream = response.getOutputStream()) {
			getProjectHandler.exportProjects(format, new CompositeFilter(Operator.AND, filter, predefinedFilter), outputStream);
		} catch (IOException e) {
			throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Unable to write data to the response.");
		}

	}

	@Transactional(readOnly = true)
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	@GetMapping("/list/{projectName}")
	@ResponseStatus(HttpStatus.OK)
	public ProjectInfoResource getProjectInfo(@PathVariable String projectName,
			@RequestParam(value = "interval", required = false, defaultValue = "3M") String interval,
			@AuthenticationPrincipal ReportPortalUser user) {
		return projectInfoHandler.getProjectInfo(projectName, interval);
	}

	@Transactional(readOnly = true)
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	@GetMapping("/{projectName}/widget/{widgetCode}")
	@ResponseStatus(HttpStatus.OK)
	public Map<String, ?> getProjectWidget(@PathVariable String projectName,
			@RequestParam(value = "interval", required = false, defaultValue = "3M") String interval, @PathVariable String widgetCode,
			@AuthenticationPrincipal ReportPortalUser user) {
		return projectInfoHandler.getProjectInfoWidgetContent(projectName, interval, widgetCode);
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
	public Iterable<String> searchProjectNames(@RequestParam("term") String term, @AuthenticationPrincipal ReportPortalUser user) {
		return getProjectHandler.getAllProjectNamesByTerm(term);
	}

	@Transactional(readOnly = true)
	@PreAuthorize(ADMIN_ONLY)
	@GetMapping("analyzer/status")
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	public Map<String, Boolean> getAnalyzerIndexingStatus(@AuthenticationPrincipal ReportPortalUser user) {
		return getProjectHandler.getAnalyzerIndexingStatus();
	}

}
