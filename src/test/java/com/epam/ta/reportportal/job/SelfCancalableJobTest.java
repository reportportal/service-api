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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by Andrey_Ivanov1 on 01-Jun-17.
 */

@ExtendWith(MockitoExtension.class)
class SelfCancelableJobTest {

	@Mock
	private Trigger triggerDelegate;
	@Mock
	private TriggerContext triggerContext;

	@Test
	void selfCancelableJobTest() {
		SelfCancelableJob selfCancelableJob = new SelfCancelableJob(triggerDelegate) {
			@Override
			public void run() {
			}
		};

		assertEquals(true, ReflectionTestUtils.getField(selfCancelableJob, "oneMoreTime"));
		assertEquals(triggerDelegate, ReflectionTestUtils.getField(selfCancelableJob, "triggerDelegate"));
		selfCancelableJob.oneMoreTime(true);
		selfCancelableJob.nextExecutionTime(triggerContext);
		assertEquals(true, ReflectionTestUtils.getField(selfCancelableJob, "oneMoreTime"));
		selfCancelableJob.oneMoreTime(false);
		selfCancelableJob.nextExecutionTime(triggerContext);
		assertEquals(false, ReflectionTestUtils.getField(selfCancelableJob, "oneMoreTime"));
	}

}