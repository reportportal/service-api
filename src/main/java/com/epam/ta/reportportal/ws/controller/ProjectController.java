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
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.filter.ShareUserFilterHandler;
import com.epam.ta.reportportal.core.preference.GetPreferenceHandler;
import com.epam.ta.reportportal.core.preference.UpdatePreferenceHandler;
import com.epam.ta.reportportal.core.project.*;
import com.epam.ta.reportportal.core.user.GetUserHandler;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.preference.PreferenceResource;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.auth.permissions.Permissions.*;
import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/project")
public class ProjectController {

	private final GetProjectHandler projectHandler;
	private final GetProjectInfoHandler projectInfoHandler;
	private final CreateProjectHandler createProjectHandler;
	private final UpdateProjectHandler updateProjectHandler;
	private final DeleteProjectHandler deleteProjectHandler;
	private final ShareUserFilterHandler shareFilterHandler;
	private final GetUserHandler getUserHandler;
	private final GetPreferenceHandler getPreference;
	private final UpdatePreferenceHandler updatePreference;

	@Autowired
	public ProjectController(GetProjectHandler projectHandler, GetProjectInfoHandler projectInfoHandler,
			CreateProjectHandler createProjectHandler, UpdateProjectHandler updateProjectHandler, DeleteProjectHandler deleteProjectHandler,
			ShareUserFilterHandler shareFilterHandler, GetUserHandler getUserHandler, GetPreferenceHandler getPreference,
			UpdatePreferenceHandler updatePreference) {
		this.projectHandler = projectHandler;
		this.projectInfoHandler = projectInfoHandler;
		this.createProjectHandler = createProjectHandler;
		this.updateProjectHandler = updateProjectHandler;
		this.deleteProjectHandler = deleteProjectHandler;
		this.shareFilterHandler = shareFilterHandler;
		this.getUserHandler = getUserHandler;
		this.getPreference = getPreference;
		this.updatePreference = updatePreference;
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
	@ApiOperation(value = "Update project", notes = "'Email Configuration' can be also update via PUT to /{projectName}/emailconfig resource.")
	public OperationCompletionRS updateProject(@PathVariable String projectName, @RequestBody @Validated UpdateProjectRQ updateProjectRQ,
			@AuthenticationPrincipal ReportPortalUser user) {
		return updateProjectHandler.updateProject(ProjectExtractor.extractProjectDetails(user, projectName), updateProjectRQ, user);
	}

	@Transactional
	@PutMapping("/{projectName}/emailconfig")
	@ResponseStatus(OK)
	@PreAuthorize(PROJECT_MANAGER)
	@ApiOperation("Update project email configuration")
	public OperationCompletionRS updateProjectEmailConfig(@PathVariable String projectName,
			@RequestBody @Validated ProjectEmailConfigDTO updateProjectRQ, @AuthenticationPrincipal ReportPortalUser user) {
<<<<<<<HEAD return updateProjectHandler.updateProjectEmailConfig(ProjectExtractor.extractProjectDetails(user, projectName), =======
		return updateProjectHandler.updateProjectEmailConfig(ProjectExtractor.extractProjectDetails(user, projectName), >>>>>>>
		e9cf928f5ced8fc3589d5b613d17fb203e07ebb8 user, updateProjectRQ
		);
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
	public OperationCompletionRS indexProjectData(@PathVariable String projectName, Principal principal) {
		return updateProjectHandler.indexProjectData(normalizeId(projectName), principal.getName());
	}

	@Transactional(readOnly = true)
	@GetMapping("/{projectName}/users")
	@PreAuthorize(NOT_CUSTOMER)
	@ApiOperation("Get users from project")
	public Iterable<UserResource> getProjectUsers(@PathVariable String projectName, @FilterFor(User.class) Filter filter,
			@SortFor(User.class) Pageable pageable, @AuthenticationPrincipal ReportPortalUser user) {
		return projectHandler.getProjectUsers(ProjectExtractor.extractProjectDetails(user, projectName), filter, pageable);
	}

	@Transactional(readOnly = true)
	@GetMapping("/{projectName}")
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	@ApiOperation(value = "Get information about project", notes = "Only for users that are assigned to the project")
	public ProjectResource getProject(@PathVariable String projectName, @AuthenticationPrincipal ReportPortalUser user) {
		return projectHandler.getProject(EntityUtils.normalizeId(projectName));
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
		return updateProjectHandler.assignUsers(ProjectExtractor.extractProjectDetails(user, projectName), assignUsersRQ, user);
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
		return projectHandler.getUserNames(ProjectExtractor.extractProjectDetails(user, projectName), normalizeId(value));
	}

	@Transactional(readOnly = true)
	@GetMapping("/{projectName}/usernames/search")
	@ResponseStatus(OK)
	@ApiIgnore
	@PreAuthorize(PROJECT_MANAGER)
	public Iterable<UserResource> searchForUser(@PathVariable String projectName, @RequestParam(value = "term") String term,
			Pageable pageable, @AuthenticationPrincipal ReportPortalUser user) {
		ProjectExtractor.extractProjectDetails(user, projectName);
		return projectHandler.getUserNames(term, pageable);
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
	@PostMapping(value = "/{projectName}/shared/{filterId}")
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation("Share user filter to project")
	public void shareFilter(@PathVariable String projectName, @PathVariable Long filterId) {
		shareFilterHandler.shareFilter(projectName, filterId);
	}
}
