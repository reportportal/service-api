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

package com.epam.ta.reportportal.demodata.service;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.demodata.model.DemoDataRq;
import com.epam.ta.reportportal.demodata.model.DemoDataRs;
import com.epam.ta.reportportal.entity.dashboard.Dashboard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @author Ihar Kahadouski
 */
@Service
public class DemoDataService {

	private final DemoDashboardsService demoDashboardsService;
	private final DemoDataFacade demoDataFacade;

	@Autowired
	public DemoDataService(DemoDashboardsService demoDashboardsService, DemoDataFacade demoDataFacade) {
		this.demoDashboardsService = demoDashboardsService;
		this.demoDataFacade = demoDataFacade;
	}

	public DemoDataRs generate(DemoDataRq demoDataRq, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		DemoDataRs demoDataRs = new DemoDataRs();
		final List<Long> launches = demoDataFacade.generateDemoLaunches(demoDataRq, user, projectDetails);
		demoDataRs.setLaunches(launches);
		if (demoDataRq.isCreateDashboard()) {
			Dashboard demoDashboard = demoDashboardsService.generate(user, projectDetails.getProjectId());
			demoDataRs.setDashboards(Collections.singletonList(demoDashboard.getId()));
		}

		return demoDataRs;
	}
}