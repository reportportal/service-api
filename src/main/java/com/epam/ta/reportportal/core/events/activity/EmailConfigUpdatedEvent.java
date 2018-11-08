/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import java.time.LocalDateTime;

/**
 * @author Andrei Varabyeu
 */
public class EmailConfigUpdatedEvent extends BeforeEvent<Project> implements ActivityEvent {

	//	private final ProjectEmailConfigDTO updateProjectEmailRQ;
	private final Long updatedBy;

	public EmailConfigUpdatedEvent(Project before, Long updatedBy) {
		super(before);
		//		this.updateProjectEmailRQ = updateProjectEmailRQ;
		this.updatedBy = updatedBy;
	}

	@Override
	public Activity toActivity() {
		Activity activity = new Activity();
		activity.setCreatedAt(LocalDateTime.now());
		activity.setAction(ActivityAction.UPDATE_PROJECT.getValue());
		activity.setActivityEntityType(Activity.ActivityEntityType.EMAIL_CONFIG);
		activity.setProjectId(getBefore().getId());
		activity.setUserId(updatedBy);
		activity.setObjectId(getBefore().getId());

		ActivityDetails details = new ActivityDetails(getBefore().getName());
		//		processEmailConfiguration(details, getBefore(), updateProjectEmailRQ);

		activity.setDetails(details);
		return activity;
	}

	//	private void processEmailConfiguration(ActivityDetails details, Project project, ProjectEmailConfigDTO configuration) {
	//		Map<String, String> configParameters = ProjectUtils.getConfigParameters(getBefore().getProjectAttributes());
	//
	//		/*
	//		 * Has EmailEnabled trigger been updated?
	//		 */
	//		boolean isEmailOptionChanged = configuration.getEmailEnabled() != null && !configuration.getEmailEnabled()
	//				.equals(Boolean.valueOf(configParameters.get(EMAIL_ENABLED.getAttribute())));
	//
	//		/*
	//		 * Request contains From field update and it not equal for stored project one
	//		 */
	//		boolean isEmailFromChanged = null != configuration.getFrom() && !configuration.getFrom()
	//				.equalsIgnoreCase(configParameters.get(EMAIL_FROM.getAttribute()));
	//
	//		/*
	//		 * Request contains EmailCases block and its not equal for stored project one
	//		 */
	//		ProjectEmailConfigDTO builtProjectEmailConfig = EmailConfigConverter.TO_RESOURCE.apply(project.getProjectAttributes(),
	//				project.getEmailCases()
	//		);
	//
	//		boolean isEmailCasesChanged =
	//				null != configuration.getEmailCases() && !configuration.getEmailCases().equals(builtProjectEmailConfig.getEmailCases());
	//
	//		if (isEmailOptionChanged) {
	//			details.addHistoryField(HistoryField.of(EMAIL_STATUS, configParameters.get(EMAIL_ENABLED.getAttribute()),
	//					String.valueOf(configuration.getEmailEnabled())
	//			));
	//		}
	//
	//		if (isEmailCasesChanged) {
	//			details.addHistoryField(HistoryField.of(EMAIL_CASES, EMPTY_FIELD, EMPTY_FIELD));
	//		}
	//
	//		if (isEmailFromChanged) {
	//			details.addHistoryField(HistoryField.of(EMAIL_FROM.getAttribute(), configParameters.get(EMAIL_FROM.getAttribute()),
	//					configuration.getFrom()
	//			));
	//		}
	//
	//	}
}
