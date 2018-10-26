/*
 * Copyright (C) 2018 EPAM Systems
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

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.filter.IShareUserFilterHandler;
import com.epam.ta.reportportal.core.project.*;
import com.epam.ta.reportportal.core.user.GetUserHandler;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.util.ProjectUtils;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.project.*;
import com.epam.ta.reportportal.ws.model.user.UserResource;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.auth.permissions.Permissions.*;
import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/project")
public class ProjectController {

	private final IGetProjectHandler projectHandler;
	private final IGetProjectInfoHandler projectInfoHandler;
	private final ICreateProjectHandler createProjectHandler;
	private final IUpdateProjectHandler updateProjectHandler;
	private final IDeleteProjectHandler deleteProjectHandler;
	private final IShareUserFilterHandler shareFilterHandler;
	private final GetUserHandler getUserHandler;

	@Autowired
	public ProjectController(IGetProjectHandler projectHandler, IGetProjectInfoHandler projectInfoHandler,
			ICreateProjectHandler createProjectHandler, IUpdateProjectHandler updateProjectHandler,
			IDeleteProjectHandler deleteProjectHandler, IShareUserFilterHandler shareFilterHandler, GetUserHandler getUserHandler) {
		this.projectHandler = projectHandler;
		this.projectInfoHandler = projectInfoHandler;
		this.createProjectHandler = createProjectHandler;
		this.updateProjectHandler = updateProjectHandler;
		this.deleteProjectHandler = deleteProjectHandler;
		this.shareFilterHandler = shareFilterHandler;
		this.getUserHandler = getUserHandler;
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
	@PutMapping(value = "/{projectName}")
	@ResponseStatus(OK)
	@PreAuthorize(PROJECT_MANAGER_OR_ADMIN)
	@ApiOperation(value = "Update project", notes = "'Email Configuration' can be also update via PUT to /{projectName}/emailconfig resource.")
	public OperationCompletionRS updateProject(@PathVariable String projectName, @RequestBody @Validated UpdateProjectRQ updateProjectRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		return updateProjectHandler.updateProject(ProjectUtils.extractProjectDetails(user, projectName), updateProjectRQ, user);
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/{projectName}")
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	@ApiOperation(value = "Get information about project", notes = "Only for users that are assigned to the project")
	public ProjectResource getProject(@PathVariable String projectName, @AuthenticationPrincipal ReportPortalUser user) {
		return projectHandler.getProject(EntityUtils.normalizeId(projectName));
	}

	@PutMapping("/{projectName}/unassign")
	@ResponseStatus(OK)
	@PreAuthorize(PROJECT_MANAGER)
	@ApiOperation("Un assign users")
	public OperationCompletionRS unassignProjectUsers(@PathVariable String projectName,
			@RequestBody @Validated UnassignUsersRQ unassignUsersRQ, @AuthenticationPrincipal ReportPortalUser user) {
		return updateProjectHandler.unassignUsers(ProjectUtils.extractProjectDetails(user, projectName), unassignUsersRQ, user);
	}

	@PutMapping("/{projectName}/assign")
	@ResponseStatus(OK)
	@PreAuthorize(PROJECT_MANAGER)
	@ApiOperation("Assign users")
	public OperationCompletionRS assignProjectUsers(@PathVariable String projectName, @RequestBody @Validated AssignUsersRQ assignUsersRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		return updateProjectHandler.assignUsers(ProjectUtils.extractProjectDetails(user, projectName), assignUsersRQ, user);
	}

	@GetMapping("/{projectName}/assignable")
	@ResponseStatus(OK)
	@PreAuthorize(PROJECT_MANAGER)
	@ApiOperation(value = "Load users which can be assigned to specified project", notes = "Only for users with project manager permissions")
	public Iterable<UserResource> getUsersForAssign(@FilterFor(User.class) Filter filter, @SortFor(User.class) Pageable pageable,
			@PathVariable String projectName, @AuthenticationPrincipal ReportPortalUser user) {
		return getUserHandler.getUsers(filter, pageable, ProjectUtils.extractProjectDetails(user, projectName));
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/{projectName}/users")
	@PreAuthorize(NOT_CUSTOMER)
	@ApiOperation("Get users from project")
	public Iterable<UserResource> getProjectUsers(@PathVariable String projectName, @FilterFor(User.class) Filter filter,
			@SortFor(User.class) Pageable pageable, @AuthenticationPrincipal ReportPortalUser user) {
		return projectHandler.getProjectUsers(ProjectUtils.extractProjectDetails(user, projectName), filter, pageable);
	}

	@GetMapping("/{projectName}/usernames/search")
	@ResponseStatus(OK)
	@ApiIgnore
	@PreAuthorize(PROJECT_MANAGER)
	public Iterable<UserResource> searchForUser(@SuppressWarnings("unused") @PathVariable String projectName,
			@RequestParam(value = "term") String term, Pageable pageable) {
		return projectHandler.getUserNames(term, pageable);
	}

	//	@PutMapping("/{projectName}/preference/{login}")
	//	@ResponseStatus(HttpStatus.OK)
	//	@PreAuthorize(ALLOWED_TO_EDIT_USER)
	//	@ApiIgnore
	//	// Hide method cause results using for UI only and doesn't affect WS
	//	public PreferenceResource updateUserPreference(@PathVariable String projectName,
	//			@RequestBody @Validated UpdatePreferenceRQ updatePreferenceRQ, @PathVariable String login, Principal principal) {
	//		return updatePreferenceHandler.updatePreference(principal.getName(), EntityUtils.normalizeId(projectName), updatePreferenceRQ);
	//	}
	//
	//	@GetMapping("/{projectName}/preference/{login}")
	//	@ResponseStatus(HttpStatus.OK)
	//	@PreAuthorize(ALLOWED_TO_EDIT_USER)
	//	@ApiOperation(value = "Load user preferences", notes = "Only for users that allowed to edit other users")
	//	public PreferenceResource getUserPreference(@PathVariable String projectName, @PathVariable String login, Principal principal) {
	//		return getPreferenceHandler.getPreference(normalizeId(projectName), normalizeId(login));
	//	}

	@Transactional(readOnly = true)
	@PreAuthorize(ADMIN_ONLY)
	@GetMapping(value = "/list")
	@ResponseStatus(HttpStatus.OK)
	@ApiIgnore
	public Iterable<ProjectInfoResource> getAllProjectsInfo(@FilterFor(Project.class) Filter filter,
			@SortFor(Project.class) Pageable pageable, @AuthenticationPrincipal ReportPortalUser user) {
		return projectInfoHandler.getAllProjectsInfo(filter, pageable);
	}

	@Transactional(readOnly = true)
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	@GetMapping("/list/{projectName}")
	@ResponseStatus(HttpStatus.OK)
	@ApiIgnore
	public ProjectInfoResource getProjectInfo(@PathVariable String projectName,
			@RequestParam(value = "interval", required = false, defaultValue = "3M") String interval,
			@AuthenticationPrincipal ReportPortalUser user) {
		return projectInfoHandler.getProjectInfo(normalizeId(projectName), interval);
	}

	@Transactional(readOnly = true)
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	@GetMapping("/{projectName}/widget/{widgetId}")
	@ResponseStatus(HttpStatus.OK)
	@ApiIgnore
	public Map<String, List<ChartObject>> getProjectWidget(@PathVariable String projectName,
			@RequestParam(value = "interval", required = false, defaultValue = "3M") String interval, @PathVariable String widgetId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return projectInfoHandler.getProjectInfoWidgetContent(normalizeId(projectName), interval, widgetId);
	}

	@Transactional(readOnly = true)
	@PreAuthorize(ADMIN_ONLY)
	@RequestMapping(value = "/names", method = RequestMethod.GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiIgnore
	public Iterable<String> getAllProjectNames(@AuthenticationPrincipal ReportPortalUser user) {
		return projectHandler.getAllProjectNames();
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
	@PostMapping(value = "/{projectName}/shared/{filterId}")
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation("Share user filter to project")
	public void shareFilter(@PathVariable String projectName, @PathVariable Long filterId) {
		shareFilterHandler.shareFilter(projectName, filterId);
	}
}
