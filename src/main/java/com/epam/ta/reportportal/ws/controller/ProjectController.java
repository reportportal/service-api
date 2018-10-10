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
import com.epam.ta.reportportal.core.project.ICreateProjectHandler;
import com.epam.ta.reportportal.core.project.IGetProjectHandler;
import com.epam.ta.reportportal.core.project.IUpdateProjectHandler;
import com.epam.ta.reportportal.util.ProjectUtils;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.project.CreateProjectRQ;
import com.epam.ta.reportportal.ws.model.project.ProjectResource;
import com.epam.ta.reportportal.ws.model.project.UpdateProjectRQ;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.epam.ta.reportportal.auth.permissions.Permissions.*;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/project")
public class ProjectController {

	private final ICreateProjectHandler createProjectHandler;
	private final IGetProjectHandler getProjectHandler;
	private final IUpdateProjectHandler updateProjectHandler;

	@Autowired
	public ProjectController(ICreateProjectHandler createProjectHandler, IGetProjectHandler getProjectHandler,
			IUpdateProjectHandler updateProject) {
		this.createProjectHandler = createProjectHandler;
		this.getProjectHandler = getProjectHandler;
		this.updateProjectHandler = updateProject;
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
		return getProjectHandler.getProject(projectName);
	}

}
