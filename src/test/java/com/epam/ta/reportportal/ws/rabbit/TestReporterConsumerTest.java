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
import com.epam.ta.reportportal.core.item.FinishTestItemHandler;
import com.epam.ta.reportportal.core.item.StartTestItemHandler;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import org.assertj.core.util.Maps;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.MessagePostProcessor;

import java.util.Collections;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.core.configs.rabbit.ReportingConfiguration.DEAD_LETTER_MAX_RETRY;
import static com.epam.ta.reportportal.core.configs.rabbit.ReportingConfiguration.QUEUE_ITEM_FINISH_DLQ_DROPPED;
import static com.epam.ta.reportportal.core.configs.rabbit.ReportingConfiguration.QUEUE_ITEM_START_DLQ_DROPPED;
import static com.epam.ta.reportportal.util.ProjectExtractor.extractProjectDetails;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class TestReporterConsumerTest {

	@Mock
	private DatabaseUserDetailsService userDetailsService;

	@Mock
	private StartTestItemHandler startTestItemHandler;

	@Mock
	private FinishTestItemHandler finishTestItemHandler;

	@Mock
	AmqpTemplate amqpTemplate;

	@InjectMocks
	private TestReporterConsumer testReporterConsumer;

	@Test
	void onStartStepItem() {
		StartTestItemRQ rq = new StartTestItemRQ();
		rq.setLaunchId("1");
		rq.setType("STEP");
		rq.setName("name");
		rq.setDescription("description");
		ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.PROJECT_MANAGER, 1L);
		String username = "user";
		String parentId = "2";

		when(userDetailsService.loadUserByUsername(username)).thenReturn(user);

		testReporterConsumer.onStartItem(rq, username, "test_project", parentId,null);

		verify(startTestItemHandler, times(1)).startChildItem(user, extractProjectDetails(user, "test_project"), rq, parentId);
	}

	@Test
	void onStartParentItem() {
		StartTestItemRQ rq = new StartTestItemRQ();
		rq.setLaunchId("1");
		rq.setType("TEST");
		rq.setName("name");
		rq.setDescription("description");
		ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.PROJECT_MANAGER, 1L);
		String username = "user";

		when(userDetailsService.loadUserByUsername(username)).thenReturn(user);

		testReporterConsumer.onStartItem(rq, username, "test_project", null, null);

		verify(startTestItemHandler, times(1)).startRootItem(user, extractProjectDetails(user, "test_project"), rq);
	}

	@Test
	void onItemStartSpooledToDroppedDLQ() {
		StartTestItemRQ rq = new StartTestItemRQ();
		rq.setLaunchId("1");
		rq.setType("STEP");
		rq.setName("name");
		rq.setDescription("description");
		String username = "user";
		String parentId = "2";

		testReporterConsumer.onStartItem(rq, username, "test_project", parentId, Collections.singletonList(Maps.newHashMap("count", new Long(DEAD_LETTER_MAX_RETRY + 1))));

		verify(amqpTemplate).convertAndSend(eq(QUEUE_ITEM_START_DLQ_DROPPED), any(Object.class), any(MessagePostProcessor.class));
	}

	@Test
	void onFinishItem() {
		FinishTestItemRQ rq = new FinishTestItemRQ();
		rq.setStatus("PASSED");
		String username = "user";
		String itemId = "1";
		ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.PROJECT_MANAGER, 1L);

		when(userDetailsService.loadUserByUsername(username)).thenReturn(user);

		testReporterConsumer.onFinishItem(rq, username, "test_project", itemId, null);

		verify(finishTestItemHandler, times(1)).finishTestItem(user, extractProjectDetails(user, "test_project"), itemId, rq);
	}

	@Test
	void onItemStopSpooledToDroppedDLQ() {
		FinishTestItemRQ rg = new FinishTestItemRQ();
		rg.setStatus("PASSED");
		String username = "user";
		String itemId = "1";

		testReporterConsumer.onFinishItem(rg, username, "test_project", itemId, Collections.singletonList(Maps.newHashMap("count", new Long(DEAD_LETTER_MAX_RETRY + 1))));

		verify(amqpTemplate).convertAndSend(eq(QUEUE_ITEM_FINISH_DLQ_DROPPED), any(Object.class), any(MessagePostProcessor.class));
	}
}