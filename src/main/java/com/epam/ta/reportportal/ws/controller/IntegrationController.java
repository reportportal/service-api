/*
 * Copyright 2019 EPAM Systems
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

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.integration.CreateIntegrationHandler;
import com.epam.ta.reportportal.core.integration.DeleteIntegrationHandler;
import com.epam.ta.reportportal.core.integration.ExecuteIntegrationHandler;
import com.epam.ta.reportportal.core.integration.GetIntegrationHandler;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.integration.CreateIntegrationRQ;
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
import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.auth.permissions.Permissions.*;
import static com.epam.ta.reportportal.util.ProjectExtractor.extractProjectDetails;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@RestController
@RequestMapping(value = "/integration")
public class IntegrationController {

	private final DeleteIntegrationHandler deleteIntegrationHandler;
	private final GetIntegrationHandler getIntegrationHandler;
	private final CreateIntegrationHandler createIntegrationHandler;
	private final ExecuteIntegrationHandler executeIntegrationHandler;

	@Autowired
	public IntegrationController(DeleteIntegrationHandler deleteIntegrationHandler, GetIntegrationHandler getIntegrationHandler,
			CreateIntegrationHandler createIntegrationHandler, ExecuteIntegrationHandler executeIntegrationHandler) {
		this.deleteIntegrationHandler = deleteIntegrationHandler;
		this.getIntegrationHandler = getIntegrationHandler;
		this.createIntegrationHandler = createIntegrationHandler;
		this.executeIntegrationHandler = executeIntegrationHandler;
	}

	@Transactional(readOnly = true)
	@GetMapping("/global/all")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get available global integrations")
	public List<IntegrationResource> getGlobalIntegrations(@AuthenticationPrincipal ReportPortalUser reportPortalUser) {
		return getIntegrationHandler.getGlobalIntegrations();
	}

	@Transactional(readOnly = true)
	@GetMapping("/global/all/{pluginName}")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get available global integrations for plugin")
	public List<IntegrationResource> getGlobalIntegrations(@AuthenticationPrincipal ReportPortalUser reportPortalUser,
			@PathVariable String pluginName) {
		return getIntegrationHandler.getGlobalIntegrations(pluginName);
	}

	@Transactional(readOnly = true)
	@GetMapping("/project/{projectName}/all")
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	@ApiOperation("Get available global integrations")
	public List<IntegrationResource> getGlobalIntegrations(@PathVariable String projectName,
			@AuthenticationPrincipal ReportPortalUser reportPortalUser) {
		return getIntegrationHandler.getProjectIntegrations(extractProjectDetails(reportPortalUser, projectName));
	}

	@Transactional(readOnly = true)
	@GetMapping("/project/{projectName}/all/{pluginName}")
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	@ApiOperation("Get available global integrations for plugin")
	public List<IntegrationResource> getGlobalIntegrations(@AuthenticationPrincipal ReportPortalUser reportPortalUser,
			@PathVariable String projectName, @PathVariable String pluginName) {
		return getIntegrationHandler.getProjectIntegrations(pluginName, extractProjectDetails(reportPortalUser, projectName));
	}

	@Transactional
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation("Create global Report Portal integration instance")
	@PreAuthorize(ADMIN_ONLY)
	public EntryCreatedRS createGlobalIntegration(@RequestBody @Valid CreateIntegrationRQ createRequest,
			@AuthenticationPrincipal ReportPortalUser user) {
		return createIntegrationHandler.createGlobalIntegration(createRequest);
	}

	@Transactional
	@PostMapping(value = "/{projectName}")
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation("Create global Report Portal integration instance")
	@PreAuthorize(PROJECT_MANAGER)
	public EntryCreatedRS createProjectIntegration(@RequestBody @Valid CreateIntegrationRQ createRequest, @PathVariable String projectName,
			@AuthenticationPrincipal ReportPortalUser user) {
		return createIntegrationHandler.createProjectIntegration(extractProjectDetails(user, projectName), createRequest, user);

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
	@DeleteMapping(value = "/all/{type}")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Delete all integrations assigned to specified project")
	@PreAuthorize(ADMIN_ONLY)
	public OperationCompletionRS deleteAllIntegrations(@PathVariable String type, @AuthenticationPrincipal ReportPortalUser user) {
		return deleteIntegrationHandler.deleteGlobalIntegrationsByType(type);
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
	@DeleteMapping(value = "/{projectName}/all/{type}")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Delete all integrations assigned to specified project")
	@PreAuthorize(PROJECT_MANAGER)
	public OperationCompletionRS deleteAllProjectIntegrations(@PathVariable String type, @PathVariable String projectName,
			@AuthenticationPrincipal ReportPortalUser user) {
		return deleteIntegrationHandler.deleteProjectIntegrationsByType(type,
				extractProjectDetails(user, EntityUtils.normalizeId(projectName)),
				user
		);
	}

	@Transactional(readOnly = true)
	@PutMapping(value = "{projectName}/{integrationId}/{command}", consumes = { APPLICATION_JSON_VALUE })
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	@ApiOperation("Execute command to the integration instance")
	public Object executeIntegrationCommand(@PathVariable String projectName, @PathVariable("integrationId") Long integrationId,
			@PathVariable("command") String command, @RequestBody Map<String, ?> executionParams,
			@AuthenticationPrincipal ReportPortalUser user) {
		return executeIntegrationHandler.executeCommand(extractProjectDetails(user, projectName), integrationId, command, executionParams);
	}

}
