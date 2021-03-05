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

package com.epam.ta.reportportal.core.jasper.constants;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public final class LaunchReportConstants {

	/* Defined fields in JRXML template */
	public final static String LAUNCH_NAME = "LAUNCH_NAME";
	public final static String LAUNCH_DESC = "LAUNCH_DESCRIPTION";
	public final static String LAUNCH_TAGS = "LAUNCH_TAGS";
	public final static String DURATION = "LAUNCH_DURATION";
	public final static String OWNER = "LAUNCH_OWNER";

	/* Launch statistics fields */
	// TODO could be inject in report as DataSource
	public final static String TOTAL = "TOTAL";
	public final static String PASSED = "PASSED";
	public final static String FAILED = "FAILED";
	public final static String SKIPPED = "SKIPPED";
	public final static String UNTESTED = "UNTESTED";
	public final static String AB = "AB";
	public final static String PB = "PB";
	public final static String SI = "SI";
	public final static String ND = "ND";
	public final static String TI = "TI";

	/* Data sets */
	public final static String TEST_ITEMS = "TEST_ITEMS";

	public LaunchReportConstants() {

		//static only
	}
}
