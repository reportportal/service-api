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

import com.epam.reportportal.extension.saucelabs.SaucelabsExtensionPoint;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

import static com.epam.ta.reportportal.ws.controller.FileStorageController.toResponse;
import static com.epam.ta.reportportal.ws.model.ErrorType.UNABLE_INTERACT_WITH_INTEGRATION;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@RestController
@Transactional(readOnly = true)
@RequestMapping(value = "/{projectName}/saucelabs")
public class SaucelabsController {

	@Autowired
	private PluginBox pluginBox;

	@Autowired
	private IntegrationRepository integrationRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Transactional
	@GetMapping("{integrationId}/{jobId}/{dataCenter}/info")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get information about Saucelabs job")
	public Object getJobInfo(@PathVariable String projectName, @PathVariable Long integrationId, @PathVariable String jobId,
			@PathVariable String dataCenter, @AuthenticationPrincipal ReportPortalUser user) {
		Project project = projectRepository.findByName(projectName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectName));
		Integration integration = integrationRepository.findGlobalById(integrationId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, integrationId));
		SaucelabsExtensionPoint saucelabsExtensionPoint = pluginBox.getInstance(integration.getType().getName(),
				SaucelabsExtensionPoint.class
		)
				.orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION));
		return saucelabsExtensionPoint.getJobInfo(integration, jobId, dataCenter);
	}

	@Transactional
	@GetMapping("{integrationId}/{jobId}/{dataCenter}/logs")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get Saucelabs job logs")
	public Object getJobLogs(@PathVariable String projectName, @PathVariable Long integrationId, @PathVariable String jobId,
			@PathVariable String dataCenter, @AuthenticationPrincipal ReportPortalUser user) {
		Project project = projectRepository.findByName(projectName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectName));
		Integration integration = integrationRepository.findGlobalById(integrationId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, integrationId));
		SaucelabsExtensionPoint saucelabsExtensionPoint = pluginBox.getInstance(integration.getType().getName(),
				SaucelabsExtensionPoint.class
		)
				.orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION));
		return saucelabsExtensionPoint.getLogs(integration, jobId, dataCenter);
	}

	@Transactional
	@GetMapping("{integrationId}/{jobId}/{dataCenter}/video")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Get video of the Saucelabs job")
	public void getJobVideo(@PathVariable String projectName, @PathVariable Long integrationId, @PathVariable String jobId,
			@PathVariable String dataCenter, @AuthenticationPrincipal ReportPortalUser user, HttpServletResponse response) {
		Project project = projectRepository.findByName(projectName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectName));
		Integration integration = integrationRepository.findGlobalById(integrationId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.INTEGRATION_NOT_FOUND, integrationId));
		SaucelabsExtensionPoint saucelabsExtensionPoint = pluginBox.getInstance(integration.getType().getName(),
				SaucelabsExtensionPoint.class
		)
				.orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION));
		toResponse(response, saucelabsExtensionPoint.downloadVideo(integration, jobId, dataCenter));
	}

}
