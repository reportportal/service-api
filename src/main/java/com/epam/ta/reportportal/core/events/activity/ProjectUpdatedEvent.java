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
package com.epam.ta.reportportal.core.events.activity;

import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.core.events.activity.details.ActivityDetails;
import com.epam.ta.reportportal.core.events.activity.details.HistoryField;
import com.epam.ta.reportportal.entity.Activity;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.ws.model.project.ProjectConfiguration;
import com.epam.ta.reportportal.ws.model.project.UpdateProjectRQ;

import java.time.LocalDateTime;

import static com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum.KEEP_LOGS;
import static com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum.KEEP_SCREENSHOTS;

/**
 * Being triggered on after project update
 *
 * @author Andrei Varabyeu
 */
public class ProjectUpdatedEvent extends AroundEvent<Project> implements ActivityEvent {

	static final String LAUNCH_INACTIVITY = "launchInactivity";

	private final Long updatedBy;
	private final UpdateProjectRQ updateProjectRQ;

	/**
	 * Create a new ApplicationEvent.
	 *
	 * @param before Project before update
	 * @param after  Project after update
	 */
	public ProjectUpdatedEvent(Project before, Project after, Long updatedBy, UpdateProjectRQ updateProjectRQ) {
		super(before, after);
		this.updatedBy = updatedBy;
		this.updateProjectRQ = updateProjectRQ;
	}

	@Override
	public Activity toActivity() {
		Activity activity = new Activity();
		activity.setCreatedAt(LocalDateTime.now());
		activity.setAction(ActivityAction.UPDATE_PROJECT.getValue());
		activity.setEntity(Activity.Entity.PROJECT);
		activity.setProjectId(getAfter().getId());
		activity.setUserId(updatedBy);

		ActivityDetails details = new ActivityDetails();
		ProjectConfiguration configuration = updateProjectRQ.getConfiguration();
		if (null != configuration) {
			processKeepLogs(details, getBefore(), configuration);
			processKeepScreenshots(details, getBefore(), configuration);
			processLaunchInactivityTimeout(details, getBefore(), configuration);
		}
		if (!details.isEmpty()) {
			activity.setDetails(details);
		}
		return activity;
	}

	private void processLaunchInactivityTimeout(ActivityDetails details, Project project, ProjectConfiguration configuration) {
		if ((null != configuration.getInterruptJobTime()) && (!configuration.getInterruptJobTime()
				.equals(project.getConfiguration().getInterruptJobTime()))) {
			HistoryField historyField = new HistoryField(project.getConfiguration().getInterruptJobTime(),
					configuration.getInterruptJobTime()
			);
			details.addHistoryField(LAUNCH_INACTIVITY, historyField);
		}
	}

	private void processKeepScreenshots(ActivityDetails details, Project project, ProjectConfiguration configuration) {
		if ((null != configuration.getKeepScreenshots()) && (!configuration.getKeepScreenshots()
				.equals(project.getConfiguration().getKeepScreenshots()))) {
			HistoryField historyField = new HistoryField(project.getConfiguration().getKeepScreenshots(),
					configuration.getKeepScreenshots()
			);
			details.addHistoryField(KEEP_SCREENSHOTS.getValue(), historyField);
		}
	}

	private void processKeepLogs(ActivityDetails details, Project project, ProjectConfiguration configuration) {
		if ((null != configuration.getKeepLogs()) && (!configuration.getKeepLogs().equals(project.getConfiguration().getKeepLogs()))) {
			HistoryField historyField = new HistoryField(project.getConfiguration().getKeepLogs(), configuration.getKeepLogs());
			details.addHistoryField(KEEP_LOGS.getValue(), historyField);
		}
	}
}
