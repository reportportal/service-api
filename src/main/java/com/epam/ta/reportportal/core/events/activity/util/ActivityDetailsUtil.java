/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.ta.reportportal.core.events.activity.util;

/**
 * @author Ihar Kahadouski
 */

import com.epam.ta.reportportal.entity.ActivityDetails;
import com.epam.ta.reportportal.entity.HistoryField;
import com.google.common.base.Strings;

public class ActivityDetailsUtil {

	private ActivityDetailsUtil() {
		//static only
	}

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

	public static void processDescription(ActivityDetails details, String oldDescription, String newDescription) {
		oldDescription = Strings.nullToEmpty(oldDescription);
		newDescription = Strings.nullToEmpty(newDescription);
		if (!newDescription.equals(oldDescription)) {
			details.addHistoryField(HistoryField.of(DESCRIPTION, oldDescription, newDescription));
		}
	}
}
