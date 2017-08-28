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
package com.epam.ta.reportportal.events.handler;

import com.epam.ta.reportportal.database.dao.ActivityRepository;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.events.ProjectUpdatedEvent;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import com.epam.ta.reportportal.ws.model.project.ProjectConfiguration;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.epam.ta.reportportal.database.entity.item.ActivityEventType.UPDATE_PROJECT;
import static com.epam.ta.reportportal.database.entity.item.ActivityObjectType.PROJECT;

/**
 * Saves new project activity
 *
 * @author Andrei Varabyeu
 */
@Component
public class ProjectActivityHandler {

	public static final String KEEP_SCREENSHOTS = "keepScreenshots";
	public static final String KEEP_LOGS = "keepLogs";
	public static final String LAUNCH_INACTIVITY = "launchInactivity";
	public static final String STATISTICS_CALCULATION_STRATEGY = "statisticsCalculationStrategy";
	public static final String AUTO_ANALYZE = "auto_analyze";

	private final ActivityRepository activityRepository;

	@Autowired
	public ProjectActivityHandler(ActivityRepository activityRepository) {
		this.activityRepository = activityRepository;
	}

	@EventListener
	public void onApplicationEvent(ProjectUpdatedEvent event) {
		Project project = event.getBefore();
		List<Activity.FieldValues> history = Lists.newArrayList();

		ProjectConfiguration configuration = event.getUpdateProjectRQ().getConfiguration();
		if (null != configuration) {
			processKeepLogs(history, project, configuration);
			processKeepScreenshots(history, project, configuration);
			processLaunchInactivityTimeout(history, project, configuration);
			processAutoAnalyze(history, project, configuration);
			processStatisticsStrategy(history, project, configuration);
		}

		if (!history.isEmpty()) {
			Activity activityLog = new ActivityBuilder()
                    .addProjectRef(project.getName())
                    .addObjectType(PROJECT)
					.addActionType(UPDATE_PROJECT)
                    .addUserRef(event.getUpdatedBy())
                    .addHistory(history)
                    .get();
			activityRepository.save(activityLog);
		}
	}

	private void processStatisticsStrategy(List<Activity.FieldValues> history, Project project, ProjectConfiguration configuration) {
		if ((null != configuration.getStatisticCalculationStrategy())
				&& (!configuration.getStatisticCalculationStrategy().equalsIgnoreCase((project.getConfiguration().getStatisticsCalculationStrategy().name())))){
			Activity.FieldValues fieldValues = Activity.FieldValues.newOne()
                    .withField(STATISTICS_CALCULATION_STRATEGY)
                    .withOldValue(project.getConfiguration().getStatisticsCalculationStrategy().name())
					.withNewValue(configuration.getStatisticCalculationStrategy());
			history.add(fieldValues);
		}
	}

	private void processKeepLogs(List<Activity.FieldValues> history, Project project, ProjectConfiguration configuration) {
		if ((null != configuration.getKeepLogs()) && (!configuration.getKeepLogs().equals(project.getConfiguration().getKeepLogs()))) {
			Activity.FieldValues fieldValues = Activity.FieldValues.newOne()
                    .withField(KEEP_LOGS)
                    .withOldValue(project.getConfiguration().getKeepLogs())
					.withNewValue(configuration.getKeepLogs());
			history.add(fieldValues);
		}
	}

	private void processLaunchInactivityTimeout(List<Activity.FieldValues> history, Project project,
			ProjectConfiguration configuration) {
		if ((null != configuration.getInterruptJobTime()) && (!configuration.getInterruptJobTime()
				.equals(project.getConfiguration().getInterruptJobTime()))) {
			Activity.FieldValues fieldValues = Activity.FieldValues.newOne()
                    .withField(LAUNCH_INACTIVITY)
                    .withOldValue(project.getConfiguration().getInterruptJobTime())
					.withNewValue(configuration.getInterruptJobTime());
			history.add(fieldValues);
		}
	}

	private void processAutoAnalyze(List<Activity.FieldValues> history, Project project, ProjectConfiguration configuration) {
		if ((null != configuration.getIsAAEnabled()) && (!configuration.getIsAAEnabled()
				.equals(project.getConfiguration().getIsAutoAnalyzerEnabled()))) {
			Activity.FieldValues fieldValues = Activity.FieldValues.newOne()
                    .withField(AUTO_ANALYZE)
                    .withOldValue(project.getConfiguration().getIsAutoAnalyzerEnabled() == null ? "" :
							project.getConfiguration().getIsAutoAnalyzerEnabled().toString())
					.withNewValue(configuration.getIsAAEnabled().toString());
			history.add(fieldValues);
		}
	}

	private void processKeepScreenshots(List<Activity.FieldValues> history, Project project,
			ProjectConfiguration configuration) {
		if ((null != configuration.getKeepScreenshots()) && (!configuration.getKeepScreenshots()
				.equals(project.getConfiguration().getKeepScreenshots()))) {
			Activity.FieldValues fieldValues = Activity.FieldValues.newOne()
                    .withField(KEEP_SCREENSHOTS)
                    .withOldValue(project.getConfiguration().getKeepScreenshots())
					.withNewValue(configuration.getKeepScreenshots());
			history.add(fieldValues);
		}
	}
}
