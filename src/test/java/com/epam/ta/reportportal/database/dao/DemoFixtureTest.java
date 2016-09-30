/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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

package com.epam.ta.reportportal.database.dao;

import static org.hamcrest.Matchers.greaterThan;

import java.util.List;

import com.epam.ta.BaseTest;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.epam.ta.reportportal.database.entity.Log;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.fixture.SpringFixture;
import com.epam.ta.reportportal.database.fixture.SpringFixtureRule;

@SpringFixture("unitTestsDemoData")
public class DemoFixtureTest extends BaseTest {

	@Autowired
	public TestItemRepository testStepRepository;

	@Autowired
	private LogRepository logRepository;

	@Rule
	@Autowired
	public SpringFixtureRule dfRule;

	@Test
	@Ignore
	public void checkTestSteps() {
		Long countOfImportedSteps = testStepRepository.count();
		Assert.assertThat(countOfImportedSteps.intValue(), greaterThan(0));
	}

	@Ignore
	@Test
	public void checkLogs() {
		Long countOfImportedLogs = logRepository.count();
		Assert.assertThat(countOfImportedLogs.intValue(), greaterThan(0));
	}

	@Test
	@Ignore
	public void checkReference() {
		List<TestItem> steps = testStepRepository.findAll();
		List<Log> logs = logRepository.findByTestItemRef(steps.get(0).getId());
		Assert.assertThat(logs.size(), greaterThan(0));
	}
}
