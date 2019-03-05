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
import com.epam.ta.reportportal.core.integration.plugin.CreatePluginHandler;
import com.epam.ta.reportportal.core.integration.plugin.GetPluginHandler;
import com.epam.ta.reportportal.core.integration.plugin.UpdatePluginHandler;
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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ADMIN_ONLY;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@RestController
@RequestMapping(value = "/plugin")
public class PluginController {

	private final CreatePluginHandler createPluginHandler;
	private final UpdatePluginHandler updatePluginHandler;
	private final GetPluginHandler getPluginHandler;

	@Autowired
	public PluginController(CreatePluginHandler createPluginHandler, UpdatePluginHandler updatePluginHandler,
			GetPluginHandler getPluginHandler) {
		this.createPluginHandler = createPluginHandler;
		this.updatePluginHandler = updatePluginHandler;
		this.getPluginHandler = getPluginHandler;
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
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation("Update Report Portal plugin state")
	@PreAuthorize(ADMIN_ONLY)
	public OperationCompletionRS updatePluginState(@PathVariable(value = "pluginId") Long id,
			@RequestBody @Valid UpdatePluginStateRQ updatePluginStateRQ, @AuthenticationPrincipal ReportPortalUser user) {
		return updatePluginHandler.updatePluginState(id, updatePluginStateRQ);
	}

	@Transactional(readOnly = true)
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Update Report Portal plugin state")
	@PreAuthorize(ADMIN_ONLY)
	public List<IntegrationTypeResource> getPlugins(@AuthenticationPrincipal ReportPortalUser user) {
		return getPluginHandler.getPlugins();
	}
}
