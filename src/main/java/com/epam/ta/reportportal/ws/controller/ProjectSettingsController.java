/*
 * Copyright 2018 EPAM Systems
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

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.core.project.settings.CreateProjectSettingsHandler;
import com.epam.ta.reportportal.core.project.settings.DeleteProjectSettingsHandler;
import com.epam.ta.reportportal.core.project.settings.GetProjectSettingsHandler;
import com.epam.ta.reportportal.core.project.settings.UpdateProjectSettingsHandler;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.project.config.CreateIssueSubTypeRQ;
import com.epam.ta.reportportal.ws.model.project.config.IssueSubTypeCreatedRS;
import com.epam.ta.reportportal.ws.model.project.config.ProjectSettingsResource;
import com.epam.ta.reportportal.ws.model.project.config.UpdateIssueSubTypeRQ;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;
import static com.epam.ta.reportportal.auth.permissions.Permissions.PROJECT_MANAGER;
import static com.epam.ta.reportportal.util.ProjectExtractor.extractProjectDetails;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

/**
 * Projects settings controller.
 * Provides resources for manipulation of various project settings items.
 *
 * @author Andrei_Ramanchuk
 */
@RestController
@RequestMapping("/{projectName}/settings")
@PreAuthorize(ASSIGNED_TO_PROJECT)
public class ProjectSettingsController {

	private final CreateProjectSettingsHandler createHandler;

	private final UpdateProjectSettingsHandler updateHandler;

	private final DeleteProjectSettingsHandler deleteHandler;

	private final GetProjectSettingsHandler getHandler;

	@Autowired
	public ProjectSettingsController(CreateProjectSettingsHandler createHandler, UpdateProjectSettingsHandler updateHandler,
			DeleteProjectSettingsHandler deleteHandler, GetProjectSettingsHandler getHandler) {
		this.createHandler = createHandler;
		this.updateHandler = updateHandler;
		this.deleteHandler = deleteHandler;
		this.getHandler = getHandler;
	}

	@PostMapping("/sub-type")
	@ResponseStatus(CREATED)
	@PreAuthorize(PROJECT_MANAGER)
	@ApiOperation("Creation of custom project specific issue sub-type")
	public IssueSubTypeCreatedRS createProjectIssueSubType(@PathVariable String projectName,
			@RequestBody @Validated CreateIssueSubTypeRQ request,
			@AuthenticationPrincipal ReportPortalUser user) {
		return createHandler.createProjectIssueSubType(extractProjectDetails(user, EntityUtils.normalizeId(projectName)), user, request);
	}

	@PutMapping("/sub-type")
	@ResponseStatus(OK)
	@PreAuthorize(PROJECT_MANAGER)
	@ApiOperation("Update of custom project specific issue sub-type")
	public OperationCompletionRS updateProjectIssueSubType(@PathVariable String projectName,
			@RequestBody @Validated UpdateIssueSubTypeRQ request, @AuthenticationPrincipal ReportPortalUser user) {
		return updateHandler.updateProjectIssueSubType(extractProjectDetails(user, EntityUtils.normalizeId(projectName)), user, request);
	}

	@DeleteMapping("/sub-type/{id}")
	@ResponseStatus(OK)
	@PreAuthorize(PROJECT_MANAGER)
	@ApiOperation("Delete custom project specific issue sub-type")
	public OperationCompletionRS deleteProjectIssueSubType(@PathVariable String projectName, @PathVariable Long id,
			@AuthenticationPrincipal ReportPortalUser user) {
		return deleteHandler.deleteProjectIssueSubType(extractProjectDetails(user, EntityUtils.normalizeId(projectName)), user, id);
	}

	@GetMapping
	@ResponseStatus(OK)
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	@ApiOperation(value = "Get project specific issue sub-types", notes = "Only for users that are assigned to the project")
	public ProjectSettingsResource getProjectSettings(@PathVariable String projectName, @AuthenticationPrincipal ReportPortalUser user) {
		return getHandler.getProjectSettings(extractProjectDetails(user, EntityUtils.normalizeId(projectName)));
	}
}