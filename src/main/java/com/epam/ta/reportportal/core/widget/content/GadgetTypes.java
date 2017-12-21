/*
 * Copyright 2016 EPAM Systems
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

package com.epam.ta.reportportal.core.widget.content;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Optional;

/**
 * Types of gadgets for Report Portal
 *
 * @author Andrei_Ramanchuk
 */
public enum GadgetTypes {

	OLD_LINE_CHART("old_line_chart"),
	INVESTIGATED_TREND("investigated_trend"),
	LAUNCH_STATISTICS("launch_statistics"),
	STATISTIC_TREND("statistic_trend"),
	CASES_TREND("cases_trend"),
	NOT_PASSED("not_passed"),
	//almost the same as cases_trend, so could be removed if UI handle this
	OVERALL_STATISTICS("overall_statistics"),
	UNIQUE_BUG_TABLE("unique_bug_table"),
	BUG_TREND("bug_trend"),
	ACTIVITY("activity_stream"),
	LAUNCHES_COMPARISON_CHART("launches_comparison_chart"),
	LAUNCHES_DURATION_CHART("launches_duration_chart"),
	LAUNCHES_TABLE("launches_table"),
	MOST_FAILED_TEST_CASES("most_failed_test_cases"),
	FLAKY_TEST_CASES("flaky_test_cases"),
	PASSING_RATE_SUMMARY("passing_rate_summary"),
	PASSING_RATE_PER_LAUNCH("passing_rate_per_launch"),
	PRODUCT_STATUS("product_status"),
	CUMULATIVE("cumulative");

	private final String type;

	GadgetTypes(String type) {
		this.type = type;
	}

	public String getType() {
		return this.type;
	}

	public static GadgetTypes getByName(String type) {
		return GadgetTypes.valueOf(type);
	}

	public static Optional<GadgetTypes> findByName(@Nullable String name) {
		return Arrays.stream(GadgetTypes.values()).filter(gadgetTypes -> gadgetTypes.getType().equalsIgnoreCase(name)).findAny();
	}
}
