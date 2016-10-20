/*
 * Copyright 2016 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.demo_data;

import static com.epam.ta.reportportal.auth.permissions.Permissions.PROJECT_LEAD;

import java.security.Principal;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@RestController
@RequestMapping("/demo/{projectName}")
class DemoDataController {

	private DemoDataService demoDataService;

	DemoDataController(DemoDataService demoDataService) {
		this.demoDataService = demoDataService;
	}

	@PostMapping
	@PreAuthorize(PROJECT_LEAD)
	DemoDataRs generate(@PathVariable String projectName, @Validated @RequestBody DemoDataRq demoDataRq, Principal principal) {
		return demoDataService.generate(demoDataRq, projectName, principal.getName());
	}
}
