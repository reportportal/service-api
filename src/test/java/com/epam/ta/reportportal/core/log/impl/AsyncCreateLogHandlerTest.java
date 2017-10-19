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

package com.epam.ta.reportportal.core.log.impl;

import com.epam.ta.BaseTest;
import com.epam.ta.reportportal.core.log.ICreateLogHandler;
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;

@SpringFixture("itemsUnitTestsSorting")
public class AsyncCreateLogHandlerTest extends BaseTest {

	public static final String ITEM_ID = "44524cc1524de753b3e5aa2f";

	@Rule
	@Autowired
	public SpringFixtureRule dfRule;

	@Autowired
	private ICreateLogHandler logHandler;

	/**
	 * Added for for covering EPMCDP-700 bug fixes.
	 * Start time of log shouldn't be earlier than
	 * start time of testItem
	 */
	@Test(expected = ReportPortalException.class)
	public void testCreateLogIncorrectTime() {
		SaveLogRQ saveLogRQ = new SaveLogRQ();
		Calendar calendar = Calendar.getInstance();
		calendar.set(1980, 2, 3);
		saveLogRQ.setLogTime(calendar.getTime());
		saveLogRQ.setMessage("Log message");
		saveLogRQ.setTestItemId(ITEM_ID);
		logHandler.createLog(saveLogRQ, null, null);
	}
}
