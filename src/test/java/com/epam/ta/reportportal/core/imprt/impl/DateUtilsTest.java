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

package com.epam.ta.reportportal.core.imprt.impl;

import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author Pavel Bortnik
 */
public class DateUtilsTest {

	@Test
	public void testNullValue() {
		Assert.assertNull(DateUtils.toDate(null));
		Assert.assertEquals(0, DateUtils.toMillis(null));
	}

	@Test
	public void toDate() {
		LocalDateTime t = LocalDateTime.ofInstant(Instant.ofEpochMilli(1512118850), ZoneId.systemDefault());
		Date date = DateUtils.toDate(t);
		Assert.assertEquals(1512118850L, date.getTime());
	}

	@Test
	public void toMillis() {
		long l = DateUtils.toMillis("1");
		Assert.assertEquals(1000L, l);
	}

}