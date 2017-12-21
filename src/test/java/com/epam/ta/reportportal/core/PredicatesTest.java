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
package com.epam.ta.reportportal.core;

import com.epam.ta.reportportal.commons.Predicates;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Dzmitry_Kavalets
 */
public class PredicatesTest {

	@Test
	public void notNullPredicate() {
		Assert.assertFalse(Predicates.notNull().test(null));
		Assert.assertTrue(Predicates.notNull().test(5));
	}

	@Test
	public void isNullPredicate() {
		Assert.assertTrue(Predicates.isNull().test(null));
		Assert.assertFalse(Predicates.isNull().test(5));
	}

	@Test
	public void equalsToPredicate() {
		Assert.assertTrue(Predicates.equalTo(0).test(0));
		Assert.assertFalse(Predicates.equalTo(0).test(1));
	}
}