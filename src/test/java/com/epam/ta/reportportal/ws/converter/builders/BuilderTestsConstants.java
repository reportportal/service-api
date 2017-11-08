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

package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.database.entity.BinaryContent;

import java.util.Date;

public final class BuilderTestsConstants {

	public static final String EMAIL = "test@test.com";
	public static final String DESCRIPTION = "Description";
	public static final Date DATE_START = new Date(3333);
	public static final Date DATE_END = new Date(4444);
	public static final String TAG = "TagValue";
	public static final String ID = "33333";

	public static final String LOG_MESSAGE = "test message";
	public static final Date LOG_TIME = new Date(8888);
	public static final String BINARY_DATA_ID = "123456";
	public static final BinaryContent BINARY_CONTENT = new BinaryContent("123456", null, null);
	public static final String TESTSTEP_ID = "123456";
	public static final String TESTSUITE_ID = "123456566";

	public static final String TEST_TYPE = "test";
	public static final String TEST_ROLE = "Administrator";
	public static final String ENTRY_TYPE = "INTERNAL";

	public static final String NAME = "Name";
	public static final String PASSWORD = "testvalue";

	public static final String NAME_CRITERIA = "name";
	public static final String LAUNCH = "Launch";

	public static final String PROJECT = "default_project";
	public static final String USER = "user2";

	private BuilderTestsConstants() {
		// do not create instance. Just holder for constants
	}
}