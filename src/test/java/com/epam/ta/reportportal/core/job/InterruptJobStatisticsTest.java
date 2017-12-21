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
import com.epam.ta.reportportal.database.entity.item.TestItem;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.quartz.JobExecutionContext;

import static org.mockito.Mockito.mock;

/**
 * Validates statistics updates after execution of interrupt launches job
 *
 * @author Andrei Varabyeu
 */
@Ignore
public class InterruptJobStatisticsTest extends BaseInterruptTest {

	/**
	 * Validates increment of total and failed statistics items after
	 * interrupting
	 *
	 * @throws InterruptedException
	 */
	@Test
	public void interruptInProgressItem() {
		Launch launch = insertLaunchInProgres();
		TestItem item = prepareTestItem(launch);
		testItemRepository.save(item);

		brokenLaunchesJob.execute(mock(JobExecutionContext.class));

		Assert.assertEquals(
				"Total count of launch is expected to be == 1", Integer.valueOf(1),
				launchRepository.findOne(launch.getId()).getStatistics().getExecutionCounter().getTotal()
		);
		Assert.assertEquals(
				"Total count of item is expected to be == 1", Integer.valueOf(1),
				testItemRepository.findOne(item.getId()).getStatistics().getExecutionCounter().getTotal()
		);

		Assert.assertEquals(
				"Failed count of launch is expected to be == 1", Integer.valueOf(1),
				launchRepository.findOne(launch.getId()).getStatistics().getExecutionCounter().getFailed()
		);
		Assert.assertEquals(
				"Failed count of item is expected to be == 1", Integer.valueOf(1),
				testItemRepository.findOne(item.getId()).getStatistics().getExecutionCounter().getFailed()
		);
	}

}
