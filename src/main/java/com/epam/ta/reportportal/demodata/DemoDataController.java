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

import static com.epam.ta.reportportal.auth.permissions.Permissions.ALLOWED_TO_EDIT_PROJECT;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.demodata.model.DemoDataRq;
import com.epam.ta.reportportal.demodata.model.DemoDataRs;
import com.epam.ta.reportportal.demodata.service.DemoDataService;
import com.epam.ta.reportportal.util.ProjectExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Ihar Kahadouski
 */
@RestController
@RequestMapping("/v1/demo/{projectKey}")
@PreAuthorize(ALLOWED_TO_EDIT_PROJECT)
@Tag(name = "Demo Data", description = "Demo data API collection")
class DemoDataController {

	private final DemoDataService demoDataService;

	private final ProjectExtractor projectExtractor;

	DemoDataController(DemoDataService demoDataService, ProjectExtractor projectExtractor) {
		this.demoDataService = demoDataService;
		this.projectExtractor = projectExtractor;
	}

	@PostMapping("/generate")
	@Operation(summary =  "generate")
	public DemoDataRs generate(@PathVariable String projectKey, @Validated @RequestBody DemoDataRq demoDataRq,
			@AuthenticationPrincipal ReportPortalUser user) {
		return demoDataService.generate(demoDataRq, projectExtractor.extractProjectDetailsAdmin(projectKey), user);
	}
}
