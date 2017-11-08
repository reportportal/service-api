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

package com.epam.ta.reportportal.database.search;

import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Tests for CriteriaMap Factory
 *
 * @author Andrei Varabyeu
 */
public class CriteriaMapFactoryTest {

	private Supplier<CriteriaMapFactory> criteriaMapFactory = CriteriaMapFactory.DEFAULT_INSTANCE_SUPPLIER;

	private static final String CORRECT_CRITERIA = "statistics$executions$total";

	private static final String INCORRECT_CRITERIA = "statistics$executions$totalIncorrect";

	@Test
	public void testCriteriaMapFactory() throws IOException, ClassNotFoundException {
		CriteriaMap<TestItem> cm = criteriaMapFactory.get().getCriteriaMap(TestItem.class);
		CriteriaHolder holder = cm.getCriteriaHolder(CORRECT_CRITERIA);
		Assert.assertThat(holder, notNullValue());
	}

	@Test(expected = ReportPortalException.class)
	public void testCriteriaMapFactoryNegative() throws IOException, ClassNotFoundException {
		CriteriaMap<TestItem> cm = criteriaMapFactory.get().getCriteriaMap(TestItem.class);
		CriteriaHolder holder = cm.getCriteriaHolder(INCORRECT_CRITERIA);
		Assert.assertThat(holder, notNullValue());
	}

	@Test
	public void testCriteriaMapFactorySafe() throws IOException, ClassNotFoundException {
		CriteriaMap<TestItem> cm = criteriaMapFactory.get().getCriteriaMap(TestItem.class);
		Optional<CriteriaHolder> holderCorrect = cm.getCriteriaHolderUnchecked(CORRECT_CRITERIA);
		Assert.assertThat(holderCorrect.isPresent(), is(true));
	}

	@Test
	public void testCriteriaMapFactorySafeNegative() throws IOException, ClassNotFoundException {
		CriteriaMap<TestItem> cm = criteriaMapFactory.get().getCriteriaMap(TestItem.class);
		Optional<CriteriaHolder> holderCorrect = cm.getCriteriaHolderUnchecked(INCORRECT_CRITERIA);
		Assert.assertThat(holderCorrect.isPresent(), is(false));
	}
}
