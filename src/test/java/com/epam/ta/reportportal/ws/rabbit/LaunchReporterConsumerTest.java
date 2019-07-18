/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.ws.rabbit;

import com.epam.ta.reportportal.auth.basic.DatabaseUserDetailsService;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.launch.FinishLaunchHandler;
import com.epam.ta.reportportal.core.launch.StartLaunchHandler;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import org.assertj.core.util.Maps;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.MessagePostProcessor;

import java.util.Collections;
import java.util.Date;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.core.configs.rabbit.ReportingConfiguration.DEAD_LETTER_MAX_RETRY;
import static com.epam.ta.reportportal.core.configs.rabbit.ReportingConfiguration.QUEUE_LAUNCH_FINISH_DLQ_DROPPED;
import static com.epam.ta.reportportal.core.configs.rabbit.ReportingConfiguration.QUEUE_LAUNCH_START_DLQ_DROPPED;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class LaunchReporterConsumerTest {

	@Mock
	private DatabaseUserDetailsService userDetailsService;

	@Mock
	private StartLaunchHandler startLaunchHandler;

	@Mock
	private FinishLaunchHandler finishLaunchHandler;

	@Mock
	AmqpTemplate amqpTemplate;

	@InjectMocks
	private LaunchReporterConsumer launchReporterConsumer;

	@Test
	void onStartLaunch() {
		StartLaunchRQ startLaunchRQ = new StartLaunchRQ();
		startLaunchRQ.setName("name");
		startLaunchRQ.setStartTime(new Date());
		startLaunchRQ.setDescription("description");
		startLaunchRQ.setUuid("uuid");
		String username = "user";
		ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.PROJECT_MANAGER, 1L);

		when(userDetailsService.loadUserByUsername(username)).thenReturn(user);

		launchReporterConsumer.onStartLaunch(startLaunchRQ, username, "test_project", null);

		verify(startLaunchHandler, times(1)).startLaunch(eq(user), any(), eq(startLaunchRQ));
	}

	@Test
	void onStartLaunchSpooledToDroppedDLQ() {
		StartLaunchRQ startLaunchRQ = new StartLaunchRQ();
		startLaunchRQ.setName("name");
		startLaunchRQ.setStartTime(new Date());
		startLaunchRQ.setDescription("description");
		startLaunchRQ.setUuid("uuid");
		String username = "user";

		launchReporterConsumer.onStartLaunch(startLaunchRQ, username, "test_project", Collections.singletonList(Maps.newHashMap("count", new Long(DEAD_LETTER_MAX_RETRY + 1))));

		verify(amqpTemplate).convertAndSend(eq(QUEUE_LAUNCH_START_DLQ_DROPPED), any(Object.class), any(MessagePostProcessor.class));
	}


	@Test
	void onFinishLaunch() {
		FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();
		finishExecutionRQ.setEndTime(new Date());
		finishExecutionRQ.setDescription("description");
		String username = "user";
		ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.PROJECT_MANAGER, 1L);

		when(userDetailsService.loadUserByUsername(username)).thenReturn(user);

		String launchId = "1";
		launchReporterConsumer.onFinishLaunch(finishExecutionRQ, username, "test_project", launchId, null, "http://example.com");

		verify(finishLaunchHandler, times(1)).finishLaunch(eq(launchId), eq(finishExecutionRQ), any(), eq(user), eq("http://example.com"));
	}

	@Test
	void onFinishLaunchSpooledToDroppedDLQ() {
		FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();
		finishExecutionRQ.setEndTime(new Date());
		finishExecutionRQ.setDescription("description");
		String username = "user";

		String launchId = "1";
		launchReporterConsumer.onFinishLaunch(finishExecutionRQ, username, "test_project", launchId, Collections.singletonList(Maps.newHashMap("count", new Long(DEAD_LETTER_MAX_RETRY + 1))));

		verify(amqpTemplate).convertAndSend(eq(QUEUE_LAUNCH_FINISH_DLQ_DROPPED), any(Object.class), any(MessagePostProcessor.class));
	}
}