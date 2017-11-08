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
import com.epam.ta.reportportal.events.EmailConfigUpdatedEvent;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import com.epam.ta.reportportal.ws.converter.converters.EmailConfigConverters;
import com.epam.ta.reportportal.ws.model.project.email.ProjectEmailConfigDTO;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.epam.ta.reportportal.database.entity.item.ActivityEventType.UPDATE_PROJECT;
import static com.epam.ta.reportportal.database.entity.item.ActivityObjectType.PROJECT;
import static com.epam.ta.reportportal.events.handler.EventHandlerUtil.EMPTY_FIELD;
import static com.epam.ta.reportportal.events.handler.EventHandlerUtil.createHistoryField;

/**
 * Handles {@link com.epam.ta.reportportal.events.ProjectUpdatedEvent}
 *
 * @author Andrei Varabyeu
 */
@Component
public class ProjectEmailUpdatedHandler {

	private static final String EMAIL_STATUS = "emailEnabled";
	private static final String EMAIL_CASES = "emailCases";
	private static final String EMAIL_FROM = "from";

	private final ActivityRepository activityRepository;

	@Autowired
	public ProjectEmailUpdatedHandler(ActivityRepository activityRepository) {
		this.activityRepository = activityRepository;
	}

	@EventListener
	public void onProjectEmailUpdate(EmailConfigUpdatedEvent event) {
		List<Activity.FieldValues> history = Lists.newArrayList();
		final ProjectEmailConfigDTO configuration = event.getUpdateProjectEmailRQ();
		if (null != configuration) {
			processEmailConfiguration(history, event.getBefore(), configuration);
		}
		if (!history.isEmpty()) {
			Activity activityLog = new ActivityBuilder().addProjectRef(event.getBefore().getName())
					.addObjectType(PROJECT)
					.addObjectName(event.getBefore().getName())
					.addActionType(UPDATE_PROJECT)
					.addUserRef(event.getUpdatedBy())
					.addHistory(history)
					.get();
			activityRepository.save(activityLog);
		}
	}

	private void processEmailConfiguration(List<Activity.FieldValues> history, Project project, ProjectEmailConfigDTO configuration) {

		/* Has EmailEnabled trigger been updated? */
		boolean isEmailOptionChanged = configuration.getEmailEnabled() != null && !configuration.getEmailEnabled()
				.equals(project.getConfiguration().getEmailConfig().getEmailEnabled());
		/*
		 * Request contains From field update and it not equal for stored project one
		 */
		boolean isEmailFromChanged = null != configuration.getFrom() && !configuration.getFrom()
				.equalsIgnoreCase(project.getConfiguration().getEmailConfig().getFrom());
		/*
		 * Request contains EmailCases block and its not equal for stored project one
		 */
		ProjectEmailConfigDTO builtProjectEmailConfig = EmailConfigConverters.TO_RESOURCE.apply(
				project.getConfiguration().getEmailConfig());

		boolean isEmailCasesChanged =
				null != configuration.getEmailCases() && !configuration.getEmailCases().equals(builtProjectEmailConfig.getEmailCases());

		if (isEmailOptionChanged) {
			Activity.FieldValues fieldValues = createHistoryField(EMAIL_STATUS,
					String.valueOf(project.getConfiguration().getEmailConfig().getEmailEnabled()),
					String.valueOf(configuration.getEmailEnabled())
			);
			history.add(fieldValues);
		} else {
			if (isEmailCasesChanged) {
				history.add(createHistoryField(EMAIL_CASES, EMPTY_FIELD, EMPTY_FIELD));
			}
			if (isEmailFromChanged) {
				Activity.FieldValues fieldValues = createHistoryField(
						EMAIL_FROM, project.getConfiguration().getEmailConfig().getFrom(), configuration.getFrom());
				history.add(fieldValues);
			}
		}
	}
}
