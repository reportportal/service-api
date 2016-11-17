/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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

package com.epam.ta.reportportal.ws.converter.builders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.commons.SendCase;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.ProjectSpecific;
import com.epam.ta.reportportal.database.entity.StatisticsCalculationStrategy;
import com.epam.ta.reportportal.database.entity.project.EntryType;
import com.epam.ta.reportportal.database.entity.project.InterruptionJobDelay;
import com.epam.ta.reportportal.database.entity.project.KeepLogsDelay;
import com.epam.ta.reportportal.database.entity.project.KeepScreenshotsDelay;
import com.epam.ta.reportportal.ws.model.project.CreateProjectRQ;
import com.epam.ta.reportportal.ws.model.project.email.EmailSenderCase;
import com.epam.ta.reportportal.ws.model.project.email.ProjectEmailConfig;
import com.google.common.collect.Lists;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;

/**
 * New project object builder
 * 
 * @author Andrei_Ramanchuk
 */
@Service
@Scope("prototype")
public class ProjectBuilder extends Builder<Project> {

	public ProjectBuilder addCreateProjectRQ(CreateProjectRQ createProjectRQ) {
		if (createProjectRQ != null) {
			getObject().setName(createProjectRQ.getProjectName().trim());
			getObject().setCreationDate(new Date());

			getObject().getConfiguration().setEntryType(EntryType.findByName(createProjectRQ.getEntryType()).orElse(null));
			if (null != createProjectRQ.getCustomer())
				getObject().setCustomer(createProjectRQ.getCustomer().trim());
			if (null != createProjectRQ.getAddInfo())
				getObject().setAddInfo(createProjectRQ.getAddInfo().trim());

			// Empty fields creation by default
			getObject().getConfiguration().setExternalSystem(new ArrayList<>());
			getObject().getConfiguration().setProjectSpecific(ProjectSpecific.DEFAULT);
			getObject().getConfiguration().setInterruptJobTime(InterruptionJobDelay.ONE_DAY.getValue());
			getObject().getConfiguration().setKeepLogs(KeepLogsDelay.THREE_MONTHS.getValue());
			getObject().getConfiguration().setKeepScreenshots(KeepScreenshotsDelay.TWO_WEEKS.getValue());
			getObject().getConfiguration().setIsAutoAnalyzerEnabled(false);
			getObject().getConfiguration().setStatisticsCalculationStrategy(StatisticsCalculationStrategy
					.fromString(createProjectRQ.getStatsCalculationStrategy()).orElse(StatisticsCalculationStrategy.STEP_BASED));

			// Email settings by default
			EmailSenderCase defaultOne = new EmailSenderCase(newArrayList("OWNER"), SendCase.ALWAYS.name(), emptyList(), emptyList());
			ProjectEmailConfig config = new ProjectEmailConfig(false, "reportportal@example.com", newArrayList(defaultOne));
			getObject().getConfiguration().setEmailConfig(config);

			// Users
			getObject().setUsers(new HashMap<>());
		}
		return this;
	}

	@Override
	protected Project initObject() {
		return new Project();
	}
}
