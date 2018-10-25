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
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.filter.IShareUserFilterHandler;
import com.epam.ta.reportportal.core.project.*;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.util.ProjectUtils;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.project.CreateProjectRQ;
import com.epam.ta.reportportal.ws.model.project.ProjectInfoResource;
import com.epam.ta.reportportal.ws.model.project.ProjectResource;
import com.epam.ta.reportportal.ws.model.project.UpdateProjectRQ;
import com.epam.ta.reportportal.ws.model.user.UserResource;
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

import static com.epam.ta.reportportal.auth.permissions.Permissions.*;
import static com.epam.ta.reportportal.commons.EntityUtils.normalizeId;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;

@RestController
@RequestMapping("/project")
public class ProjectController {

	private final IGetProjectHandler projectHandler;
	private final IGetProjectInfoHandler projectInfoHandler;
	private final ICreateProjectHandler createProjectHandler;
	private final IUpdateProjectHandler updateProjectHandler;
	private final IDeleteProjectHandler deleteProjectHandler;
	private final IShareUserFilterHandler shareFilterHandler;

	@Autowired
	public ProjectController(IGetProjectHandler projectHandler, IGetProjectInfoHandler projectInfoHandler,
			ICreateProjectHandler createProjectHandler, IUpdateProjectHandler updateProjectHandler,
			IDeleteProjectHandler deleteProjectHandler, IShareUserFilterHandler shareFilterHandler) {
		this.projectHandler = projectHandler;
		this.projectInfoHandler = projectInfoHandler;
		this.createProjectHandler = createProjectHandler;
		this.updateProjectHandler = updateProjectHandler;
		this.deleteProjectHandler = deleteProjectHandler;
		this.shareFilterHandler = shareFilterHandler;
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
		return projectHandler.getProject(projectName);
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/{projectName}/users")
	@PreAuthorize(NOT_CUSTOMER)
	@ApiOperation("Get users from project")
	public Iterable<UserResource> getProjectUsers(@PathVariable String projectName, @FilterFor(User.class) Filter filter,
			@SortFor(User.class) Pageable pageable, @AuthenticationPrincipal ReportPortalUser user) {
		return projectHandler.getProjectUsers(normalizeId(projectName), filter, pageable);
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

	@DeleteMapping
	@RequestMapping(value = "/{projectName}", method = DELETE)
	@ResponseBody
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
