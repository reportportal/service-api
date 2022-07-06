/*
 * Copyright 2022 EPAM Systems
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

import com.epam.ta.reportportal.core.integration.ExecuteIntegrationHandler;
import com.epam.ta.reportportal.core.integration.plugin.GetPluginHandler;
import com.epam.ta.reportportal.core.integration.plugin.binary.PluginFilesProvider;
import com.epam.ta.reportportal.entity.attachment.BinaryData;
import com.epam.ta.reportportal.util.BinaryDataResponseWriter;
import com.epam.ta.reportportal.ws.model.integration.IntegrationTypeResource;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@RestController
@RequestMapping(value = "/v1/plugin/public")
public class PluginPublicController {

	private final PluginFilesProvider pluginPublicFilesProvider;
	private final BinaryDataResponseWriter binaryDataResponseWriter;
	private final ExecuteIntegrationHandler executeIntegrationHandler;
	private final GetPluginHandler getPluginHandler;

	public PluginPublicController(PluginFilesProvider pluginPublicFilesProvider, BinaryDataResponseWriter binaryDataResponseWriter,
			ExecuteIntegrationHandler executeIntegrationHandler, GetPluginHandler getPluginHandler) {
		this.pluginPublicFilesProvider = pluginPublicFilesProvider;
		this.binaryDataResponseWriter = binaryDataResponseWriter;
		this.executeIntegrationHandler = executeIntegrationHandler;
		this.getPluginHandler = getPluginHandler;
	}

	@GetMapping(value = "/{pluginName}/file/{name}")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get public plugin file without authentication")
	public void getPublicFile(@PathVariable(value = "pluginName") String pluginName, @PathVariable(value = "name") String fileName,
			HttpServletResponse response) {
		final BinaryData binaryData = pluginPublicFilesProvider.load(pluginName, fileName);
		binaryDataResponseWriter.write(binaryData, response);
	}

	@PutMapping(value = "/{pluginName}/{command}", consumes = { APPLICATION_JSON_VALUE })
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Execute public command without authentication")
	public Object executePublicPluginCommand(@PathVariable("pluginName") String pluginName,
			@PathVariable("command") String command, @RequestBody Map<String, Object> executionParams) {
		return executeIntegrationHandler.executePublicCommand(pluginName, command, executionParams);
	}

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get all available public plugins")
	public List<IntegrationTypeResource> getPlugins() {
		return getPluginHandler.getPublicPlugins();
	}
}
