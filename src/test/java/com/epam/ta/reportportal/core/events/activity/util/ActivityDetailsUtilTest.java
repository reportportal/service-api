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

package com.epam.ta.reportportal.core.events.activity.util;

import com.epam.ta.reportportal.entity.activity.HistoryField;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.SHARE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class ActivityDetailsUtilTest {

	private static final String OLD_VALUE = "oldValue";
	private static final String NEW_VALUE = "newValue";

	@Test
	void processName() {
		final Optional<HistoryField> historyField = ActivityDetailsUtil.processName(OLD_VALUE, NEW_VALUE);
		assertTrue(historyField.isPresent());
		assertThat(historyField.get()).isEqualToIgnoringGivenFields(getExpected("name"), "mapper");
	}

	@Test
	void processNameReturnEmpty() {
		final Optional<HistoryField> sameValue = ActivityDetailsUtil.processName(OLD_VALUE, OLD_VALUE);
		assertFalse(sameValue.isPresent());
		final Optional<HistoryField> newNull = ActivityDetailsUtil.processName(OLD_VALUE, null);
		assertFalse(newNull.isPresent());
		final Optional<HistoryField> newEmpty = ActivityDetailsUtil.processName(OLD_VALUE, "");
		assertFalse(newEmpty.isPresent());
	}

	@Test
	void processDescription() {
		final Optional<HistoryField> historyField = ActivityDetailsUtil.processDescription(OLD_VALUE, NEW_VALUE);
		assertTrue(historyField.isPresent());
		assertThat(historyField.get()).isEqualToIgnoringGivenFields(getExpected("description"), "mapper");
	}

	@Test
	void processDescriptionReturnEmpty() {
		final Optional<HistoryField> sameValue = ActivityDetailsUtil.processDescription(OLD_VALUE, OLD_VALUE);
		assertFalse(sameValue.isPresent());
	}

	@Test
	void processShared() {
		final Optional<HistoryField> historyField = ActivityDetailsUtil.processBoolean(SHARE, false, true);
		assertTrue(historyField.isPresent());
		assertThat(historyField.get()).isEqualToIgnoringGivenFields(getExpected("share"), "mapper");
		final Optional<HistoryField> sameValue = ActivityDetailsUtil.processBoolean(SHARE, false, false);
		assertFalse(sameValue.isPresent());
	}

	private HistoryField getExpected(String field) {
		if ("share".equals(field)) {
			return HistoryField.of(field, "false", "true");
		}
		return HistoryField.of(field, OLD_VALUE, NEW_VALUE);
	}
}