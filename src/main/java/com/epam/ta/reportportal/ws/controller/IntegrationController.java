/*
 * Copyright 2018 EPAM Systems
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

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.integration.CreateIntegrationHandler;
import com.epam.ta.reportportal.core.integration.DeleteIntegrationHandler;
import com.epam.ta.reportportal.core.integration.GetIntegrationHandler;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.integration.IntegrationResource;
import com.epam.ta.reportportal.ws.model.integration.UpdateIntegrationRQ;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.epam.ta.reportportal.auth.permissions.Permissions.*;
import static com.epam.ta.reportportal.util.ProjectExtractor.extractProjectDetails;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@RestController
@RequestMapping(value = "/integration")
public class IntegrationController {

	private final DeleteIntegrationHandler deleteIntegrationHandler;
	private final GetIntegrationHandler getIntegrationHandler;
	private final CreateIntegrationHandler createIntegrationHandler;

	@Autowired
	public IntegrationController(DeleteIntegrationHandler deleteIntegrationHandler, GetIntegrationHandler getIntegrationHandler,
			CreateIntegrationHandler createIntegrationHandler) {
		this.deleteIntegrationHandler = deleteIntegrationHandler;
		this.getIntegrationHandler = getIntegrationHandler;
		this.createIntegrationHandler = createIntegrationHandler;
	}

	@Transactional
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation("Create global Report Portal integration instance")
	@PreAuthorize(ADMIN_ONLY)
	public OperationCompletionRS createGlobalIntegration(@RequestBody @Valid UpdateIntegrationRQ updateRequest,
			@AuthenticationPrincipal ReportPortalUser user) {

		return createIntegrationHandler.createGlobalIntegration(updateRequest);

	}

	@Transactional
	@PostMapping(value = "/{projectName}")
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation("Create global Report Portal integration instance")
	@PreAuthorize(PROJECT_MANAGER)
	public OperationCompletionRS createProjectIntegration(@RequestBody @Valid UpdateIntegrationRQ updateRequest,
			@PathVariable String projectName, @AuthenticationPrincipal ReportPortalUser user) {

		return createIntegrationHandler.createProjectIntegration(extractProjectDetails(user, projectName), updateRequest, user);

	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/{integrationId}")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get global Report Portal integration instance")
	@PreAuthorize(ADMIN_ONLY)
	public IntegrationResource getGlobalIntegration(@PathVariable Long integrationId, @AuthenticationPrincipal ReportPortalUser user) {

		return getIntegrationHandler.getGlobalIntegrationById(integrationId);
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/{projectName}/{integrationId}")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get integration instance")
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	public IntegrationResource getProjectIntegration(@PathVariable String projectName, @PathVariable Long integrationId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getIntegrationHandler.getProjectIntegrationById(integrationId,
				extractProjectDetails(user, EntityUtils.normalizeId(projectName))
		);
	}

	@Transactional
	@PutMapping(value = "/{integrationId}")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Update global Report Portal integration instance")
	@PreAuthorize(ADMIN_ONLY)
	public OperationCompletionRS updateGlobalIntegration(@PathVariable Long integrationId,
			@RequestBody @Valid UpdateIntegrationRQ updateRequest, @AuthenticationPrincipal ReportPortalUser user) {

		return createIntegrationHandler.updateGlobalIntegration(integrationId, updateRequest);

	}

	@Transactional
	@PutMapping(value = "/{projectName}/{integrationId}")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Update global Report Portal integration instance")
	@PreAuthorize(PROJECT_MANAGER)
	public OperationCompletionRS updateProjectIntegration(@PathVariable Long integrationId,
			@RequestBody @Valid UpdateIntegrationRQ updateRequest, @PathVariable String projectName,
			@AuthenticationPrincipal ReportPortalUser user) {

		return createIntegrationHandler.updateProjectIntegration(integrationId,
				extractProjectDetails(user, projectName),
				updateRequest,
				user
		);

	}

	@Transactional
	@DeleteMapping(value = "/{integrationId}")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Delete integration instance")
	@PreAuthorize(ADMIN_ONLY)
	public OperationCompletionRS deleteGlobalIntegration(@PathVariable Long integrationId, @AuthenticationPrincipal ReportPortalUser user) {
		return deleteIntegrationHandler.deleteGlobalIntegration(integrationId);
	}

	@Transactional
	@DeleteMapping(value = "/all")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Delete all integrations assigned to specified project")
	@PreAuthorize(ADMIN_ONLY)
	public OperationCompletionRS deleteAllIntegrations(@AuthenticationPrincipal ReportPortalUser user) {
		return deleteIntegrationHandler.deleteAllIntegrations();
	}

	@Transactional
	@DeleteMapping(value = "/{projectName}/{integrationId}")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Delete integration instance")
	@PreAuthorize(PROJECT_MANAGER)
	public OperationCompletionRS deleteProjectIntegration(@PathVariable String projectName, @PathVariable Long integrationId,
			@AuthenticationPrincipal ReportPortalUser user) {
		return deleteIntegrationHandler.deleteProjectIntegration(integrationId,
				extractProjectDetails(user, EntityUtils.normalizeId(projectName)),
				user
		);
	}

	@Transactional
	@DeleteMapping(value = "/{projectName}/all")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Delete all integrations assigned to specified project")
	@PreAuthorize(PROJECT_MANAGER)
	public OperationCompletionRS deleteAllProjectIntegrations(@PathVariable String projectName,
			@AuthenticationPrincipal ReportPortalUser user) {
		return deleteIntegrationHandler.deleteProjectIntegrations(extractProjectDetails(user, EntityUtils.normalizeId(projectName)), user);
	}

}
