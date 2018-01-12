/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 * 
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.ws.controller.impl;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.core.preference.IGetPreferenceHandler;
import com.epam.ta.reportportal.core.preference.IUpdatePreferenceHandler;
import com.epam.ta.reportportal.core.project.*;
import com.epam.ta.reportportal.core.user.IGetUserHandler;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.database.search.Condition;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.ws.controller.IProjectController;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.ModelViews;
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
import com.epam.ta.reportportal.ws.resolver.ResponseView;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
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
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * Project controller implementation
 *
 * @author Hanna_Sukhadolava
 */
@Controller
@RequestMapping("/project")
@Api
public class ProjectController implements IProjectController {

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

	@Override
	@PreAuthorize(ADMIN_ONLY)
	@RequestMapping(method = POST)
	@ResponseBody
	@ResponseStatus(CREATED)
	@ApiOperation("Create new project")
	public EntryCreatedRS createProject(@RequestBody @Validated CreateProjectRQ createProjectRQ, Principal principal) {
		return createProjectHandler.createProject(createProjectRQ, principal.getName());
	}

	@Override
	@RequestMapping(value = "/{projectName}", method = PUT, consumes = { APPLICATION_JSON_VALUE })
	@ResponseBody
	@ResponseStatus(OK)
	@PreAuthorize(PROJECT_MANAGER_OR_ADMIN)
	@ApiOperation(value = "Update project", notes = "'Email Configuration' can be also update via PUT to /{projectName}/emailconfig resource.")
	public OperationCompletionRS updateProject(@PathVariable String projectName, @RequestBody @Validated UpdateProjectRQ updateProjectRQ,
			Principal principal) {
		return updateProjectHandler.updateProject(normalizeId(projectName), updateProjectRQ, principal.getName());
	}

	@Override
	@RequestMapping(value = "/{projectName}/emailconfig", method = PUT, consumes = { APPLICATION_JSON_VALUE })
	@ResponseBody
	@ResponseStatus(OK)
	@PreAuthorize(PROJECT_MANAGER)
	@ApiOperation("Update project email configuration")
	public OperationCompletionRS updateProjectEmailConfig(@PathVariable String projectName,
			@RequestBody @Validated ProjectEmailConfigDTO updateProjectRQ, Principal principal) {
		return updateProjectHandler.updateProjectEmailConfig(normalizeId(projectName), principal.getName(), updateProjectRQ);
	}

	@Override
	@RequestMapping(value = "/{projectName}", method = DELETE)
	@ResponseBody
	@ResponseStatus(OK)
	@PreAuthorize(ADMIN_ONLY)
	@ApiOperation(value = "Delete project", notes = "Could be deleted only by users with administrator role")
	public OperationCompletionRS deleteProject(@PathVariable String projectName, Principal principal) {
		return deleteProjectHandler.deleteProject(normalizeId(projectName));
	}

	@Override
	@RequestMapping(value = "/{projectName}/users", method = GET)
	@ResponseBody
	@ResponseView(ModelViews.DefaultView.class)
	@PreAuthorize(NOT_CUSTOMER)
	@ApiOperation("Get users from project")
	public Iterable<UserResource> getProjectUsers(@PathVariable String projectName, @FilterFor(User.class) Filter filter,
			@SortFor(User.class) Pageable pageable, Principal principal) {
		return getProjectHandler.getProjectUsers(normalizeId(projectName), filter, pageable);
	}

	@Override
	@RequestMapping(value = "/{projectName}", method = GET)
	@ResponseBody
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	@ApiOperation(value = "Get information about project", notes = "Only for users that are assigned to the project")
	public ProjectResource getProject(@PathVariable String projectName, Principal principal) {
		return getProjectHandler.getProject(normalizeId(projectName));
	}

	@Override
	@RequestMapping(value = "/{projectName}/unassign", method = PUT, consumes = { APPLICATION_JSON_VALUE })
	@ResponseBody
	@ResponseStatus(OK)
	@PreAuthorize(PROJECT_MANAGER)
	@ApiOperation("Un assign users")
	public OperationCompletionRS unassignProjectUsers(@PathVariable String projectName,
			@RequestBody @Validated UnassignUsersRQ unassignUsersRQ, Principal principal) {
		return updateProjectHandler.unassignUsers(normalizeId(projectName), principal.getName(), unassignUsersRQ);
	}

	@Override
	@RequestMapping(value = "/{projectName}/assign", method = PUT, consumes = { APPLICATION_JSON_VALUE })
	@ResponseBody
	@ResponseStatus(OK)
	@PreAuthorize(PROJECT_MANAGER)
	@ApiOperation("Assign users")
	public OperationCompletionRS assignProjectUsers(@PathVariable String projectName, @RequestBody @Validated AssignUsersRQ assignUsersRQ,
			Principal principal) {
		return updateProjectHandler.assignUsers(normalizeId(projectName), principal.getName(), assignUsersRQ);
	}

	@Override
	@RequestMapping(value = "/{projectName}/assignable", method = GET)
	@ResponseBody
	@ResponseStatus(OK)
	@ResponseView(ModelViews.DefaultView.class)
	@PreAuthorize(PROJECT_MANAGER)
	@ApiOperation(value = "Load users which can be assigned to specified project", notes = "Only for users with project manager permissions")
	public Iterable<UserResource> getUsersForAssign(@FilterFor(User.class) Filter filter, @SortFor(User.class) Pageable pageable,
			@PathVariable String projectName, Principal principal) {
		return userHandler.getUsers(filter, pageable, normalizeId(projectName));
	}

	@Override
	@RequestMapping(value = "/{projectName}/usernames", method = RequestMethod.GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize(NOT_CUSTOMER)
	@ApiOperation(value = "Load project users by filter", notes = "Only for users that are members of the project")
	public List<String> getProjectUsers(@PathVariable String projectName,
			@RequestParam(value = FilterCriteriaResolver.DEFAULT_FILTER_PREFIX + Condition.CNT + Project.USERS) String value,
			Principal principal) {
		return getProjectHandler.getUserNames(normalizeId(projectName), normalizeId(value));
	}

	@RequestMapping(value = "/{projectName}/usernames/search", method = GET)
	@ResponseStatus(OK)
	@ResponseBody
	@ApiIgnore
	@PreAuthorize(PROJECT_MANAGER)
	public Page<UserResource> searchForUser(@SuppressWarnings("unused") @PathVariable String projectName,
			@RequestParam(value = "term") String term,
			Pageable pageable) {
		return getProjectHandler.getUserNames(term, pageable);
	}

	@Override
	@RequestMapping(value = "/{projectName}/preference/{login}", method = RequestMethod.PUT)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize(ALLOWED_TO_EDIT_USER)
	@ApiIgnore
	// Hide method cause results using for UI only and doesn't affect WS
	public OperationCompletionRS updateUserPreference(@PathVariable String projectName,
			@RequestBody @Validated UpdatePreferenceRQ updatePreferenceRQ, @PathVariable String login, Principal principal) {
		return updatePreferenceHandler.updatePreference(principal.getName(), EntityUtils.normalizeId(projectName), updatePreferenceRQ);
	}

	@Override
	@RequestMapping(value = "/{projectName}/preference/{login}", method = RequestMethod.GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize(ALLOWED_TO_EDIT_USER)
	@ApiOperation(value = "Load user preferences", notes = "Only for users that allowed to edit other users")
	public PreferenceResource getUserPreference(@PathVariable String projectName, @PathVariable String login, Principal principal) {
		return getPreferenceHandler.getPreference(normalizeId(projectName), normalizeId(login));
	}

	@Override
	@PreAuthorize(ADMIN_ONLY)
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiIgnore
	public Iterable<ProjectInfoResource> getAllProjectsInfo(@FilterFor(Project.class) Filter filter,
			@SortFor(Project.class) Pageable pageable, Principal principal) {
		return getProjectInfoHandler.getAllProjectsInfo(filter, pageable);
	}

	@Override
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	@RequestMapping(value = "/list/{projectName}", method = RequestMethod.GET)
	@ResponseBody
	@ResponseView(ModelViews.FullProjectInfoView.class)
	@ResponseStatus(HttpStatus.OK)
	@ApiIgnore
	public ProjectInfoResource getProjectInfo(@PathVariable String projectName,
			@RequestParam(value = "interval", required = false, defaultValue = "3M") String interval, Principal principal) {
		return getProjectInfoHandler.getProjectInfo(normalizeId(projectName), interval);
	}

	@Override
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	@RequestMapping(value = "/{projectName}/widget/{widgetId}", method = RequestMethod.GET)
	@ResponseBody
	@ResponseView(ModelViews.FullProjectInfoView.class)
	@ResponseStatus(HttpStatus.OK)
	@ApiIgnore
	public Map<String, List<ChartObject>> getProjectWidget(@PathVariable String projectName,
			@RequestParam(value = "interval", required = false, defaultValue = "3M") String interval, @PathVariable String widgetId,
			Principal principal) {
		return getProjectInfoHandler.getProjectInfoWidgetContent(normalizeId(projectName), interval, widgetId);
	}

	@Override
	@PreAuthorize(ADMIN_ONLY)
	@RequestMapping(value = "/names", method = RequestMethod.GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiIgnore
	public Iterable<String> getAllProjectNames(Principal principal) {
		return getProjectHandler.getAllProjectNames();
	}

}
