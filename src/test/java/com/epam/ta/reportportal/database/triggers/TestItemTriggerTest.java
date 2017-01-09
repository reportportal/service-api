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

package com.epam.ta.reportportal.database.triggers;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.epam.ta.BaseTest;
import com.epam.ta.reportportal.database.dao.LogRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;

/**
 * Unit tests for TestItem triggers
 * 
 * @author Andrei Varabyeu
 * 
 */
@SpringFixture("triggerTests")
public class TestItemTriggerTest extends BaseTest {

	private static final String CHILD_ID = "44524cc1553de753b3e5ab2f";

	@Autowired
	private TestItemRepository testItemRepository;
	@Autowired
	private LogRepository logRepository;

	@Rule
	@Autowired
	public SpringFixtureRule dfRule;

	@Test
	public void testDeleteByAsList() {
		testItemRepository.delete(singletonList(CHILD_ID));

		assertNull(testItemRepository.findOne(CHILD_ID));
		assertTrue(logRepository.findLogIdsByTestItemId(CHILD_ID).isEmpty());
	}
}