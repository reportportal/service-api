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
 * Describe possible gadgets data types
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
public enum WidgetDataTypes {

	LINE_CHART("line_chart"),
	COLUMN_CHART("column_chart"),
	BAR_CHART("bar_chart"),
	PIE_CHART("combine_pie_chart"),
	TRENDS_CHART("trends_chart"),
	NOT_PASSED_TESTS("not_passed_chart"),
	CASES_TREND_CHART("cases_trend_chart"),
	TABLE("table"),
	ACTIVITY("activity_panel"),
	STATISTICS_PANEL("statistics_panel"),
	UNIQUE_BUG_TABLE("unique_bug_table"),
	BUG_TREND("bug_trend"),
	LAUNCHES_COMPARISON_CHART("launches_comparison_chart"),
	LAUNCHES_DURATION_CHART("launches_duration_chart"),
	LAUNCHES_TABLE("launches_table"),
	CLEAN_WIDGET("clean_widget");

	private String type;

	WidgetDataTypes(String type) {
		this.type = type;
	}

	public String getType() {
		return this.type;
	}

	public static WidgetDataTypes getByName(String type) {
		return WidgetDataTypes.valueOf(type);
	}

	public static Optional<WidgetDataTypes> findByName(@Nullable String input) {
		return Arrays.stream(WidgetDataTypes.values()).filter(widgetDataType -> widgetDataType.getType().equalsIgnoreCase(input)).findAny();
	}
}