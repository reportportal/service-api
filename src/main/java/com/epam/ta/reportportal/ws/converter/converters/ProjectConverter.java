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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.AnalyzeMode;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.ProjectSpecific;
import com.epam.ta.reportportal.database.entity.StatisticsCalculationStrategy;
import com.epam.ta.reportportal.database.entity.project.*;
import com.epam.ta.reportportal.database.entity.statistics.StatisticSubType;
import com.epam.ta.reportportal.ws.model.project.CreateProjectRQ;
import com.epam.ta.reportportal.ws.model.project.ProjectConfiguration;
import com.epam.ta.reportportal.ws.model.project.ProjectResource;
import com.epam.ta.reportportal.ws.model.project.config.IssueSubTypeResource;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

/**
 * Converts internal DB model to DTO
 *
 * @author Pavel Bortnik
 */
public final class ProjectConverter {
	private ProjectConverter() {
		//static only
	}

	public static final Function<CreateProjectRQ, Project> TO_MODEL = request -> {
		Preconditions.checkNotNull(request);
		Project project = new Project();
		project.setName(request.getProjectName().trim());
		project.setCreationDate(new Date());
		project.getConfiguration().setEntryType(EntryType.findByName(request.getEntryType()).orElse(null));
		ofNullable(request.getCustomer()).ifPresent(project::setCustomer);
		ofNullable(request.getAddInfo()).ifPresent(project::setAddInfo);

		// Empty fields creation by default
		project.getConfiguration().setExternalSystem(Lists.newArrayList());
		project.getConfiguration().setProjectSpecific(ProjectSpecific.DEFAULT);
		project.getConfiguration().setInterruptJobTime(InterruptionJobDelay.ONE_DAY.getValue());
		project.getConfiguration().setKeepLogs(KeepLogsDelay.THREE_MONTHS.getValue());
		project.getConfiguration().setKeepScreenshots(KeepScreenshotsDelay.TWO_WEEKS.getValue());
		project.getConfiguration().setIsAutoAnalyzerEnabled(false);
		project.getConfiguration().setAnalyzerMode(AnalyzeMode.BY_LAUNCH_NAME);
		project.getConfiguration().setStatisticsCalculationStrategy(StatisticsCalculationStrategy.STEP_BASED);

		// Email settings by default
		ProjectUtils.setDefaultEmailCofiguration(project);

		// Users
		project.setUsers(Lists.newArrayList());
		return project;
	};

	public static final Function<Project, ProjectResource> TO_RESOURCE = db -> {
		Preconditions.checkNotNull(db);
		ProjectResource resource = new ProjectResource();
		resource.setProjectId(db.getId());
		resource.setCustomer(db.getCustomer());
		resource.setAddInfo(db.getAddInfo());
		resource.setCreationDate(db.getCreationDate());

		List<ProjectResource.ProjectUser> users = db.getUsers().stream().map(user -> {
			ProjectResource.ProjectUser one = new ProjectResource.ProjectUser();
			one.setLogin(user.getLogin());
			ofNullable(user.getProjectRole()).ifPresent(role -> one.setProjectRole(role.name()));
			ofNullable(user.getProposedRole()).ifPresent(role -> one.setProposedRole(role.name()));
			return one;
		}).collect(Collectors.toList());
		resource.setUsers(users);

		ProjectConfiguration configuration = new ProjectConfiguration();
		configuration.setEntry(db.getConfiguration().getEntryType().name());
		configuration.setProjectSpecific(db.getConfiguration().getProjectSpecific().name());
		configuration.setKeepLogs(db.getConfiguration().getKeepLogs());
		configuration.setInterruptJobTime(db.getConfiguration().getInterruptJobTime());
		configuration.setKeepScreenshots(db.getConfiguration().getKeepScreenshots());
		configuration.setIsAutoAnalyzerEnabled(db.getConfiguration().getIsAutoAnalyzerEnabled());
		configuration.setAnalyzerMode(
				Optional.ofNullable(db.getConfiguration().getAnalyzerMode()).orElse(AnalyzeMode.BY_LAUNCH_NAME).getValue());
		configuration.setStatisticCalculationStrategy(db.getConfiguration().getStatisticsCalculationStrategy().name());

		// =============== EMAIL settings ===================
		configuration.setEmailConfig(EmailConfigConverters.TO_RESOURCE.apply(db.getConfiguration().getEmailConfig()));
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		// ============= External sub-types =================
		Map<String, List<IssueSubTypeResource>> result = db.getConfiguration().getSubTypes().entrySet().stream().collect(Collectors.toMap(
				entry -> entry.getKey().getValue(),
				entry -> entry.getValue().stream().map(ProjectConverter.TO_SUBTYPE_RESOURCE).collect(Collectors.toList())
		));
		configuration.setSubTypes(result);
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		resource.setConfiguration(configuration);
		return resource;
	};

	static final Function<StatisticSubType, IssueSubTypeResource> TO_SUBTYPE_RESOURCE = statisticSubType -> {
		IssueSubTypeResource issueSubTypeResource = new IssueSubTypeResource();
		issueSubTypeResource.setLocator(statisticSubType.getLocator());
		issueSubTypeResource.setTypeRef(statisticSubType.getTypeRef());
		issueSubTypeResource.setLongName(statisticSubType.getLongName());
		issueSubTypeResource.setShortName(statisticSubType.getShortName());
		issueSubTypeResource.setColor(statisticSubType.getHexColor());
		return issueSubTypeResource;
	};
}
