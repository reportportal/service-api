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

import com.epam.ta.BaseTest;
import com.epam.ta.reportportal.database.entity.Log;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.inject.Provider;

public class LogBuilderTest extends BaseTest {

	@Autowired
	private Provider<LogBuilder> logBuilderProvider;

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	public void testNull() {
		Log actualLog = logBuilderProvider.get().addBinaryContent(null).addSaveLogRQ(null).addTestItem(null).build();
		Log expectedLog = new Log();
		validateLog(expectedLog, actualLog);
	}

	@Test
	public void testBeanScope() {
		Assert.assertTrue(
				"Log builder should be prototype bean because it's not stateless",
				applicationContext.isPrototype(applicationContext.getBeanNamesForType(LogBuilder.class)[0])
		);
	}

	@Test
	public void testValues() {
		Log actualValue = logBuilderProvider.get()
				.addTestItem(new TestItem())
				.addBinaryContent(BuilderTestsConstants.BINARY_CONTENT)
				.addSaveLogRQ(getTestLogRQ())
				.build();
		validateLog(Utils.getTestLog(), actualValue);
	}

	private SaveLogRQ getTestLogRQ() {
		SaveLogRQ rq = new SaveLogRQ();
		rq.setLogTime(BuilderTestsConstants.LOG_TIME);
		rq.setMessage(BuilderTestsConstants.LOG_MESSAGE);
		rq.setTestItemId(BuilderTestsConstants.TESTSTEP_ID);
		return rq;
	}

	private void validateLog(Log expected, Log actual) {
		Assert.assertEquals(expected.getLogMsg(), actual.getLogMsg());
		Assert.assertEquals(expected.getLogTime(), actual.getLogTime());
		Assert.assertEquals(expected.getBinaryContent(), actual.getBinaryContent());
		Assert.assertEquals(expected.getTestItemRef(), actual.getTestItemRef());
		Assert.assertEquals(expected.getId(), actual.getId());
	}
}