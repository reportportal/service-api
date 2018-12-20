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

import com.epam.ta.reportportal.entity.HistoryField;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class ActivityDetailsUtilTest {

	private static final String OLD_VALUE = "oldValue";
	private static final String NEW_VALUE = "newValue";

	@Test
	public void processName() {
		final Optional<HistoryField> historyField = ActivityDetailsUtil.processName(OLD_VALUE, NEW_VALUE);
		assertTrue(historyField.isPresent());
		assertThat(historyField.get()).isEqualToIgnoringGivenFields(getExpected("name"), "mapper");
	}

	@Test
	public void processNameReturnEmpty() {
		final Optional<HistoryField> sameValue = ActivityDetailsUtil.processName(OLD_VALUE, OLD_VALUE);
		assertFalse(sameValue.isPresent());
		final Optional<HistoryField> newNull = ActivityDetailsUtil.processName(OLD_VALUE, null);
		assertFalse(newNull.isPresent());
		final Optional<HistoryField> newEmpty = ActivityDetailsUtil.processName(OLD_VALUE, "");
		assertFalse(newEmpty.isPresent());
	}

	@Test
	public void processDescription() {
		final Optional<HistoryField> historyField = ActivityDetailsUtil.processDescription(OLD_VALUE, NEW_VALUE);
		assertTrue(historyField.isPresent());
		assertThat(historyField.get()).isEqualToIgnoringGivenFields(getExpected("description"), "mapper");
	}

	@Test
	public void processDescriptionReturnEmpty() {
		final Optional<HistoryField> sameValue = ActivityDetailsUtil.processDescription(OLD_VALUE, OLD_VALUE);
		assertFalse(sameValue.isPresent());
	}

	@Test
	public void processShared() {
		final Optional<HistoryField> historyField = ActivityDetailsUtil.processShared(false, true);
		assertTrue(historyField.isPresent());
		assertThat(historyField.get()).isEqualToIgnoringGivenFields(getExpected("share"), "mapper");
		final Optional<HistoryField> sameValue = ActivityDetailsUtil.processShared(false, false);
		assertFalse(sameValue.isPresent());
	}

	private HistoryField getExpected(String field) {
		if ("share".equals(field)) {
			return HistoryField.of(field, OLD_VALUE, NEW_VALUE);
		}
		return HistoryField.of(field, "false", "true");
	}
}