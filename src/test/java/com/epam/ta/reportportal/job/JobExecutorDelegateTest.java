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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by Andrey_Ivanov1 on 31-May-17.
 */

@ExtendWith(MockitoExtension.class)
class JobExecutorDelegateTest {

	@InjectMocks
	private JobExecutorDelegate jobExecutorDelegate = new JobExecutorDelegate();
	@Mock
	private SelfCancelableJob selfCancalableJob;
	@Mock
	private TaskScheduler taskScheduler;

	@Test
	void submitJobTest() {
		jobExecutorDelegate.submitJob(selfCancalableJob);
		verify(taskScheduler, times(1)).schedule(selfCancalableJob, selfCancalableJob);
	}

}