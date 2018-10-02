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
import com.epam.ta.reportportal.ws.converter.converters.EmailConfigConverter;
import com.epam.ta.reportportal.ws.model.project.email.ProjectEmailConfigDTO;

import java.time.LocalDateTime;

/**
 * @author Andrei Varabyeu
 */
public class EmailConfigUpdatedEvent extends BeforeEvent<Project> implements ActivityEvent {

	private static final String EMAIL_STATUS = "emailEnabled";
	private static final String EMAIL_CASES = "emailCases";
	private static final String EMAIL_FROM = "from";
	private static final String EMPTY_FIELD = "";

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

		ActivityDetails details = new ActivityDetails();
		processEmailConfiguration(details, getBefore(), updateProjectEmailRQ);

		activity.setDetails(details);
		return activity;
	}

	private void processEmailConfiguration(ActivityDetails details, Project project, ProjectEmailConfigDTO configuration) {

		/* Has EmailEnabled trigger been updated? */
		boolean isEmailOptionChanged = configuration.getEmailEnabled() != null && !configuration.getEmailEnabled()
				.equals(project.getConfiguration().getProjectEmailConfig().getEmailEnabled());
		/*
		 * Request contains From field update and it not equal for stored project one
		 */
		boolean isEmailFromChanged = null != configuration.getFrom() && !configuration.getFrom()
				.equalsIgnoreCase(project.getConfiguration().getProjectEmailConfig().getFrom());
		/*
		 * Request contains EmailCases block and its not equal for stored project one
		 */

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
	}
}
