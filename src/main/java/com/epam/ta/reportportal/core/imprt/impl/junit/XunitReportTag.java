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

	MANUAL_TEST("manual-test"),

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
