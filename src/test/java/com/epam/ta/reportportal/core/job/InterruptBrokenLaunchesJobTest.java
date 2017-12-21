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

package com.epam.ta.reportportal.core.job;

import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Status;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.quartz.JobExecutionContext;

import java.util.Calendar;

import static org.mockito.Mockito.mock;

/**
 * Validates status of launch and test item after interrupt job execution
 *
 * @author Andrei Varabyeu
 * @see com.epam.ta.reportportal.database.entity.Status
 */
@Ignore
public class InterruptBrokenLaunchesJobTest extends BaseInterruptTest {

	/**
	 * Validates status and launch in test item when test item is in progress
	 * before job execution start
	 *
	 * @throws InterruptedException
	 */
	@Test
	public void interruptInProgressItem() {
		Launch launch = insertLaunchInProgres();
		TestItem item = prepareTestItem(launch);
		testItemRepository.save(item);

		brokenLaunchesJob.execute(mock(JobExecutionContext.class));

		Assert.assertEquals("INTERRUPTED Status is expected", Status.INTERRUPTED, launchRepository.findOne(launch.getId()).getStatus());
		Assert.assertEquals("INTERRUPTED Status is expected", Status.INTERRUPTED, testItemRepository.findOne(item.getId()).getStatus());
	}

	/**
	 * Validates launch and test item status for case where there is launch in
	 * progress and two test items: first one in progress and second one is
	 * passed
	 *
	 * @throws InterruptedException
	 */
	@Test
	public void interruptPassedItem() {
		Launch launch = insertLaunchInProgres();
		TestItem itemToBeInterrupted = prepareTestItem(launch);
		testItemRepository.save(itemToBeInterrupted);

		TestItem itemNotInProgress = prepareTestItem(launch);
		itemNotInProgress.setStatus(Status.PASSED);
		itemNotInProgress.setEndTime(Calendar.getInstance().getTime());
		itemNotInProgress.setLastModified(Calendar.getInstance().getTime());
		testItemRepository.save(itemNotInProgress);

		brokenLaunchesJob.execute(mock(JobExecutionContext.class));

		Assert.assertEquals("INTERRUPTED Status is expected", Status.INTERRUPTED, launchRepository.findOne(launch.getId()).getStatus());
		Assert.assertEquals("INTERRUPTED Status is expected", Status.INTERRUPTED,
				testItemRepository.findOne(itemToBeInterrupted.getId()).getStatus()
		);
		Assert.assertEquals("PASSED Status is expected", Status.PASSED, testItemRepository.findOne(itemNotInProgress.getId()).getStatus());

	}

	/**
	 * Validates launch and test item status for case where there is launch in
	 * progress and two test items: first one in progress and second one is
	 * passed
	 *
	 * @throws InterruptedException
	 */
	@Test
	public void interruptItemWithOnlyPassedChilds() {
		Launch launch = insertLaunchInProgres();
		TestItem itemToBeInterrupted = prepareTestItem(launch);
		itemToBeInterrupted.setHasChilds(true);
		testItemRepository.save(itemToBeInterrupted);

		TestItem itemNotInProgress = prepareTestItem(launch);
		itemNotInProgress.setParent(itemToBeInterrupted.getId());
		itemNotInProgress.setStatus(Status.PASSED);
		itemNotInProgress.setEndTime(Calendar.getInstance().getTime());
		itemNotInProgress.setLastModified(Calendar.getInstance().getTime());
		testItemRepository.save(itemNotInProgress);

		brokenLaunchesJob.execute(mock(JobExecutionContext.class));

		Assert.assertEquals("INTERRUPTED Status is expected", Status.INTERRUPTED, launchRepository.findOne(launch.getId()).getStatus());
		Assert.assertEquals("INTERRUPTED Status is expected", Status.INTERRUPTED,
				testItemRepository.findOne(itemToBeInterrupted.getId()).getStatus()
		);

		Assert.assertEquals("PASSED Status is expected", Status.PASSED, testItemRepository.findOne(itemNotInProgress.getId()).getStatus());

	}

	/**
	 * Validates status update for launch with no test items
	 *
	 * @throws InterruptedException
	 */
	@Test
	public void interruptLaunchWithNoItems() {
		Launch launch = insertLaunchInProgres();
		brokenLaunchesJob.execute(null);
		Assert.assertEquals("INTERRUPTED Status is expected", Status.INTERRUPTED, launchRepository.findOne(launch.getId()).getStatus());
	}

	/**
	 * Validates status update for launch with only passed items
	 *
	 * @throws InterruptedException
	 */
	@Test
	public void interruptLaunchWithPassedItems() {
		Launch launch = insertLaunchInProgres();

		TestItem passedTestItem = prepareTestItem(launch);
		passedTestItem.setStatus(Status.PASSED);
		passedTestItem.setEndTime(Calendar.getInstance().getTime());
		passedTestItem.setLastModified(Calendar.getInstance().getTime());
		testItemRepository.save(passedTestItem);

		brokenLaunchesJob.execute(mock(JobExecutionContext.class));
		Assert.assertEquals("INTERRUPTED Status is expected", Status.INTERRUPTED, launchRepository.findOne(launch.getId()).getStatus());
	}
}
