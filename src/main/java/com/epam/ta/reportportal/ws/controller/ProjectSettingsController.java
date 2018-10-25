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
import com.epam.ta.reportportal.core.project.settings.ICreateProjectSettingsHandler;
import com.epam.ta.reportportal.core.project.settings.IDeleteProjectSettingsHandler;
import com.epam.ta.reportportal.core.project.settings.IGetProjectSettingsHandler;
import com.epam.ta.reportportal.core.project.settings.IUpdateProjectSettingsHandler;
import com.epam.ta.reportportal.util.ProjectUtils;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.project.config.CreateIssueSubTypeRQ;
import com.epam.ta.reportportal.ws.model.project.config.ProjectSettingsResource;
import com.epam.ta.reportportal.ws.model.project.config.UpdateIssueSubTypeRQ;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;
import static com.epam.ta.reportportal.auth.permissions.Permissions.PROJECT_MANAGER;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

/**
 * Projects settings controller.
 * Provides resources for manipulation of various project settings items.
 *
 * @author Andrei_Ramanchuk
 */
@Controller
@RequestMapping("/{projectName}/settings")
@PreAuthorize(ASSIGNED_TO_PROJECT)
public class ProjectSettingsController {

	private final ICreateProjectSettingsHandler createHandler;

	private final IUpdateProjectSettingsHandler updateHandler;

	private final IDeleteProjectSettingsHandler deleteHandler;

	private final IGetProjectSettingsHandler getHandler;

	@Autowired
	public ProjectSettingsController(ICreateProjectSettingsHandler createHandler, IUpdateProjectSettingsHandler updateHandler,
			IDeleteProjectSettingsHandler deleteHandler, IGetProjectSettingsHandler getHandler) {
		this.createHandler = createHandler;
		this.updateHandler = updateHandler;
		this.deleteHandler = deleteHandler;
		this.getHandler = getHandler;
	}

	@RequestMapping(value = "/sub-type", method = RequestMethod.POST)
	@ResponseBody
	@ResponseStatus(CREATED)
	@PreAuthorize(PROJECT_MANAGER)
	@ApiOperation("Creation of custom project specific issue sub-type")
	public EntryCreatedRS createProjectIssueSubType(@PathVariable String projectName, @RequestBody @Validated CreateIssueSubTypeRQ request,
			@AuthenticationPrincipal ReportPortalUser user) {
		return createHandler.createProjectIssueSubType(
				ProjectUtils.extractProjectDetails(user, EntityUtils.normalizeId(projectName)),
				user,
				request
		);
	}

	@RequestMapping(value = "/sub-type", method = RequestMethod.PUT)
	@ResponseBody
	@ResponseStatus(OK)
	@PreAuthorize(PROJECT_MANAGER)
	@ApiOperation("Update of custom project specific issue sub-type")
	public OperationCompletionRS updateProjectIssueSubType(@PathVariable String projectName,
			@RequestBody @Validated UpdateIssueSubTypeRQ request, @AuthenticationPrincipal ReportPortalUser user) {
		return updateHandler.updateProjectIssueSubType(
				ProjectUtils.extractProjectDetails(user, EntityUtils.normalizeId(projectName)),
				user,
				request
		);
	}

	@RequestMapping(value = "/sub-type/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	@ResponseStatus(OK)
	@PreAuthorize(PROJECT_MANAGER)
	@ApiOperation("Delete custom project specific issue sub-type")
	public OperationCompletionRS deleteProjectIssueSubType(@PathVariable String projectName, @PathVariable Long id,
			@AuthenticationPrincipal ReportPortalUser user) {
		return deleteHandler.deleteProjectIssueSubType(
				ProjectUtils.extractProjectDetails(user, EntityUtils.normalizeId(projectName)),
				user,
				id
		);
	}

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	@ResponseStatus(OK)
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	@ApiOperation(value = "Get project specific issue sub-types", notes = "Only for users that are assigned to the project")
	public ProjectSettingsResource getProjectSettings(@PathVariable String projectName, @AuthenticationPrincipal ReportPortalUser user) {
		return getHandler.getProjectSettings(ProjectUtils.extractProjectDetails(user, EntityUtils.normalizeId(projectName)));
	}
}
