/*
 * Copyright 2019 EPAM Systems
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

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.integration.ExecuteIntegrationHandler;
import com.epam.ta.reportportal.core.integration.plugin.CreatePluginHandler;
import com.epam.ta.reportportal.core.integration.plugin.DeletePluginHandler;
import com.epam.ta.reportportal.core.integration.plugin.GetPluginHandler;
import com.epam.ta.reportportal.core.integration.plugin.UpdatePluginHandler;
import com.epam.ta.reportportal.core.integration.plugin.binary.PluginFilesProvider;
import com.epam.ta.reportportal.entity.attachment.BinaryData;
import com.epam.ta.reportportal.util.BinaryDataResponseWriter;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.integration.IntegrationTypeResource;
import com.epam.ta.reportportal.ws.model.integration.UpdatePluginStateRQ;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.auth.permissions.Permissions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@RestController
@RequestMapping(value = "/v1/plugin")
public class PluginController {

	private final CreatePluginHandler createPluginHandler;
	private final UpdatePluginHandler updatePluginHandler;
	private final GetPluginHandler getPluginHandler;
	private final DeletePluginHandler deletePluginHandler;
	private final ExecuteIntegrationHandler executeIntegrationHandler;
	private final ProjectExtractor projectExtractor;
	private final PluginFilesProvider pluginFilesProvider;
	private final BinaryDataResponseWriter binaryDataResponseWriter;

	@Autowired
	public PluginController(CreatePluginHandler createPluginHandler, UpdatePluginHandler updatePluginHandler,
			GetPluginHandler getPluginHandler, DeletePluginHandler deletePluginHandler, ExecuteIntegrationHandler executeIntegrationHandler,
			ProjectExtractor projectExtractor, PluginFilesProvider pluginFilesProvider,
			BinaryDataResponseWriter binaryDataResponseWriter) {
		this.createPluginHandler = createPluginHandler;
		this.updatePluginHandler = updatePluginHandler;
		this.getPluginHandler = getPluginHandler;
		this.deletePluginHandler = deletePluginHandler;
		this.executeIntegrationHandler = executeIntegrationHandler;
		this.projectExtractor = projectExtractor;
		this.pluginFilesProvider = pluginFilesProvider;
		this.binaryDataResponseWriter = binaryDataResponseWriter;
	}

	@Transactional
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation("Upload new Report Portal plugin")
	@PreAuthorize(ADMIN_ONLY)
	public EntryCreatedRS uploadPlugin(@NotNull @RequestParam("file") MultipartFile pluginFile,
			@AuthenticationPrincipal ReportPortalUser user) {
		return createPluginHandler.uploadPlugin(pluginFile);
	}

	@Transactional
	@PutMapping(value = "/{pluginId}")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Update Report Portal plugin state")
	@PreAuthorize(ADMIN_ONLY)
	public OperationCompletionRS updatePluginState(@PathVariable(value = "pluginId") Long id,
			@RequestBody @Valid UpdatePluginStateRQ updatePluginStateRQ, @AuthenticationPrincipal ReportPortalUser user) {
		return updatePluginHandler.updatePluginState(id, updatePluginStateRQ);
	}

	@Transactional(readOnly = true)
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get all available plugins")
	public List<IntegrationTypeResource> getPlugins(@AuthenticationPrincipal ReportPortalUser user) {
		return getPluginHandler.getPlugins();
	}

	@GetMapping(value = "/{pluginName}/file/{name}")
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize(AUTHENTICATED)
	@ApiOperation("Get plugin file by authorized user")
	public void getFile(@PathVariable(value = "pluginName") String pluginName, @PathVariable(value = "name") String fileName,
			HttpServletResponse response) {
		final BinaryData binaryData = pluginFilesProvider.load(pluginName, fileName);
		binaryDataResponseWriter.write(binaryData, response);
	}

	@Transactional
	@DeleteMapping(value = "/{pluginId}")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Delete plugin by id")
	@PreAuthorize(ADMIN_ONLY)
	public OperationCompletionRS deletePlugin(@PathVariable(value = "pluginId") Long id, @AuthenticationPrincipal ReportPortalUser user) {
		return deletePluginHandler.deleteById(id);
	}

	@Transactional
	@PutMapping(value = "{projectName}/{pluginName}/common/{command}", consumes = { APPLICATION_JSON_VALUE })
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	@ApiOperation("Execute command to the plugin instance")
	public Object executePluginCommand(@PathVariable String projectName, @PathVariable("pluginName") String pluginName,
			@PathVariable("command") String command, @RequestBody Map<String, Object> executionParams,
			@AuthenticationPrincipal ReportPortalUser user) {
		return executeIntegrationHandler.executeCommand(projectExtractor.extractProjectDetails(user, projectName),
				pluginName,
				command,
				executionParams
		);
	}
}
