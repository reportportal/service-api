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
import java.util.List;
import java.util.stream.Collectors;

/**
 * Vocabulary for supported test item issues types. They are applied as markers
 * for test steps. User marks test step according to the type of the issue,
 * caused it's failure.<br>
 * <p>
 * Enum contains following fields:<br>
 * value - value of specified enum field;<br>
 * issueCounterField - field name in IssueCounter object for MongoDB<br>
 * locator - predefined ID for immutable issue type<br>
 *
 * @author Dzianis Shlychkou
 * @author Andrei_Ramanchuk
 */
public enum TestItemIssueGroup /*implements StatisticsAwareness*/ {

	NOT_ISSUE_FLAG("NOT_ISSUE", "notIssue", ""),

	PRODUCT_BUG("PRODUCT_BUG", "productBug", "PB001"),
	AUTOMATION_BUG("AUTOMATION_BUG", "automationBug", "AB001"),
	SYSTEM_ISSUE("SYSTEM_ISSUE", "systemIssue", "SI001"),
	TO_INVESTIGATE("TO_INVESTIGATE", "toInvestigate", "TI001"),
	NO_DEFECT("NO_DEFECT", "noDefect", "ND001");

	private final String value;

	private final String issueCounterField;

	private final String locator;

	TestItemIssueGroup(String value, String executionCounterField, String locator) {
		this.value = value;
		this.issueCounterField = executionCounterField;
		this.locator = locator;
	}

	public String getValue() {
		return value;
	}

	public String getLocator() {
		return locator;
	}

	/**
	 * Retrieves TestItemIssueType value by it's string value
	 *
	 * @param value - string representation of desired TestItemIssueType value
	 * @return TestItemIssueType value
	 */
	public static TestItemIssueGroup fromValue(String value) {
		return Arrays.stream(TestItemIssueGroup.values()).filter(type -> type.getValue().equalsIgnoreCase(value)).findAny().orElse(null);
	}

	public static TestItemIssueGroup validate(String value) {
		return Arrays.stream(TestItemIssueGroup.values())
				.filter(type -> type.getValue().replace(" ", "_").equalsIgnoreCase(value))
				.findAny()
				.orElse(null);
	}

	//	public static TestItemIssueType fromCounterField(String value) {
	//		return Arrays.stream(TestItemIssueType.values())
	//				.filter(type -> type.awareStatisticsField().equalsIgnoreCase(value))
	//				.findAny()
	//				.orElse(null);
	//	}
	//
	//	@Override
	//	public String awareStatisticsField() {
	//		return issueCounterField;
	//	}

	public static List<String> validValues() {
		return Arrays.stream(TestItemIssueGroup.values()).map(TestItemIssueGroup::getValue).collect(Collectors.toList());
	}
}