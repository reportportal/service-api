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
package com.epam.ta.reportportal.events.handler;

import com.epam.ta.reportportal.database.dao.ActivityRepository;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.events.ProjectUpdatedEvent;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import com.epam.ta.reportportal.ws.model.project.ProjectConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.inject.Provider;
import java.util.HashMap;

/**
 * Saves new project activity
 *
 * @author Andrei Varabyeu
 */
@Component
public class ProjectActivityHandler {

	public static final String UPDATE_PROJECT = "update_project";
	public static final String KEEP_SCREENSHOTS = "keepScreenshots";
	public static final String KEEP_LOGS = "keepLogs";
	public static final String LAUNCH_INACTIVITY = "launchInactivity";
	public static final String AUTO_ANALYZE = "auto_analyze";

	private final ActivityRepository activityRepository;
	private final Provider<ActivityBuilder> activityBuilder;

	@Autowired
	public ProjectActivityHandler(Provider<ActivityBuilder> activityBuilder, ActivityRepository activityRepository) {
		this.activityBuilder = activityBuilder;
		this.activityRepository = activityRepository;
	}

	@EventListener
	public void onApplicationEvent(ProjectUpdatedEvent event) {
		Project project = event.getBefore();
		HashMap<String, Activity.FieldValues> history = new HashMap<>();

		ProjectConfiguration configuration = event.getUpdateProjectRQ().getConfiguration();
		if (null != configuration) {
			processKeepLogs(history, project, configuration);
			processKeepScreenshots(history, project, configuration);
			processLaunchInactivityTimeout(history, project, configuration);
			processAutoAnalyze(history, project, configuration);
		}

		if (!history.isEmpty()) {
			Activity activityLog = activityBuilder.get().addProjectRef(project.getName()).addObjectType(Project.PROJECT)
					.addActionType(UPDATE_PROJECT).addUserRef(event.getUpdatedBy()).build();
			activityLog.setHistory(history);
			activityRepository.save(activityLog);
		}
	}

	private void processKeepLogs(HashMap<String, Activity.FieldValues> history, Project project, ProjectConfiguration configuration) {
		if ((null != configuration.getKeepLogs()) && (!configuration.getKeepLogs().equals(project.getConfiguration().getKeepLogs()))) {
			Activity.FieldValues fieldValues = Activity.FieldValues.newOne().withOldValue(project.getConfiguration().getKeepLogs())
					.withNewValue(configuration.getKeepLogs());
			history.put(KEEP_LOGS, fieldValues);
		}
	}

	private void processLaunchInactivityTimeout(HashMap<String, Activity.FieldValues> history, Project project,
			ProjectConfiguration configuration) {
		if ((null != configuration.getInterruptJobTime()) && (!configuration.getInterruptJobTime()
				.equals(project.getConfiguration().getInterruptJobTime()))) {
			Activity.FieldValues fieldValues = Activity.FieldValues.newOne().withOldValue(project.getConfiguration().getInterruptJobTime())
					.withNewValue(configuration.getInterruptJobTime());
			history.put(LAUNCH_INACTIVITY, fieldValues);
		}
	}

	private void processAutoAnalyze(HashMap<String, Activity.FieldValues> history, Project project, ProjectConfiguration configuration) {
		if ((null != configuration.getIsAAEnabled()) && (!configuration.getIsAAEnabled()
				.equals(project.getConfiguration().getIsAutoAnalyzerEnabled()))) {
			Activity.FieldValues fieldValues = Activity.FieldValues.newOne().withOldValue(
					project.getConfiguration().getIsAutoAnalyzerEnabled() == null ?
							"" :
							project.getConfiguration().getIsAutoAnalyzerEnabled().toString())
					.withNewValue(configuration.getIsAAEnabled().toString());
			history.put(AUTO_ANALYZE, fieldValues);
		}
	}

	private void processKeepScreenshots(HashMap<String, Activity.FieldValues> history, Project project,
			ProjectConfiguration configuration) {
		if ((null != configuration.getKeepScreenshots()) && (!configuration.getKeepScreenshots()
				.equals(project.getConfiguration().getKeepScreenshots()))) {
			Activity.FieldValues fieldValues = Activity.FieldValues.newOne().withOldValue(project.getConfiguration().getKeepScreenshots())
					.withNewValue(configuration.getKeepScreenshots());
			history.put(KEEP_SCREENSHOTS, fieldValues);
		}
	}
}
