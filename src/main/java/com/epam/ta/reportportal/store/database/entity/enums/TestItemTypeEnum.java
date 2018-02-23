/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.store.database.entity.enums;

import java.util.Arrays;
import java.util.Comparator;

public enum TestItemTypeEnum implements Comparable<TestItemTypeEnum> {

	//@formatter:off
SUITE(Constants.SUITE_LEVEL, true),
STORY(Constants.SUITE_LEVEL, true),
TEST(Constants.TEST_LEVEL, true),
SCENARIO(Constants.TEST_LEVEL, true),
STEP(Constants.STEP_LEVEL, true),
BEFORE_CLASS(Constants.STEP_LEVEL, false),
BEFORE_GROUPS(Constants.STEP_LEVEL, false),
BEFORE_METHOD(Constants.STEP_LEVEL, false),
BEFORE_SUITE(Constants.TEST_LEVEL, false),
BEFORE_TEST(Constants.STEP_LEVEL, false),
AFTER_CLASS(Constants.STEP_LEVEL, false),
AFTER_GROUPS(Constants.STEP_LEVEL, false),
AFTER_METHOD(Constants.STEP_LEVEL, false),
AFTER_SUITE(Constants.TEST_LEVEL, false),
AFTER_TEST(Constants.STEP_LEVEL, false);
//@formatter:on

	private int level;
	private boolean awareStatistics;

	TestItemTypeEnum(int level, boolean awareStatistics) {
		this.level = level;
		this.awareStatistics = awareStatistics;
	}

	public static TestItemTypeEnum fromValue(String value) {
		return Arrays.stream(TestItemTypeEnum.values()).filter(type -> type.name().equalsIgnoreCase(value)).findAny().orElse(null);
	}

	public boolean sameLevel(TestItemTypeEnum other) {
		return 0 == LEVEL_COMPARATOR.compare(this, other);
	}

	/**
	 * Is level of current item higher than level of specified
	 *
	 * @param type Item to compare
	 * @return
	 */
	public boolean higherThan(TestItemTypeEnum type) {
		return LEVEL_COMPARATOR.compare(this, type) > 0;
	}

	/**
	 * Is level of current item lower than level of specified
	 *
	 * @param type Item to compare
	 * @return
	 */
	public boolean lowerThan(TestItemTypeEnum type) {
		return LEVEL_COMPARATOR.compare(this, type) < 0;
	}

	public boolean awareStatistics() {
		return awareStatistics;
	}

	/**
	 * Level Comparator for TestItem types. Returns TRUE of level of first
	 * object is <b>lower</b> than level of second object
	 *
	 * @author Andrei Varabyeu
	 */
	private static final Comparator<TestItemTypeEnum> LEVEL_COMPARATOR = (TestItemTypeEnum o1, TestItemTypeEnum o2) -> (o1.level
			== o2.level) ? 0 : (o1.level < o2.level) ? 1 : -1;

	public static class Constants {
		public static final int SUITE_LEVEL = 0;
		public static final int TEST_LEVEL = 1;
		public static final int STEP_LEVEL = 2;
	}
}
