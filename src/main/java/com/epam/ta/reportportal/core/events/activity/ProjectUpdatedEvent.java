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
package com.epam.ta.reportportal.core.events.activity;

import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.Activity;
import com.epam.ta.reportportal.entity.ActivityDetails;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.ws.model.project.ProjectConfiguration;
import com.epam.ta.reportportal.ws.model.project.UpdateProjectRQ;

import java.time.LocalDateTime;

/**
 * Being triggered on after project update
 *
 * @author Andrei Varabyeu
 */
public class ProjectUpdatedEvent extends AroundEvent<Project> implements ActivityEvent {

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
		activity.setActivityEntityType(Activity.ActivityEntityType.PROJECT);
		activity.setProjectId(getAfter().getId());
		activity.setUserId(updatedBy);

		ActivityDetails details = new ActivityDetails(getAfter().getName());
		ProjectConfiguration configuration = updateProjectRQ.getConfiguration();
		if (null != configuration) {
			processKeepLogs(details, getBefore(), configuration);
			processKeepScreenshots(details, getBefore(), configuration);
			processLaunchInactivityTimeout(details, getBefore(), configuration);
		}
		if (!details.getHistory().isEmpty()) {
			activity.setDetails(details);
		}
		return activity;
	}

	private void processLaunchInactivityTimeout(ActivityDetails details, Project project, ProjectConfiguration configuration) {
		/*if ((null != configuration.getInterruptJobTime()) && (!configuration.getInterruptJobTime()
				.equals(project.getConfiguration().getInterruptJobTime()))) {
			details.addHistoryField(HistoryField.of(LAUNCH_INACTIVITY, project.getConfiguration().getInterruptJobTime(),
					configuration.getInterruptJobTime()
			));
		}*/
	}

	private void processKeepScreenshots(ActivityDetails details, Project project, ProjectConfiguration configuration) {
		/*if ((null != configuration.getKeepScreenshots()) && (!configuration.getKeepScreenshots()
				.equals(project.getConfiguration().getKeepScreenshots()))) {
			details.addHistoryField(HistoryField.of(KEEP_SCREENSHOTS.getValue(), project.getConfiguration().getKeepScreenshots(),
					configuration.getKeepScreenshots()
			));
		}*/
	}

	private void processKeepLogs(ActivityDetails details, Project project, ProjectConfiguration configuration) {
		/*if ((null != configuration.getKeepLogs()) && (!configuration.getKeepLogs().equals(project.getConfiguration().getKeepLogs()))) {
			details.addHistoryField(HistoryField.of(KEEP_LOGS.getValue(),
					project.getConfiguration().getKeepLogs(),
					configuration.getKeepLogs()
			));
		}*/
	}
}
