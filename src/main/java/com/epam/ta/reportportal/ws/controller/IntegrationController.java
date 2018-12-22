package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.core.integration.DeleteIntegrationHandler;
import com.epam.ta.reportportal.core.integration.GetIntegrationHandler;
import com.epam.ta.reportportal.core.integration.UpdateIntegrationHandler;
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
	private final UpdateIntegrationHandler updateIntegrationHandler;

	@Autowired
	public IntegrationController(DeleteIntegrationHandler deleteIntegrationHandler, GetIntegrationHandler getIntegrationHandler,
			UpdateIntegrationHandler updateIntegrationHandler) {
		this.deleteIntegrationHandler = deleteIntegrationHandler;
		this.getIntegrationHandler = getIntegrationHandler;
		this.updateIntegrationHandler = updateIntegrationHandler;
	}

	@Transactional
	@RequestMapping(method = { RequestMethod.PUT, RequestMethod.POST })
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation("Create or update global Report Portal integration instance")
	@PreAuthorize(ADMIN_ONLY)
	public OperationCompletionRS createIntegration(@RequestBody @Valid UpdateIntegrationRQ updateRequest,
			@AuthenticationPrincipal ReportPortalUser user) {

		return updateIntegrationHandler.updateIntegration(updateRequest);

	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/{integrationId}")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get global Report Portal integration instance")
	@PreAuthorize(ADMIN_ONLY)
	public IntegrationResource getIntegration(@PathVariable Long integrationId, @AuthenticationPrincipal ReportPortalUser user) {

		return getIntegrationHandler.getGlobalIntegrationById(integrationId);
	}

	@Transactional
	@DeleteMapping(value = "/{integrationId}")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Delete integration instance")
	@PreAuthorize(ADMIN_ONLY)
	public OperationCompletionRS deleteIntegration(@PathVariable Long integrationId, @AuthenticationPrincipal ReportPortalUser user) {
		return deleteIntegrationHandler.deleteIntegration(integrationId);
	}

	@Transactional
	@DeleteMapping(value = "/clear")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Delete all integrations assigned to specified project")
	@PreAuthorize(ADMIN_ONLY)
	public OperationCompletionRS deleteAllIntegrations(@AuthenticationPrincipal ReportPortalUser user) {
		return deleteIntegrationHandler.deleteAllIntegrations();
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
	@DeleteMapping(value = "/{projectName}/clear")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Delete all integrations assigned to specified project")
	@PreAuthorize(PROJECT_MANAGER)
	public OperationCompletionRS deleteAllProjectIntegrations(@PathVariable String projectName,
			@AuthenticationPrincipal ReportPortalUser user) {
		return deleteIntegrationHandler.deleteProjectIntegrations(extractProjectDetails(user, EntityUtils.normalizeId(projectName)), user);
	}

	///////////////EMAIL SERVER////////////
	///////////////EMAIL SERVER////////////
	///////////////EMAIL SERVER////////////
	///////////////EMAIL SERVER////////////
	///////////////EMAIL SERVER////////////

	//	@Transactional(readOnly = true)
	//	@GetMapping(value = "/email")
	//	@ResponseStatus(HttpStatus.OK)
	//	@ApiOperation(value = "Get server email settings")
	//	@PreAuthorize(ADMIN_ONLY)
	//	public ServerSettingsResource getServerSettings(@AuthenticationPrincipal ReportPortalUser user) {
	//		return serverHandler.getServerSettings();
	//	}
	//
	//
	//	@Transactional
	//	@DeleteMapping(value = "/email")
	//	@ResponseStatus(HttpStatus.OK)
	//	@ApiOperation(value = "Delete email settings for specified profile")
	//	@PreAuthorize(ADMIN_ONLY)
	//	public OperationCompletionRS deleteEmailSettings(@AuthenticationPrincipal ReportPortalUser user) {
	//		return serverHandler.deleteEmailSettings();
	//	}
}
