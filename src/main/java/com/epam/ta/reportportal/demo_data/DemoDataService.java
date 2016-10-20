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
			Dashboard demoDashboard = demoDashboardsService.generate(rq, user, project);
			demoDataRs.setDashboards(Collections.singletonList(demoDashboard.getId()));
		}
		return demoDataRs;
	}
}
