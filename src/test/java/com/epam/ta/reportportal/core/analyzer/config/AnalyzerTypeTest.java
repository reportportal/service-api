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

package com.epam.ta.reportportal.core.analyzer.config;

import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
class AnalyzerTypeTest {

	@Test
	void testFromStringPositive() {
		String autoAnalyser = "autoAnalyzer";
		AnalyzerType analyzer = AnalyzerType.fromString(autoAnalyser);
		assertTrue(analyzer.getName().equalsIgnoreCase(autoAnalyser));
	}

	@Test
	void testFromStringNegative() {
		String autoAnalyser = "incorrect";
		ReportPortalException exception = assertThrows(ReportPortalException.class, () -> AnalyzerType.fromString(autoAnalyser));
		assertEquals(exception.getErrorType(), ErrorType.INCORRECT_REQUEST);
		assertEquals(exception.getMessage(), "Incorrect Request. Incorrect analyzer type. Allowed are: [autoAnalyzer, patternAnalyzer]");
	}

}