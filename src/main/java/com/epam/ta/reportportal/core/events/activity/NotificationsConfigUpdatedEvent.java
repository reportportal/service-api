/*
 * Copyright 2019 EPAM Systems
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
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityAction;
import com.epam.ta.reportportal.entity.activity.ActivityDetails;
import com.epam.ta.reportportal.entity.activity.HistoryField;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import com.epam.ta.reportportal.ws.model.project.ProjectResource;
import com.epam.ta.reportportal.ws.model.project.email.ProjectNotificationConfigDTO;
import com.epam.ta.reportportal.ws.model.project.email.SenderCaseDTO;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.EMAIL_CASES;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.EMPTY_FIELD;
import static com.epam.ta.reportportal.entity.activity.Activity.ActivityEntityType.EMAIL_CONFIG;
import static java.util.Optional.ofNullable;

/**
 * @author Andrei Varabyeu
 */
public class NotificationsConfigUpdatedEvent extends BeforeEvent<ProjectResource> implements ActivityEvent {

	private ProjectNotificationConfigDTO updateProjectNotificationConfigRQ;

	public NotificationsConfigUpdatedEvent() {
	}

	public NotificationsConfigUpdatedEvent(ProjectResource before, ProjectNotificationConfigDTO updateProjectNotificationConfigRQ,
			Long userId, String userLogin) {
		super(userId, userLogin, before);
		this.updateProjectNotificationConfigRQ = updateProjectNotificationConfigRQ;
	}

	public ProjectNotificationConfigDTO getUpdateProjectNotificationConfigRQ() {
		return updateProjectNotificationConfigRQ;
	}

	public void setUpdateProjectNotificationConfigRQ(ProjectNotificationConfigDTO updateProjectNotificationConfigRQ) {
		this.updateProjectNotificationConfigRQ = updateProjectNotificationConfigRQ;
	}

	@Override
	public Activity toActivity() {
		ActivityDetails details = new ActivityDetails(getBefore().getProjectName());
		processEmailConfiguration(details, getBefore(), updateProjectNotificationConfigRQ);

		return new ActivityBuilder().addCreatedNow()
				.addAction(ActivityAction.UPDATE_PROJECT)
				.addActivityEntityType(EMAIL_CONFIG)
				.addProjectId(getBefore().getProjectId()).addUserId(getUserId()).addUserName(getUserLogin())
				.addObjectId(getBefore().getProjectId())
				.addDetails(details)
				.get();
	}

	private void processEmailConfiguration(ActivityDetails details, ProjectResource project,
			ProjectNotificationConfigDTO updateProjectNotificationConfigRQ) {
		/*
		 * Request contains EmailCases block and its not equal for stored project one
		 */

		ofNullable(project.getConfiguration().getProjectConfig()).ifPresent(cfg -> {

			List<SenderCaseDTO> before = ofNullable(cfg.getSenderCases()).orElseGet(Collections::emptyList);

			boolean isEmailCasesChanged = !before.equals(updateProjectNotificationConfigRQ.getSenderCases());

			if (!isEmailCasesChanged) {
				details.addHistoryField(HistoryField.of(EMAIL_CASES, EMPTY_FIELD, EMPTY_FIELD));
			} else {
				details.addHistoryField(HistoryField.of(
						EMAIL_CASES,
						before.stream().map(SenderCaseDTO::toString).collect(Collectors.joining(", ")),
						updateProjectNotificationConfigRQ.getSenderCases()
								.stream()
								.map(SenderCaseDTO::toString)
								.collect(Collectors.joining(", "))
				));
			}
		});

	}
}