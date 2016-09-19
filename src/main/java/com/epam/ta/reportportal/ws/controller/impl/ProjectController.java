/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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

import static com.epam.ta.reportportal.auth.permissions.Permissions.*;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import com.epam.ta.reportportal.ws.resolver.FilterCriteriaResolver;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.epam.ta.reportportal.ws.resolver.ResponseView;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
import com.epam.ta.reportportal.ws.model.preference.PreferenceResource;
import com.epam.ta.reportportal.ws.model.preference.UpdatePreferenceRQ;
import com.epam.ta.reportportal.ws.model.project.*;
import com.epam.ta.reportportal.ws.model.project.email.UpdateProjectEmailRQ;
import com.epam.ta.reportportal.ws.model.user.UserResource;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Project controller implementation
 * 
 * @author Hanna_Sukhadolava
 * 
 */
@Controller
@RequestMapping("/project")
@Api
public class ProjectController implements IProjectController {

	@Autowired
	private ICreateProjectHandler createProjectHandler;

	@Autowired
	private IUpdateProjectHandler updateProjectHandler;

	@Autowired
	private IDeleteProjectHandler deleteProjectHandler;

	@Autowired
	private IGetProjectHandler getProjectHandler;

	@Autowired
	private IGetPreferenceHandler getPreferenceHandler;

	@Autowired
	private IUpdatePreferenceHandler updatePreferenceHandler;

	@Autowired
	private IGetProjectInfoHandler getProjectInfoHandler;

	@Autowired
	private IGetUserHandler userHandler;

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
	@PreAuthorize(PROJECT_LEAD)
	@ApiOperation(value = "Update project", notes = "'Email Configuration' block is ignored at this model, please use PUT /{projectName}/emailconfig resource instead.")
	public OperationCompletionRS updateProject(@PathVariable String projectName, @RequestBody @Validated UpdateProjectRQ updateProjectRQ,
			Principal principal) {
		return updateProjectHandler.updateProject(EntityUtils.normalizeProjectName(projectName), updateProjectRQ, principal.getName());
	}

	@Override
	@RequestMapping(value = "/{projectName}/emailconfig", method = PUT, consumes = { APPLICATION_JSON_VALUE })
	@ResponseBody
	@ResponseStatus(OK)
	@PreAuthorize(PROJECT_LEAD)
	@ApiOperation("Update project email configuration")
	public OperationCompletionRS updateProjectEmailConfig(@PathVariable String projectName,
			@RequestBody @Validated UpdateProjectEmailRQ updateProjectRQ, Principal principal) {
		return updateProjectHandler.updateProjectEmailConfig(EntityUtils.normalizeProjectName(projectName), principal.getName(), updateProjectRQ);
	}

	@Override
	@RequestMapping(value = "/{projectName}", method = DELETE)
	@ResponseBody
	@ResponseStatus(OK)
	@PreAuthorize(ADMIN_ONLY)
	@ApiOperation("Delete project")
	public OperationCompletionRS deleteProject(@PathVariable String projectName, Principal principal) {
		return deleteProjectHandler.deleteProject(EntityUtils.normalizeProjectName(projectName));
	}

	@Override
	@RequestMapping(value = "/{projectName}/users", method = GET)
	@ResponseBody
	@ResponseView(ModelViews.DefaultView.class)
	@PreAuthorize(PROJECT_MEMBER)
	@ApiOperation("Get users from project")
	public Iterable<UserResource> getProjectUsers(@PathVariable String projectName, @FilterFor(User.class) Filter filter,
			@SortFor(User.class) Pageable pageable, Principal principal) {
		return getProjectHandler.getProjectUsers(EntityUtils.normalizeProjectName(projectName), filter, pageable);
	}

	@Override
	@RequestMapping(value = "/{projectName}", method = GET)
	@ResponseBody
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	@ApiOperation("Load project")
	public ProjectResource getProject(@PathVariable String projectName, Principal principal) {
		return getProjectHandler.getProject(EntityUtils.normalizeProjectName(projectName));
	}

	@Override
	@RequestMapping(value = "/{projectName}/unassign", method = PUT, consumes = { APPLICATION_JSON_VALUE })
	@ResponseBody
	@ResponseStatus(OK)
	@PreAuthorize(PROJECT_LEAD)
	@ApiOperation("Un assign users")
	public OperationCompletionRS unassignProjectUsers(@PathVariable String projectName,
			@RequestBody @Validated UnassignUsersRQ unassignUsersRQ, Principal principal) {
		return updateProjectHandler.unassignUsers(EntityUtils.normalizeProjectName(projectName), principal.getName(), unassignUsersRQ);
	}

	@Override
	@RequestMapping(value = "/{projectName}/assign", method = PUT, consumes = { APPLICATION_JSON_VALUE })
	@ResponseBody
	@ResponseStatus(OK)
	@PreAuthorize(PROJECT_LEAD)
	@ApiOperation("Assign users")
	public OperationCompletionRS assignProjectUsers(@PathVariable String projectName, @RequestBody @Validated AssignUsersRQ assignUsersRQ,
			Principal principal) {
		return updateProjectHandler.assignUsers(EntityUtils.normalizeProjectName(projectName), principal.getName(), assignUsersRQ);
	}

	@Override
	@RequestMapping(value = "/{projectName}/assignable", method = GET)
	@ResponseBody
	@ResponseStatus(OK)
	@ResponseView(ModelViews.DefaultView.class)
	@PreAuthorize(PROJECT_LEAD)
	@ApiOperation("Load users which can be assigned to specified project")
	public Iterable<UserResource> getUsersForAssign(@FilterFor(User.class) Filter filter, @SortFor(User.class) Pageable pageable,
			@PathVariable String projectName, Principal principal) {
		return userHandler.getUsers(filter, pageable, EntityUtils.normalizeProjectName(projectName));
	}

	@Override
	@RequestMapping(value = "/{projectName}/usernames", method = RequestMethod.GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize(PROJECT_MEMBER)
	@ApiOperation("Load project users by filter")
	public List<String> getProjectUsers(@PathVariable String projectName,
			@RequestParam(value = FilterCriteriaResolver.DEFAULT_FILTER_PREFIX + Condition.CNT + Project.USERS) String value,
			Principal principal) {
		return getProjectHandler.getUsernames(EntityUtils.normalizeProjectName(projectName), EntityUtils.normalizeUsername(value));
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
		return updatePreferenceHandler.updatePreference(principal.getName(), EntityUtils.normalizeProjectName(projectName),
				updatePreferenceRQ);
	}

	@Override
	@RequestMapping(value = "/{projectName}/preference/{login}", method = RequestMethod.GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize(ALLOWED_TO_EDIT_USER)
	@ApiOperation("Load user preferences")
	public PreferenceResource getUserPreference(@PathVariable String projectName, @PathVariable String login, Principal principal) {
		return getPreferenceHandler.getPreference(EntityUtils.normalizeProjectName(projectName), EntityUtils.normalizeUsername(login));
	}

	@Override
	@PreAuthorize(ADMIN_ONLY)
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	@ApiIgnore
	public Iterable<ProjectInfoResource> getAllProjectsInfo(Principal principal) {
		return getProjectInfoHandler.getAllProjectsInfo();
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
		return getProjectInfoHandler.getProjectInfo(EntityUtils.normalizeProjectName(projectName), interval);
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
		return getProjectInfoHandler.getProjectInfoWidgetContent(EntityUtils.normalizeProjectName(projectName), interval, widgetId);
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