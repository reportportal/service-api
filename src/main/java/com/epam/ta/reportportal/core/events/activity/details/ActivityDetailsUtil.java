package com.epam.ta.reportportal.core.events.activity.details;

import com.epam.ta.reportportal.entity.ActivityDetails;
import com.epam.ta.reportportal.entity.HistoryField;
import com.google.common.base.Strings;

import javax.annotation.Nullable;

public class ActivityDetailsUtil {

	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String EMPTY_FIELD = "";
	public static final String TICKET_ID = "ticketId";
	public static final String LAUNCH_INACTIVITY = "launchInactivity";
	public static final String EMPTY_STRING = "";
	public static final String COMMENT = "comment";
	public static final String ISSUE_TYPE = "issueType";
	public static final String IGNORE_ANALYZER = "ignoreAnalyzer";
	public static final String EMAIL_STATUS = "emailEnabled";
	public static final String EMAIL_CASES = "emailCases";
	public static final String EMAIL_FROM = "from";

	public static void processName(ActivityDetails details, String oldName, String newName) {
		if (!Strings.isNullOrEmpty(newName) && !oldName.equals(newName)) {
			details.addHistoryField(HistoryField.of(NAME, oldName, newName));
		}
	}

	public static void processDescription(ActivityDetails details, @Nullable String oldDescription, @Nullable String newDescription) {
		oldDescription = Strings.nullToEmpty(oldDescription);
		newDescription = Strings.nullToEmpty(newDescription);
		if (!newDescription.equals(oldDescription)) {
			details.addHistoryField(HistoryField.of(DESCRIPTION, oldDescription, newDescription));
		}
	}
}
