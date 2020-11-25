/*
 * Copyright 2020 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.ws.converter.builders;

import java.util.Objects;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class TestCaseIdEntry {

	private String id;

	private int hash;

	public String getId() {
		return id;
	}

	public int getHash() {
		return hash;
	}

	TestCaseIdEntry(String id, int hash) {
		this.id = isCropNeeded(id) ? cropTestCaseId(id) : id;
		this.hash = hash;
	}

	TestCaseIdEntry(int hash) {
		this.hash = hash;
	}

	public static TestCaseIdEntry empty() {
		return new TestCaseIdEntry(0);
	}

	private static boolean isCropNeeded(String testCaseId) {
		return Objects.nonNull(testCaseId) && testCaseId.length() > 1024;
	}

	private static String cropTestCaseId(String testCaseId) {
		return testCaseId.substring(0, 1011) + "[" + testCaseId.substring(1011).hashCode() + "]";
	}

}
