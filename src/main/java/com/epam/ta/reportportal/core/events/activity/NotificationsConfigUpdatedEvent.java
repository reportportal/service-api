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
import com.epam.ta.reportportal.ws.model.project.ProjectResource;
import com.epam.ta.reportportal.ws.model.project.email.ProjectNotificationConfigDTO;
import com.epam.ta.reportportal.ws.model.project.email.SenderCaseDTO;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.EMAIL_CASES;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.EMPTY_FIELD;
import static java.util.Optional.ofNullable;

/**
 * @author Andrei Varabyeu
 */
public class NotificationsConfigUpdatedEvent extends BeforeEvent<ProjectResource> implements ActivityEvent {

	private ProjectNotificationConfigDTO updateProjectNotificationConfigRQ;
	private Long updatedBy;

	public NotificationsConfigUpdatedEvent() {
	}

	public NotificationsConfigUpdatedEvent(ProjectResource before, ProjectNotificationConfigDTO updateProjectNotificationConfigRQ, Long updatedBy) {
		super(before);
		this.updateProjectNotificationConfigRQ = updateProjectNotificationConfigRQ;
		this.updatedBy = updatedBy;
	}

	public ProjectNotificationConfigDTO getUpdateProjectNotificationConfigRQ() {
		return updateProjectNotificationConfigRQ;
	}

	public void setUpdateProjectNotificationConfigRQ(ProjectNotificationConfigDTO updateProjectNotificationConfigRQ) {
		this.updateProjectNotificationConfigRQ = updateProjectNotificationConfigRQ;
	}

	public Long getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(Long updatedBy) {
		this.updatedBy = updatedBy;
	}

	@Override
	public Activity toActivity() {
		Activity activity = new Activity();
		activity.setCreatedAt(LocalDateTime.now());
		activity.setAction(ActivityAction.UPDATE_PROJECT.getValue());
		activity.setActivityEntityType(Activity.ActivityEntityType.EMAIL_CONFIG.getValue());
		activity.setProjectId(getBefore().getProjectId());
		activity.setUserId(updatedBy);
		activity.setObjectId(getBefore().getProjectId());

		ActivityDetails details = new ActivityDetails(getBefore().getProjectName());
		processEmailConfiguration(details, getBefore(), updateProjectNotificationConfigRQ);

		activity.setDetails(details);
		return activity;
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