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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
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

	@InjectMocks
	private TestReporterConsumer testReporterConsumer;

	@Test
	void onStartStepItem() {
		StartTestItemRQ rq = new StartTestItemRQ();
		rq.setLaunchId(1L);
		rq.setType("STEP");
		rq.setName("name");
		rq.setDescription("description");
		ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.PROJECT_MANAGER, 1L);
		String username = "user";
		long parentId = 2L;

		when(userDetailsService.loadUserByUsername(username)).thenReturn(user);

		testReporterConsumer.onStartItem(username, "test_project", parentId, rq);

		verify(startTestItemHandler, times(1)).startChildItem(user, extractProjectDetails(user, "test_project"), rq, parentId);
	}

	@Test
	void onStartParentItem() {
		StartTestItemRQ rq = new StartTestItemRQ();
		rq.setLaunchId(1L);
		rq.setType("TEST");
		rq.setName("name");
		rq.setDescription("description");
		ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.PROJECT_MANAGER, 1L);
		String username = "user";

		when(userDetailsService.loadUserByUsername(username)).thenReturn(user);

		testReporterConsumer.onStartItem(username, "test_project", null, rq);

		verify(startTestItemHandler, times(1)).startRootItem(user, extractProjectDetails(user, "test_project"), rq);
	}

	@Test
	void onStartParentItemWithNegativeParentId() {
		StartTestItemRQ rq = new StartTestItemRQ();
		rq.setLaunchId(1L);
		rq.setType("TEST");
		rq.setName("name");
		rq.setDescription("description");
		ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.PROJECT_MANAGER, 1L);
		String username = "user";

		when(userDetailsService.loadUserByUsername(username)).thenReturn(user);

		testReporterConsumer.onStartItem(username, "test_project", -2L, rq);

		verify(startTestItemHandler, times(1)).startRootItem(user, extractProjectDetails(user, "test_project"), rq);
	}

	@Test
	void onFinishItem() {
		FinishTestItemRQ finishTestItemRQ = new FinishTestItemRQ();
		finishTestItemRQ.setStatus("PASSED");
		String username = "user";
		long itemId = 1L;
		ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.PROJECT_MANAGER, 1L);

		when(userDetailsService.loadUserByUsername(username)).thenReturn(user);

		testReporterConsumer.onFinishItem(username, "test_project", itemId, finishTestItemRQ);

		verify(finishTestItemHandler, times(1)).finishTestItem(user, extractProjectDetails(user, "test_project"), itemId, finishTestItemRQ);
	}
}