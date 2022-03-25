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
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@RestController
@RequestMapping(value = "/v1/plugin/public")
public class PluginPublicController {

	private final PluginFilesProvider pluginPublicFilesProvider;

	public PluginPublicController(PluginFilesProvider pluginPublicFilesProvider) {
		this.pluginPublicFilesProvider = pluginPublicFilesProvider;
	}

	@GetMapping(value = "/{pluginName}/file/{name}")
	public void getPublicFile(@PathVariable(value = "pluginName") String pluginName, @PathVariable(value = "name") String fileName,
			HttpServletResponse response) {
		final BinaryData binaryData = pluginPublicFilesProvider.load(pluginName, fileName);
		toResponse(binaryData, response);
	}

	/**
	 * Copies data from provided {@link InputStream} to Response
	 *
	 * @param binaryData File data
	 * @param response   Response object
	 */
	private void toResponse(BinaryData binaryData, HttpServletResponse response) {
		try {
			response.setContentType(binaryData.getContentType());
			IOUtils.copy(binaryData.getInputStream(), response.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
			throw new ReportPortalException(ErrorType.UNABLE_TO_LOAD_BINARY_DATA, e.getMessage());
		}
	}
}
