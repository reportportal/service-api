/*
 * Copyright 2017 EPAM Systems
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

import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.entity.Dashboard;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.StatisticsCalculationStrategy;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.epam.ta.reportportal.commons.Predicates.in;
import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.google.common.base.Strings.isNullOrEmpty;

@Service
class DemoDataService {

	private final DemoDashboardsService demoDashboardsService;
	private final DemoDataFacadeFactory demoDataFacadeFactory;
	private final ProjectRepository projectRepository;

	@Autowired
	DemoDataService(DemoDashboardsService demoDashboardsService, ProjectRepository projectRepository,
			DemoDataFacadeFactory demoDataFacadeFactory) {
		this.demoDashboardsService = demoDashboardsService;
		this.projectRepository = projectRepository;
		this.demoDataFacadeFactory = demoDataFacadeFactory;
	}

	DemoDataRs generate(DemoDataRq rq, String projectName, String user) {
		DemoDataRs demoDataRs = new DemoDataRs();
		Project project = projectRepository.findOne(projectName);
		BusinessRule.expect(project, Predicates.notNull()).verify(ErrorType.PROJECT_NOT_FOUND, projectName);

		List<String> postfixes = Optional.ofNullable(project.getMetadata())
				.map(Project.Metadata::getDemoDataPostfix)
				.orElse(new ArrayList<>());
		if (!isNullOrEmpty(rq.getPostfix())) {
			BusinessRule.expect(rq.getPostfix(), not(in(postfixes)))
					.verify(ErrorType.DEMO_DATA_GENERATION_ERROR, String.format("Postfix %s already used", rq.getPostfix()));
		}

		StatisticsCalculationStrategy statsStrategy = project.getConfiguration().getStatisticsCalculationStrategy();
		DemoDataFacade demoData = demoDataFacadeFactory.getDemoDataFacade(statsStrategy);
		final List<String> launches = demoData.generateDemoLaunches(rq, user, projectName);
		demoDataRs.setLaunches(launches);
		if (rq.isCreateDashboard()) {
			Dashboard demoDashboard = demoDashboardsService.generate(rq, user, projectName);
			demoDataRs.setDashboards(Collections.singletonList(demoDashboard.getId()));
		}

		projectRepository.addDemoDataPostfix(project.getName(), rq.getPostfix());
		return demoDataRs;
	}
}
