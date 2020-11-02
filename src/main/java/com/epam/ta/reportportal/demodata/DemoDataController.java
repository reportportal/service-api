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

package com.epam.ta.reportportal.demodata;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.demodata.model.DemoDataRq;
import com.epam.ta.reportportal.demodata.model.DemoDataRs;
import com.epam.ta.reportportal.demodata.service.DemoDataService;
import com.epam.ta.reportportal.util.ProjectExtractor;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.epam.ta.reportportal.auth.permissions.Permissions.PROJECT_MANAGER;

/**
 * @author Ihar Kahadouski
 */
@RestController
@RequestMapping("/v1/demo/{projectName}")
@PreAuthorize(PROJECT_MANAGER)
class DemoDataController {

	private final DemoDataService demoDataService;

	private final ProjectExtractor projectExtractor;

	DemoDataController(DemoDataService demoDataService, ProjectExtractor projectExtractor) {
		this.demoDataService = demoDataService;
		this.projectExtractor = projectExtractor;
	}

	@PostMapping
	@ApiOperation(value = "generate")
	public DemoDataRs generate(@PathVariable String projectName, @Validated @RequestBody DemoDataRq demoDataRq,
			@AuthenticationPrincipal ReportPortalUser user) {
		return demoDataService.generate(demoDataRq, projectExtractor.extractProjectDetailsAdmin(user, projectName), user);
	}
}
