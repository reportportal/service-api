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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.Log;
import com.epam.ta.reportportal.database.entity.LogLevel;
import com.epam.ta.reportportal.ws.model.log.LogResource;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

/**
 * @author Pavel_Bortnik
 */
public class LogConverterTest {

	@Test(expected = NullPointerException.class)
	public void testNull() {
		LogConverter.TO_RESOURCE.apply(null);
	}

	@Test
	public void testConvert() {
		Log log = new Log();
		Date date = new Date(0);
		log.setId("id");
		log.setLastModified(date);
		log.setTestItemRef("testItemRef");
		log.setLevel(LogLevel.DEBUG);
		log.setLogMsg("message");
		log.setLogTime(date);
		log.setBinaryContent(null);
		validate(log, LogConverter.TO_RESOURCE.apply(log));
	}

	private void validate(Log log, LogResource logResource) {
		Assert.assertEquals(log.getId(), logResource.getIdLog());
		Assert.assertEquals(log.getLastModified(), log.getLastModified());
		Assert.assertEquals(log.getLevel().toString(), logResource.getLevel());
		Assert.assertEquals(log.getLogMsg(), logResource.getMessage());
		Assert.assertEquals(log.getLogTime(), logResource.getLogTime());
		Assert.assertEquals(log.getTestItemRef(), log.getTestItemRef());
		Assert.assertNull(logResource.getBinaryContent());
	}

}