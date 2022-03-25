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

import com.epam.ta.reportportal.core.integration.plugin.binary.PluginFilesProvider;
import com.epam.ta.reportportal.entity.attachment.BinaryData;
import com.epam.ta.reportportal.util.BinaryDataResponseWriter;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@RestController
@RequestMapping(value = "/v1/plugin/public")
public class PluginPublicController {

	private final PluginFilesProvider pluginPublicFilesProvider;
	private final BinaryDataResponseWriter binaryDataResponseWriter;

	public PluginPublicController(PluginFilesProvider pluginPublicFilesProvider, BinaryDataResponseWriter binaryDataResponseWriter) {
		this.pluginPublicFilesProvider = pluginPublicFilesProvider;
		this.binaryDataResponseWriter = binaryDataResponseWriter;
	}

	@GetMapping(value = "/{pluginName}/file/{name}")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get public plugin file without authentication")
	public void getPublicFile(@PathVariable(value = "pluginName") String pluginName, @PathVariable(value = "name") String fileName,
			HttpServletResponse response) {
		final BinaryData binaryData = pluginPublicFilesProvider.load(pluginName, fileName);
		binaryDataResponseWriter.write(binaryData, response);
	}
}
