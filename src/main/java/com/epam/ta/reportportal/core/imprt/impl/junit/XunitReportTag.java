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
package com.epam.ta.reportportal.core.imprt.impl.junit;

import java.util.Arrays;

public enum XunitReportTag {
	// the testsuites element for the aggregate document
	TESTSUITES("testsuites"),

	// the testsuite element
	TESTSUITE("testsuite"),

	// the testcase element
	TESTCASE("testcase"),

	// the error element
	ERROR("error"),

	// the warning element,
	WARNING("warning"),

	// the failure element
	FAILURE("failure"),

	// the system-err element
	SYSTEM_ERR("system-err"),

	// the system-out element
	SYSTEM_OUT("system-out"),

	// name attribute for property, testcase and testsuite elements
	ATTR_NAME("name"),

	// time attribute for testcase and testsuite elements
	ATTR_TIME("time"),

	SKIPPED("skipped"),

	// type attribute for failure and error elements
	ATTR_TYPE("type"),

	// message attribute for failure elements
	ATTR_MESSAGE("message"),

	// the properties element
	PROPERTIES("properties"),

	// the property element
	PROPERTY("property"),

	// value attribute for property elements
	ATTR_VALUE("value"),

	// timestamp of test cases
	TIMESTAMP("timestamp"),

	//unknown tag
	UNKNOWN("unknown");

	private String value;

	XunitReportTag(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	static XunitReportTag fromString(String type) {
		return Arrays.stream(values()).filter(it -> it.getValue().equalsIgnoreCase(type)).findAny().orElse(UNKNOWN);
	}
}
