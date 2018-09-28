package com.epam.ta.reportportal.core.events.activity.details;

import com.google.common.base.Strings;

import javax.annotation.Nullable;

public class ActivityDetailsUtil {

	private static final String NAME = "name";
	private static final String DESCRIPTION = "description";

	public static void processName(ActivityDetails details, String oldName, String newName) {
		if (!Strings.isNullOrEmpty(newName) && !oldName.equals(newName)) {
			details.addHistoryField(NAME, new HistoryField<String>(oldName, newName));
		}
	}

	public static void processDescription(ActivityDetails details, @Nullable String oldDescription, @Nullable String newDescription) {
		oldDescription = Strings.nullToEmpty(oldDescription);
		newDescription = Strings.nullToEmpty(newDescription);
		if (!newDescription.equals(oldDescription)) {
			details.addHistoryField(DESCRIPTION, new HistoryField(oldDescription, newDescription));
		}
	}
}
