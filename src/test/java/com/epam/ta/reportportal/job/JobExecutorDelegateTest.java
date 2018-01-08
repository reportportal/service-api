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

package com.epam.ta.reportportal.job;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by Andrey_Ivanov1 on 31-May-17.
 */

@RunWith(SpringJUnit4ClassRunner.class)
public class JobExecutorDelegateTest {

	@InjectMocks
	private JobExecutorDelegate jobExecutorDelegate = new JobExecutorDelegate();
	@Mock
	private SelfCancelableJob selfCancalableJob;
	@Mock
	private TaskScheduler taskScheduler;

	@Test
	public void submitJobTest() {
		jobExecutorDelegate.submitJob(selfCancalableJob);
		verify(taskScheduler, times(1)).schedule(selfCancalableJob, selfCancalableJob);
	}

}