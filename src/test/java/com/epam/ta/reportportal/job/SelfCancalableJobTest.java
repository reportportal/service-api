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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;

/**
 * Created by Andrey_Ivanov1 on 01-Jun-17.
 */

@RunWith(MockitoJUnitRunner.class)
public class SelfCancalableJobTest {

	@Mock
	private Trigger triggerDelegate;
	@Mock
	private TriggerContext triggerContext;

	@Test
	public void selfCancalableJobTest() {
		SelfCancelableJob selfCancalableJob = new SelfCancelableJob(triggerDelegate) {
			@Override
			public void run() {
			}
		};
		Assert.assertEquals(true, Whitebox.getInternalState(selfCancalableJob, "oneMoreTime"));
		Assert.assertEquals(triggerDelegate, Whitebox.getInternalState(selfCancalableJob, "triggerDelegate"));
		selfCancalableJob.oneMoreTime(true);
		selfCancalableJob.nextExecutionTime(triggerContext);
		Assert.assertEquals(true, Whitebox.getInternalState(selfCancalableJob, "oneMoreTime"));
		selfCancalableJob.oneMoreTime(false);
		selfCancalableJob.nextExecutionTime(triggerContext);
		Assert.assertEquals(false, Whitebox.getInternalState(selfCancalableJob, "oneMoreTime"));
	}

}