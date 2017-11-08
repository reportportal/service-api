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

package com.epam.ta.reportportal.database.entity.filter;

import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link UserFilter}'s {@link ObjectType}
 */
public class ObjectTypeTest {

	@Test
	public void testGetType() {
		Class<?> classObject = ObjectType.getTypeByName(ObjectType.Launch.toString());
		Assert.assertEquals(Launch.class, classObject);
	}

	@Test(expected = ReportPortalException.class)
	public void testNull() {
		ObjectType.getTypeByName(null);
	}

	@Test(expected = ReportPortalException.class)
	public void testIncorrectObjectType() {
		ObjectType.getTypeByName(String.class.toString());
	}
}