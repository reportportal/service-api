package com.epam.ta.reportportal.core.events.activity;

import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityAction;
import com.epam.ta.reportportal.entity.activity.ActivityDetails;
import com.epam.ta.reportportal.entity.activity.HistoryField;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.ws.converter.converters.EmailConfigConverter;
import com.epam.ta.reportportal.ws.model.project.email.ProjectConfigDTO;
import com.epam.ta.reportportal.ws.model.project.email.SenderCaseDTO;

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

	private final ProjectConfigDTO updateProjectConfigRQ;
	private final Long updatedBy;

	public EmailConfigUpdatedEvent(Project before, ProjectConfigDTO updateProjectConfigRQ, Long updatedBy) {
		super(before);
		this.updateProjectConfigRQ = updateProjectConfigRQ;
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
		processEmailConfiguration(details, getBefore(), updateProjectConfigRQ);

		activity.setDetails(details);
		return activity;
	}

	private void processEmailConfiguration(ActivityDetails details, Project project, ProjectConfigDTO configuration) {
		/*
		 * Request contains EmailCases block and its not equal for stored project one
		 */

		List<SenderCaseDTO> before = ofNullable(project.getSenderCases()).map(sc -> sc.stream()
				.map(EmailConfigConverter.TO_CASE_RESOURCE)
				.collect(toList())).orElseGet(Collections::emptyList);

		boolean isEmailCasesChanged = !before.equals(configuration.getSenderCases());

		if (isEmailCasesChanged) {
			details.addHistoryField(HistoryField.of(EMAIL_CASES, EMPTY_FIELD, EMPTY_FIELD));
		} else {
			details.addHistoryField(HistoryField.of(
					EMAIL_CASES,
					before.stream().map(SenderCaseDTO::toString).collect(Collectors.joining(", ")),
					configuration.getSenderCases().stream().map(SenderCaseDTO::toString).collect(Collectors.joining(", "))
			));
		}

	}
}