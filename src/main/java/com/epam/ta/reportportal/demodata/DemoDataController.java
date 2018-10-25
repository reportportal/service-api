/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.demodata;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import static com.epam.ta.reportportal.util.ProjectUtils.extractProjectDetails;

/**
 * @author Ihar Kahadouski
 */
@RestController
@RequestMapping("/demo/{projectName}")
class DemoDataController {

	private final DemoDataService demoDataService;

	@Autowired
	DemoDataController(DemoDataService demoDataService) {
		this.demoDataService = demoDataService;
	}

	@PostMapping
	//	@PreAuthorize(PROJECT_MANAGER)
	@ApiOperation(value = "generate")
	@ApiIgnore
	DemoDataRs generate(@PathVariable String projectName, @Validated @RequestBody DemoDataRq demoDataRq,
			@AuthenticationPrincipal ReportPortalUser user) {
		return demoDataService.generate(demoDataRq, extractProjectDetails(user, projectName), user);
	}
}
