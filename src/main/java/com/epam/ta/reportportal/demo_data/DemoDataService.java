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
import java.util.Optional;

import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.StatisticsCalculationStrategy;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.database.entity.Dashboard;

@Service
class DemoDataService {

	private final DemoLaunchesService demoLaunchesService;
	private final DemoDashboardsService demoDashboardsService;
	private final ProjectRepository projectRepository;

	@Autowired
	DemoDataService(DemoLaunchesService demoLaunchesService, DemoDashboardsService demoDashboardsService, ProjectRepository projectRepository) {
		this.demoLaunchesService = demoLaunchesService;
		this.demoDashboardsService = demoDashboardsService;
		this.projectRepository = projectRepository;
	}

	DemoDataRs generate(DemoDataRq rq, String projectName, String user) {
		DemoDataRs demoDataRs = new DemoDataRs();
		Project project = projectRepository.findOne(projectName);
		StatisticsCalculationStrategy statsStrategy =
				Optional.ofNullable(project.getConfiguration().getStatisticsCalculationStrategy())
						.orElse(StatisticsCalculationStrategy.STEP_BASED);
		BusinessRule.expect(project, Predicates.notNull()).verify(ErrorType.PROJECT_NOT_FOUND, projectName);
		final List<String> launches = demoLaunchesService.generateDemoLaunches(rq, user, projectName, statsStrategy);
		demoDataRs.setLaunches(launches);
		if (rq.isCreateDashboard()) {
			Dashboard demoDashboard = demoDashboardsService.generate(rq, user, projectName);
			demoDataRs.setDashboards(Collections.singletonList(demoDashboard.getId()));
		}
		return demoDataRs;
	}
}
