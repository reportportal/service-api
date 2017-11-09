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
package com.epam.ta.reportportal.core.item;

import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.core.Is.is;

/**
 * @author Andrei Varabyeu
 */
public class StartTestItemHandlerImplTest {

	@Test
	public void getRootTestSuccess() {
		TestItem testItem = new TestItem();
		TestItem retryRoot = new StartTestItemHandlerImpl().getRetryRoot(singletonList(testItem));
		Assert.assertThat(retryRoot, is(testItem));
	}

	@Test(expected = ReportPortalException.class)
	public void getRootTestSeveralNoRetries() {
		TestItem testItem1 = new TestItem();
		TestItem testItem2 = new TestItem();
		new StartTestItemHandlerImpl().getRetryRoot(asList(testItem1, testItem2));
	}

	@Test
	public void getRootTestSeveralRetries() {
		TestItem testItem1 = new TestItem();
		testItem1.setRetries(Collections.singletonList(new TestItem()));
		TestItem testItem2 = new TestItem();
		TestItem retryRoot = new StartTestItemHandlerImpl().getRetryRoot(asList(testItem1, testItem2));
		Assert.assertThat(retryRoot, is(testItem1));

	}

	@Test(expected = ReportPortalException.class)
	public void getRootTestEmpty() {
		new StartTestItemHandlerImpl().getRetryRoot(Collections.emptyList());
	}

}