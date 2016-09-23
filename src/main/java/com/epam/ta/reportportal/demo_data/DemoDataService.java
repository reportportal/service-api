package com.epam.ta.reportportal.demo_data;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.database.entity.Dashboard;

@Service
class DemoDataService {

	private DemoLaunchesService demoLaunchesService;
	private DemoDashboardsService demoDashboardsService;

	@Autowired
	DemoDataService(DemoLaunchesService demoLaunchesService, DemoDashboardsService demoDashboardsService) {
		this.demoLaunchesService = demoLaunchesService;
		this.demoDashboardsService = demoDashboardsService;
	}

	DemoDataRs generate(DemoDataRq rq, String project, String user) {
		DemoDataRs demoDataRs = new DemoDataRs();
		final List<String> launches = demoLaunchesService.generateDemoLaunches(rq, user, project);
		demoDataRs.setLaunches(launches);
		if (rq.isCreateDashboard()) {
			Dashboard demoDashboard = demoDashboardsService.createDemoDashboard(rq, user, project);
			demoDataRs.setDashboards(Collections.singletonList(demoDashboard.getId()));
		}
		return demoDataRs;
	}
}
