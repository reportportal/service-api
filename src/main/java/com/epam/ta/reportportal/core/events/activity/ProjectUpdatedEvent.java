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
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.ws.model.project.ProjectConfiguration;
import com.epam.ta.reportportal.ws.model.project.UpdateProjectRQ;
import com.google.common.base.Strings;

import java.time.LocalDateTime;
import java.util.Map;

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.LAUNCH_INACTIVITY;
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
		Map<String, String> existedConfig = ProjectUtils.getConfigParameters(getAfter().getProjectAttributes());
		ProjectConfiguration updatedConfig = updateProjectRQ.getConfiguration();
		if (null != updatedConfig && null != updatedConfig.getProjectAttributes()) {
			processKeepLogs(details, existedConfig, updatedConfig.getProjectAttributes());
			processKeepScreenshots(details, existedConfig, updatedConfig.getProjectAttributes());
			processLaunchInactivityTimeout(details, existedConfig, updatedConfig.getProjectAttributes());
		}
		if (!details.getHistory().isEmpty()) {
			activity.setDetails(details);
		}
		return activity;
	}

	private void processLaunchInactivityTimeout(ActivityDetails details, Map<String, String> existedConfig,
			Map<String, String> updatedAttributes) {
		processField(
				details,
				LAUNCH_INACTIVITY,
				existedConfig.get(INTERRUPT_JOB_TIME.getAttribute()),
				updatedAttributes.get(INTERRUPT_JOB_TIME.getAttribute())
		);
	}

	private void processKeepScreenshots(ActivityDetails details, Map<String, String> existedConfig, Map<String, String> updatedAttributes) {
		processField(
				details,
				KEEP_SCREENSHOTS.getAttribute(),
				existedConfig.get(KEEP_SCREENSHOTS.getAttribute()),
				updatedAttributes.get(KEEP_SCREENSHOTS.getAttribute())
		);
	}

	private void processKeepLogs(ActivityDetails details, Map<String, String> existedConfig, Map<String, String> updatedAttributes) {
		processField(
				details,
				KEEP_LOGS.getAttribute(),
				existedConfig.get(KEEP_LOGS.getAttribute()),
				updatedAttributes.get(KEEP_LOGS.getAttribute())
		);
	}

	private void processField(ActivityDetails details, String field, String oldValue, String newValue) {
		if ((null != newValue) && !Strings.isNullOrEmpty(oldValue) && (!newValue.equals(oldValue))) {
			details.addHistoryField(HistoryField.of(field, oldValue, newValue));
		}
	}
}
