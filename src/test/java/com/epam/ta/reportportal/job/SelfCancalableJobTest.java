/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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