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

import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Status;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.TestItemType;
import com.epam.ta.reportportal.database.search.Condition;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.database.search.FilterCondition;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.function.Supplier;

/**
 * Bunch of unit tests for internal preconditions and predicates
 *
 * @author Andrei Varabyeu
 */
public class PreconditionsTest {

	private static final Supplier<String> SOME_ERROR_TEXT = Suppliers.stringSupplier("some error");

	/**
	 * Check status In predicate
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testStatusIsOrNull() {
		Launch launch = new Launch();
		BusinessRule.expect(launch.getStatus(), Predicates.or(Preconditions.statusIn(Status.PASSED), Predicates.isNull()), SOME_ERROR_TEXT)
				.verify(ReportPortalException.class);
	}

	@Test
	public void testStatusIn() {
		Launch launch = new Launch();
		launch.setStatus(Status.PASSED);
		BusinessRule.expect(launch.getStatus(), Preconditions.statusIn(Status.PASSED), SOME_ERROR_TEXT).verify(ReportPortalException.class);
	}

	@Test(expected = ReportPortalException.class)
	public void testStatusIsNull() {
		Launch launch = new Launch();
		BusinessRule.expect(launch.getStatus(), Preconditions.statusIn(Status.PASSED), SOME_ERROR_TEXT).verify(ReportPortalException.class);
	}

	@Test(expected = ReportPortalException.class)
	public void testHasAnyModePrecondition() {
		Filter filter = new Filter(Launch.class, Sets.newHashSet(new FilterCondition(Condition.EQUALS, false, "test", "name"),
				new FilterCondition(Condition.EQUALS, false, "mode", Launch.MODE_CRITERIA)
		));
		BusinessRule.expect(filter.getFilterConditions().stream().filter(Preconditions.HAS_ANY_MODE).findFirst().isPresent(),
				Predicates.equalTo(false)
		)
				.verify(ErrorType.INCORRECT_FILTER_PARAMETERS, "Filters for 'mode' aren't applicable for project's launches.");
	}

	@Test
	public void testContains() {
		List<String> elements = Lists.newArrayList("first", "seconds");
		boolean present = Preconditions.contains(Predicates.equalTo("first")).test(elements);
		boolean doNotPresent = Preconditions.contains(Predicates.equalTo("wrong")).test(elements);
		Assert.assertTrue("Incorrent behavior in 'Preconditions.contains' predicate", present);
		Assert.assertFalse("Incorrent behavior in 'Preconditions.contains' predicate", doNotPresent);
	}

	@Test(expected = ReportPortalException.class)
	public void testIsSiblings() {
		TestItem item = new TestItem();
		item.setParent("12345");
		TestItem secondItem = new TestItem();
		secondItem.setParent("1234");

		BusinessRule.expect(
				Preconditions.contains(Predicates.not(Preconditions.hasSameParent("12345"))).test(Lists.newArrayList(item, secondItem)),
				Predicates.equalTo(false)
		).verify(ErrorType.UNABLE_LOAD_TEST_ITEM_HISTORY, "All test items should be siblings.");
	}

	@Test(expected = ReportPortalException.class)
	public void testIsInTheSameLaunch() {
		TestItem item = new TestItem();
		item.setLaunchRef("1234");
		item.setType(TestItemType.SUITE);
		TestItem secondItem = new TestItem();
		secondItem.setLaunchRef("12345");
		secondItem.setType(TestItemType.SUITE);
		BusinessRule.expect(Preconditions.contains(Predicates.not(Preconditions.hasSameLaunch("1234").and(Preconditions.IS_SUITE)))
				.test(Lists.newArrayList(item, secondItem)), Predicates.equalTo(false))
				.verify(ErrorType.UNABLE_LOAD_TEST_ITEM_HISTORY, "All test items should be siblings.");
	}

	@Test
	public void checkNotEmptyCollection() {
		List<String> fullList = ImmutableList.<String>builder().add("one").add("two").build();
		List<String> emptyList = ImmutableList.<String>builder().build();

		Assert.assertTrue("'Not empty collection' predicate doesn't work on full list", Preconditions.NOT_EMPTY_COLLECTION.test(fullList));
		Assert.assertFalse("'Not empty collection' predicate doesn't work on empty list",
				Preconditions.NOT_EMPTY_COLLECTION.test(emptyList)
		);
		Assert.assertFalse("'Not empty collection' predicate doesn't work on null", Preconditions.NOT_EMPTY_COLLECTION.test(null));
	}
}