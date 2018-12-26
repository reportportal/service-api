package com.epam.ta.reportportal.core.events.activity;

import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityAction;
import com.epam.ta.reportportal.entity.activity.ActivityDetails;
import com.epam.ta.reportportal.entity.activity.HistoryField;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.ws.converter.converters.EmailConfigConverter;
import com.epam.ta.reportportal.ws.model.project.email.ProjectEmailConfigDTO;

import java.time.LocalDateTime;
import java.util.Map;

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.*;
import static com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum.EMAIL_ENABLED;
import static com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum.EMAIL_FROM;

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
		Map<String, String> configParameters = ProjectUtils.getConfigParameters(getBefore().getProjectAttributes());

		/*
		 * Has EmailEnabled trigger been updated?
		 */
		boolean isEmailOptionChanged = configuration.getEmailEnabled() != null && !configuration.getEmailEnabled()
				.equals(Boolean.valueOf(configParameters.get(EMAIL_ENABLED.getAttribute())));

		/*
		 * Request contains From field update and it not equal for stored project one
		 */
		boolean isEmailFromChanged = null != configuration.getFrom() && !configuration.getFrom()
				.equalsIgnoreCase(configParameters.get(EMAIL_FROM.getAttribute()));

		/*
		 * Request contains EmailCases block and its not equal for stored project one
		 */
		ProjectEmailConfigDTO builtProjectEmailConfig = EmailConfigConverter.TO_RESOURCE.apply(project.getProjectAttributes(),
				project.getEmailCases()
		);

		boolean isEmailCasesChanged =
				null != configuration.getEmailCases() && !configuration.getEmailCases().equals(builtProjectEmailConfig.getEmailCases());

		if (isEmailOptionChanged) {
			details.addHistoryField(HistoryField.of(EMAIL_STATUS,
					configParameters.get(EMAIL_ENABLED.getAttribute()),
					String.valueOf(configuration.getEmailEnabled())
			));
		}

		if (isEmailCasesChanged) {
			details.addHistoryField(HistoryField.of(EMAIL_CASES, EMPTY_FIELD, EMPTY_FIELD));
		}

		if (isEmailFromChanged) {
			details.addHistoryField(HistoryField.of(EMAIL_FROM.getAttribute(),
					configParameters.get(EMAIL_FROM.getAttribute()),
					configuration.getFrom()
			));
		}

	}
}