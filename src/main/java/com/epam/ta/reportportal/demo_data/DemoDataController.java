package com.epam.ta.reportportal.demo_data;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ADMIN_ONLY;

import java.security.Principal;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@RestController
@RequestMapping("/demo_data/{project}")
@PreAuthorize(ADMIN_ONLY)
class DemoDataController {

	private DemoDataService demoDataService;

	DemoDataController(DemoDataService demoDataService) {
		this.demoDataService = demoDataService;
	}

	@PostMapping
	DemoDataRs generate(@PathVariable String project, @RequestBody DemoDataRq demoDataRq, Principal principal) {
		return demoDataService.generate(demoDataRq, project, principal.getName());
	}
}
