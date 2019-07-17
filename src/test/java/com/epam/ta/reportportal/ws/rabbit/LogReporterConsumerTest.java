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
import com.epam.ta.reportportal.core.configs.rabbit.DeserializablePair;
import com.epam.ta.reportportal.core.log.impl.CreateLogHandlerImpl;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import org.assertj.core.util.Maps;
import org.junit.jupiter.api.Disabled;
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
import static com.epam.ta.reportportal.core.configs.rabbit.ReportingConfiguration.QUEUE_LOG_DLQ_DROPPED;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class LogReporterConsumerTest {

	@Mock
	private DatabaseUserDetailsService userDetailsService;

	@Mock
	private CreateLogHandlerImpl createLogHandlerImpl;

	@Mock
	AmqpTemplate amqpTemplate;

	@InjectMocks
	private LogReporterConsumer logReporterConsumer;

	@Test
	@Disabled
	void onLogCreate() {
		SaveLogRQ saveLogRQ = new SaveLogRQ();
		saveLogRQ.setItemId("1");
		saveLogRQ.setLogTime(new Date());
		saveLogRQ.setLevel("ERROR");
		saveLogRQ.setMessage("message");
		String username = "user";
		ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.PROJECT_MANAGER, 1L);

		when(userDetailsService.loadUserByUsername(username)).thenReturn(user);

		logReporterConsumer.onLogCreate(DeserializablePair.of(saveLogRQ, null), 1L, "test_project", Collections.emptyList());

		verify(createLogHandlerImpl, times(1)).createLog(eq(saveLogRQ), eq(null), any());
	}

	@Test
	void onLogCreateSpooledToDroppedDLQ() {
		SaveLogRQ saveLogRQ = new SaveLogRQ();
		saveLogRQ.setItemId("1");
		saveLogRQ.setLogTime(new Date());
		saveLogRQ.setLevel("ERROR");
		saveLogRQ.setMessage("message");

		logReporterConsumer.onLogCreate(DeserializablePair.of(saveLogRQ, null), 1L, "test_project", Collections.singletonList(Maps.newHashMap("count", new Long(DEAD_LETTER_MAX_RETRY + 1))));

		verify(amqpTemplate).convertAndSend(eq(QUEUE_LOG_DLQ_DROPPED), any(Object.class), any(MessagePostProcessor.class));
	}
}