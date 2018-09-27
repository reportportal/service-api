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
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.Page;
import com.epam.ta.reportportal.ws.model.preference.PreferenceResource;
import com.epam.ta.reportportal.ws.model.preference.UpdatePreferenceRQ;
import com.epam.ta.reportportal.ws.model.project.*;
import com.epam.ta.reportportal.ws.model.project.email.ProjectEmailConfigDTO;
import com.epam.ta.reportportal.ws.model.user.UserResource;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import com.epam.ta.reportportal.ws.resolver.FilterCriteriaResolver;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;
import static com.epam.ta.reportportal.util.ProjectUtils.extractProjectDetails;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

/**
 * @author Pavel Bortnik
 */

@RestController
@RequestMapping("/project")
public class ProjectController {

	private final ICreateProjectHandler createProjectHandler;
	private final IUpdateProjectHandler updateProjectHandler;
	private final IDeleteProjectHandler deleteProjectHandler;
	private final IGetProjectHandler getProjectHandler;
	private final IGetPreferenceHandler getPreferenceHandler;
	private final IUpdatePreferenceHandler updatePreferenceHandler;
	private final IGetProjectInfoHandler getProjectInfoHandler;
	private final IGetUserHandler userHandler;

	@Autowired
	public ProjectController(ICreateProjectHandler createProjectHandler, IUpdateProjectHandler updateProjectHandler,
			IDeleteProjectHandler deleteProjectHandler, IGetProjectHandler getProjectHandler, IGetPreferenceHandler getPreferenceHandler,
			IUpdatePreferenceHandler updatePreferenceHandler, IGetProjectInfoHandler getProjectInfoHandler, IGetUserHandler userHandler) {
		this.createProjectHandler = createProjectHandler;
		this.updateProjectHandler = updateProjectHandler;
		this.deleteProjectHandler = deleteProjectHandler;
		this.getProjectHandler = getProjectHandler;
		this.getPreferenceHandler = getPreferenceHandler;
		this.updatePreferenceHandler = updatePreferenceHandler;
		this.getProjectInfoHandler = getProjectInfoHandler;
		this.userHandler = userHandler;
	}

	//@PreAuthorize(ADMIN_ONLY)
	@PostMapping
	@ResponseStatus(CREATED)
	@ApiOperation("Create new project")
	public EntryCreatedRS createProject(@RequestBody @Validated CreateProjectRQ createProjectRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		return createProjectHandler.createProject(createProjectRQ, user);
	}

	@PutMapping(value = "/{projectName}")
	@ResponseStatus(OK)
	//@PreAuthorize(PROJECT_MANAGER_OR_ADMIN)
	@ApiOperation(value = "Update project", notes = "'Email Configuration' can be also update via PUT to /{projectName}/emailconfig resource.")
	public OperationCompletionRS updateProject(@PathVariable String projectName, @RequestBody @Validated UpdateProjectRQ updateProjectRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		return updateProjectHandler.updateProject(extractProjectDetails(user, projectName), updateProjectRQ, user);
	}

	@PutMapping(value = "/{projectName}/emailconfig")
	@ResponseStatus(OK)
	//@PreAuthorize(PROJECT_MANAGER)
	@ApiOperation("Update project email configuration")
	public OperationCompletionRS updateProjectEmailConfig(@PathVariable String projectName,
			@RequestBody @Validated ProjectEmailConfigDTO updateProjectRQ, @AuthenticationPrincipal ReportPortalUser user) {
		return updateProjectHandler.updateProjectEmailConfig(extractProjectDetails(user, projectName), user, updateProjectRQ);
	}

	@DeleteMapping(value = "/{projectName}")
	@ResponseStatus(OK)
	//@PreAuthorize(ADMIN_ONLY)
	@ApiOperation(value = "Delete project", notes = "Could be deleted only by users with administrator role")
	public OperationCompletionRS deleteProject(@PathVariable String projectName, @AuthenticationPrincipal ReportPortalUser user) {
		return deleteProjectHandler.deleteProject(extractProjectDetails(user, projectName));
	}

	@DeleteMapping(value = "/{projectName}/index")
	@ResponseStatus(OK)
	//@PreAuthorize(PROJECT_MANAGER_OR_ADMIN)
	@ApiOperation(value = "Delete project index from ML")
	public OperationCompletionRS deleteProjectIndex(@PathVariable String projectName, @AuthenticationPrincipal ReportPortalUser user) {
		return deleteProjectHandler.deleteProjectIndex(extractProjectDetails(user, projectName), user);
	}

	@PutMapping(value = "/{projectName}/index")
	@ResponseStatus(OK)
	//@PreAuthorize(PROJECT_MANAGER_OR_ADMIN)
	@ApiOperation(value = "Starts reindex all project data in ML")
	public OperationCompletionRS indexProjectData(@PathVariable String projectName, @AuthenticationPrincipal ReportPortalUser user) {
		return updateProjectHandler.indexProjectData(extractProjectDetails(user, projectName), user);
	}

	@GetMapping(value = "/{projectName}/users")
	//@PreAuthorize(NOT_CUSTOMER)
	@ApiOperation("Get users from project")
	public Iterable<UserResource> getProjectUsers(@PathVariable String projectName, @FilterFor(User.class) Filter filter,
			@SortFor(User.class) Pageable pageable, @AuthenticationPrincipal ReportPortalUser user) {
		return getProjectHandler.getProjectUsers(extractProjectDetails(user, projectName), user, filter, pageable);
	}

	@GetMapping(value = "/{projectName}")
	//@PreAuthorize(ASSIGNED_TO_PROJECT)
	@ApiOperation(value = "Get information about project", notes = "Only for users that are assigned to the project")
	public ProjectResource getProject(@PathVariable String projectName, @AuthenticationPrincipal ReportPortalUser user) {
		return getProjectHandler.getProject(extractProjectDetails(user, projectName));
	}

	@PutMapping(value = "/{projectName}/unassign")
	@ResponseStatus(OK)
	//@PreAuthorize(PROJECT_MANAGER)
	@ApiOperation("Un assign users")
	public OperationCompletionRS unassignProjectUsers(@PathVariable String projectName,
			@RequestBody @Validated UnassignUsersRQ unassignUsersRQ, @AuthenticationPrincipal ReportPortalUser user) {
		return updateProjectHandler.unassignUsers(extractProjectDetails(user, projectName), user, unassignUsersRQ);
	}

	@PutMapping(value = "/{projectName}/assign")
	@ResponseStatus(OK)
	//@PreAuthorize(PROJECT_MANAGER)
	@ApiOperation("Assign users")
	public OperationCompletionRS assignProjectUsers(@PathVariable String projectName, @RequestBody @Validated AssignUsersRQ assignUsersRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		return updateProjectHandler.assignUsers(extractProjectDetails(user, projectName), user, assignUsersRQ);
	}

	@GetMapping(value = "/{projectName}/assignable")
	@ResponseStatus(OK)
	//@PreAuthorize(PROJECT_MANAGER)
	@ApiOperation(value = "Load users which can be assigned to specified project", notes = "Only for users with project manager permissions")
	public Iterable<UserResource> getUsersForAssign(@FilterFor(User.class) Filter filter, @SortFor(User.class) Pageable pageable,
			@PathVariable String projectName, @AuthenticationPrincipal ReportPortalUser user) {
		return userHandler.getUsers(filter, pageable, extractProjectDetails(user, projectName, user));
	}

	@GetMapping(value = "/{projectName}/usernames")
	@ResponseStatus(OK)
	//@PreAuthorize(NOT_CUSTOMER)
	@ApiOperation(value = "Load project users by filter", notes = "Only for users that are members of the project")
	public List<String> getProjectUsers(@PathVariable String projectName,
			@RequestParam(value = FilterCriteriaResolver.DEFAULT_FILTER_PREFIX + Condition.CNT + "user") String value,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getProjectHandler.getUserNames(extractProjectDetails(user, projectName), user, normalizeId(value));
	}

	@GetMapping(value = "/{projectName}/usernames/search")
	@ResponseStatus(OK)
	@ApiIgnore
	//@PreAuthorize(PROJECT_MANAGER)
	public Page<UserResource> searchForUser(@PathVariable String projectName, @RequestParam(value = "term") String term, Pageable pageable,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getProjectHandler.getUserNames(extractProjectDetails(user, projectName), user, term, pageable);
	}

	@PutMapping
	@ResponseStatus(OK)
	//@PreAuthorize(ALLOWED_TO_EDIT_USER)
	@ApiIgnore
	// Hide method cause results using for UI only and doesn't affect WS
	public PreferenceResource updateUserPreference(@PathVariable String projectName,
			@RequestBody @Validated UpdatePreferenceRQ updatePreferenceRQ, @PathVariable String login,
			@AuthenticationPrincipal ReportPortalUser user) {
		return updatePreferenceHandler.updatePreference(user, extractProjectDetails(user, projectName), updatePreferenceRQ);
	}

	@GetMapping(value = "/{projectName}/preference/{login}")
	@ResponseStatus(OK)
	//@PreAuthorize(ALLOWED_TO_EDIT_USER)
	@ApiOperation(value = "Load user preferences", notes = "Only for users that allowed to edit other users")
	public PreferenceResource getUserPreference(@PathVariable String projectName, @PathVariable String login,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getPreferenceHandler.getPreference(extractProjectDetails(user, projectName), user, normalizeId(login));
	}

	//@PreAuthorize(ADMIN_ONLY)
	@GetMapping(value = "/list")
	@ResponseStatus(OK)
	@ApiIgnore
	public Iterable<ProjectInfoResource> getAllProjectsInfo(@FilterFor(Project.class) Filter filter,
			@SortFor(Project.class) Pageable pageable, @AuthenticationPrincipal ReportPortalUser user) {
		return getProjectInfoHandler.getAllProjectsInfo(filter, pageable);
	}

	//@PreAuthorize(ASSIGNED_TO_PROJECT)
	@GetMapping(value = "/list/{projectName}")
	@ResponseStatus(OK)
	@ApiIgnore
	public ProjectInfoResource getProjectInfo(@PathVariable String projectName,
			@RequestParam(value = "interval", required = false, defaultValue = "3M") String interval,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getProjectInfoHandler.getProjectInfo(extractProjectDetails(user, projectName), interval);
	}

	//@PreAuthorize(ASSIGNED_TO_PROJECT)
	@GetMapping(value = "/{projectName}/widget/{widgetId}")
	@ResponseStatus(OK)
	@ApiIgnore
	public Map<String, List<ChartObject>> getProjectWidget(@PathVariable String projectName,
			@RequestParam(value = "interval", required = false, defaultValue = "3M") String interval, @PathVariable String widgetId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getProjectInfoHandler.getProjectInfoWidgetContent(normalizeId(projectName), interval, widgetId);
	}

	//@PreAuthorize(ADMIN_ONLY)
	@GetMapping(value = "/names")
	@ResponseStatus(OK)
	@ApiIgnore
	public Iterable<String> getAllProjectNames(@AuthenticationPrincipal ReportPortalUser user) {
		return getProjectHandler.getAllProjectNames();
	}

	//@PreAuthorize(ADMIN_ONLY)
	@RequestMapping(value = "analyzer/status", method = RequestMethod.GET)
	@ResponseStatus(OK)
	@ApiIgnore
	public Map<String, Boolean> getAnalyzerIndexingStatus(@AuthenticationPrincipal ReportPortalUser user) {
		return getProjectHandler.getAnalyzerIndexingStatus();
	}

}
	
}
