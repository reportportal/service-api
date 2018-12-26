/*
 * Copyright 2018 EPAM Systems
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

import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityDetails;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class ActivityTestHelper {
	static final Long USER_ID = 1L;
	static final Long OBJECT_ID = 2L;
	static final Long PROJECT_ID = 3L;
	static final String PROJECT_NAME = "Test Project";
	static final String OLD_NAME = "oldName";
	static final String NEW_NAME = "newName";
	static final boolean OLD_SHARED = false;
	static final boolean NEW_SHARED = true;
	static final String OLD_DESCRIPTION = "oldDescription";
	static final String NEW_DESCRIPTION = "newDescription";
	static final String NAME_FIELD = "name";
	static final String SHARE_FIELD = "share";
	static final String DESCRIPTION_FIELD = "description";
	static final String STATUS_FIELD = "status";
	static final String ITEMS_COUNT_FIELD = "itemsCount";
	static final String CONTENT_FIELDS_FIELD = "contentFields";
	static final String WIDGET_OPTIONS_FIELD = "widgetOptions";
	static final String TICKET_ID_FIELD = "ticketId";
	public static final String COMMENT_FIELD = "comment";
	public static final String ISSUE_TYPE_FIELD = "issueType";
	public static final String IGNORE_ANALYZER_FIELD = "ignoreAnalyzer";

	static void assertActivity(Activity expected, Activity actual) {
		assertThat(actual).isEqualToIgnoringGivenFields(expected, "details", "createdAt");
		checkActivityDetails(expected.getDetails(), actual.getDetails());
		checkCreatedAt(expected.getCreatedAt(), expected.getCreatedAt());
	}

	private static void checkActivityDetails(ActivityDetails expected, ActivityDetails actual) {
		assertThat(actual.getObjectName()).isEqualTo(expected.getObjectName());
		assertThat(actual.getHistory()).containsExactlyInAnyOrderElementsOf(expected.getHistory());
	}

	private static void checkCreatedAt(LocalDateTime expected, LocalDateTime actual) {
		assertThat(expected.minusNanos(50).isBefore(actual) && expected.plusNanos(50).isAfter(actual)).isTrue();
	}
}
