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
import com.epam.ta.reportportal.entity.HistoryField;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.ws.model.project.ProjectConfiguration;
import com.epam.ta.reportportal.ws.model.project.UpdateProjectRQ;
import com.google.common.base.Strings;

import java.time.LocalDateTime;

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.LAUNCH_INACTIVITY;
import static com.epam.ta.reportportal.core.events.activity.util.ProjectAttributeUtil.extractAttributeValue;
import static com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum.*;

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
			processKeepLogs(details, configuration);
			processKeepScreenshots(details, configuration);
			processLaunchInactivityTimeout(details, configuration);
		}
		if (!details.getHistory().isEmpty()) {
			activity.setDetails(details);
		}
		return activity;
	}

	private void processLaunchInactivityTimeout(ActivityDetails details, ProjectConfiguration configuration) {
		String oldValue = extractAttributeValue(getBefore(), INTERRUPT_JOB_TIME.getAttribute());
		if ((null != configuration.getInterruptJobTime()) && !Strings.isNullOrEmpty(oldValue) && (!configuration.getInterruptJobTime()
				.equals(oldValue))) {
			details.addHistoryField(HistoryField.of(LAUNCH_INACTIVITY, oldValue, configuration.getInterruptJobTime()));
		}
	}

	private void processKeepScreenshots(ActivityDetails details, ProjectConfiguration configuration) {
		String oldValue = extractAttributeValue(getBefore(), KEEP_SCREENSHOTS.getAttribute());
		if ((null != configuration.getKeepScreenshots()) && !Strings.isNullOrEmpty(oldValue) && (!configuration.getKeepScreenshots()
				.equals(oldValue))) {
			details.addHistoryField(HistoryField.of(KEEP_SCREENSHOTS.getAttribute(), oldValue, configuration.getKeepScreenshots()));
		}
	}

	private void processKeepLogs(ActivityDetails details, ProjectConfiguration configuration) {
		String oldValue = extractAttributeValue(getBefore(), KEEP_LOGS.getAttribute());
		if ((null != configuration.getKeepLogs()) && !Strings.isNullOrEmpty(oldValue) && (!configuration.getKeepLogs().equals(oldValue))) {
			details.addHistoryField(HistoryField.of(KEEP_LOGS.getAttribute(), oldValue, configuration.getKeepLogs()));
		}
	}
}
