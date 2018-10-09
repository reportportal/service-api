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
import com.epam.ta.reportportal.ws.model.project.email.ProjectEmailConfigDTO;

import java.time.LocalDateTime;

/**
 * @author Andrei Varabyeu
 */
public class EmailConfigUpdatedEvent extends BeforeEvent<Project> implements ActivityEvent {

	private final ProjectEmailConfigDTO updateProjectEmailRQ;
	private final Long updatedBy;

	public EmailConfigUpdatedEvent(Project before, ProjectEmailConfigDTO updateProjectEmailRQ, Long updatedBy) {
		super(before);
		this.updateProjectEmailRQ = updateProjectEmailRQ;
		this.updatedBy = updatedBy;
	}

	@Override
	public Activity toActivity() {
		Activity activity = new Activity();
		activity.setCreatedAt(LocalDateTime.now());
		activity.setAction(ActivityAction.UPDATE_PROJECT.getValue());
		activity.setEntity(Activity.Entity.EMAIL_CONFIG);
		activity.setProjectId(getBefore().getId());
		activity.setUserId(updatedBy);
		activity.setObjectId(getBefore().getId());

		ActivityDetails details = new ActivityDetails(getBefore().getName());
		//		processEmailConfiguration(details, getBefore(), updateProjectEmailRQ);

		activity.setDetails(details);
		return activity;
	}

	/*private void processEmailConfiguration(ActivityDetails details, Project project, ProjectEmailConfigDTO configuration) {

	 *//* Has EmailEnabled trigger been updated? *//*
		boolean isEmailOptionChanged = configuration.getEmailEnabled() != null && !configuration.getEmailEnabled()
				.equals(project.getConfiguration().getProjectEmailConfig().getEmailEnabled());
		*//*
		 * Request contains From field update and it not equal for stored project one
	 *//*
		boolean isEmailFromChanged = null != configuration.getFrom() && !configuration.getFrom()
				.equalsIgnoreCase(project.getConfiguration().getProjectEmailConfig().getFrom());
		*//*
		 * Request contains EmailCases block and its not equal for stored project one
	 *//*

		ProjectEmailConfigDTO builtProjectEmailConfig = EmailConfigConverter.TO_RESOURCE.apply(project.getProjectAttributes(),
				project.getEmailCases()
		);

		boolean isEmailCasesChanged =
				null != configuration.getEmailCases() && !configuration.getEmailCases().equals(builtProjectEmailConfig.getEmailCases());

		if (isEmailOptionChanged) {
			HistoryField historyField = new HistoryField(String.valueOf(project.getConfiguration()
					.getProjectEmailConfig().getEmailEnabled()),
					String.valueOf(configuration.getEmailEnabled())
			);
			details.addHistoryField(EMAIL_STATUS, historyField);
		} else {
			if (isEmailCasesChanged) {
				details.addHistoryField(EMAIL_CASES, new HistoryField(EMPTY_FIELD, EMPTY_FIELD));
			}
			if (isEmailFromChanged) {
				HistoryField historyField = new HistoryField(project.getConfiguration().getProjectEmailConfig().getFrom(),
						configuration.getFrom()
				);
				details.addHistoryField(EMAIL_FROM, historyField);
			}
		}
	}*/
}
