package com.epam.ta.reportportal.core.events.activity;

import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityAction;
import com.epam.ta.reportportal.entity.activity.ActivityDetails;
import com.epam.ta.reportportal.entity.activity.HistoryField;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.ws.converter.converters.EmailConfigConverter;
import com.epam.ta.reportportal.ws.model.project.email.EmailSenderCaseDTO;
import com.epam.ta.reportportal.ws.model.project.email.ProjectEmailConfigDTO;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.EMAIL_CASES;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.EMPTY_FIELD;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * @author Andrei Varabyeu
 */
public class EmailConfigUpdatedEvent extends BeforeEvent<Project> implements ActivityEvent {

	private final ProjectEmailConfigDTO updateProjectEmailRQ;
	//	private final ProjectEmailConfigDTO updateProjectEmailRQ;
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
		activity.setActivityEntityType(Activity.ActivityEntityType.EMAIL_CONFIG);
		activity.setProjectId(getBefore().getId());
		activity.setUserId(updatedBy);
		activity.setObjectId(getBefore().getId());

		ActivityDetails details = new ActivityDetails(getBefore().getName());
		processEmailConfiguration(details, getBefore(), updateProjectEmailRQ);
		//		processEmailConfiguration(details, getBefore(), updateProjectEmailRQ);

		activity.setDetails(details);
		return activity;
	}

	private void processEmailConfiguration(ActivityDetails details, Project project, ProjectEmailConfigDTO configuration) {
		/*
		 * Request contains EmailCases block and its not equal for stored project one
		 */

		List<EmailSenderCaseDTO> before = ofNullable(project.getSenderCases()).map(sc -> sc.stream()
				.map(EmailConfigConverter.TO_CASE_RESOURCE)
				.collect(toList())).orElseGet(Collections::emptyList);

		boolean isEmailCasesChanged = !before.equals(configuration.getEmailCases());

		if (isEmailCasesChanged) {
			details.addHistoryField(HistoryField.of(EMAIL_CASES, EMPTY_FIELD, EMPTY_FIELD));
		} else {
			details.addHistoryField(HistoryField.of(
					EMAIL_CASES,
					before.stream().map(EmailSenderCaseDTO::toString).collect(Collectors.joining(", ")),
					configuration.getEmailCases().stream().map(EmailSenderCaseDTO::toString).collect(Collectors.joining(", "))
			));
		}

	}
}