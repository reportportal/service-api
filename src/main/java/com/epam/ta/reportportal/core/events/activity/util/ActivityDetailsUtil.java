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
package com.epam.ta.reportportal.core.events.activity.util;

/**
 * @author Ihar Kahadouski
 */

import com.epam.ta.reportportal.entity.activity.HistoryField;
import com.google.common.base.Strings;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ActivityDetailsUtil {

	private ActivityDetailsUtil() {
		//static only
	}

	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String SHARE = "share";
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
	public static final String ITEMS_COUNT = "itemsCount";
	public static final String CONTENT_FIELDS = "contentFields";
	public static final String WIDGET_OPTIONS = "widgetOptions";
	public static final String STATUS = "status";
	public static final String RELEVANT_ITEM = "relevantItem";
	public static final String ENABLED = "enabled";
	public static final String ITEM_IDS = "itemIds";
	public static final String LAUNCH_ID = "launchId";

	public static Optional<HistoryField> processName(String oldName, String newName) {
		if (!Strings.isNullOrEmpty(newName) && !oldName.equals(newName)) {
			return Optional.of(HistoryField.of(NAME, oldName, newName));
		}
		return Optional.empty();
	}

	public static Optional<HistoryField> processDescription(String oldDescription, String newDescription) {
		oldDescription = Strings.nullToEmpty(oldDescription);
		newDescription = Strings.nullToEmpty(newDescription);
		if (!newDescription.equals(oldDescription)) {
			return Optional.of(HistoryField.of(DESCRIPTION, oldDescription, newDescription));
		}
		return Optional.empty();
	}

	public static Optional<HistoryField> processBoolean(String type, boolean previous, boolean current) {
		if (previous != current) {
			return Optional.of(HistoryField.of(type, String.valueOf(previous), String.valueOf(current)));
		}
		return Optional.empty();
	}

	public static Optional<HistoryField> processParameter(Map<String, String> oldConfig, Map<String, String> newConfig,
			String parameterName) {
		String before = oldConfig.get(parameterName);
		String after = newConfig.get(parameterName);
		if (after != null && !after.equals(before)) {
			return Optional.of(HistoryField.of(parameterName, before, after));
		}
		return Optional.empty();
	}

	public static boolean configEquals(Map<String, String> before, Map<String, String> after, String prefix) {
		Map<String, String> beforeJobConfig = extractConfigByPrefix(before, prefix);
		Map<String, String> afterJobConfig = extractConfigByPrefix(after, prefix);
		return beforeJobConfig.equals(afterJobConfig);
	}

	private static Map<String, String> extractConfigByPrefix(Map<String, String> config, String prefix) {
		return config.entrySet()
				.stream()
				.filter(entry -> entry.getKey().startsWith(prefix))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}
}
