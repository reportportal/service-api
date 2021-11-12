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

package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.item.FinishTestItemHandler;
import com.epam.ta.reportportal.core.item.StartTestItemHandler;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Konstantin Antipin
 */
@ExtendWith(MockitoExtension.class)
class TestItemAsyncControllerTest {

	@Mock
	ProjectExtractor projectExtractor;

	@Mock
	StartTestItemHandler startTestItemHandler;

	@Mock
	FinishTestItemHandler finishTestItemHandler;

	@InjectMocks
	TestItemAsyncController testItemAsyncController;

	@Test
	void startRootItem() {
		ReportPortalUser user = getRpUser("test", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L);

		StartTestItemRQ startTestItemRQ = new StartTestItemRQ();

		ArgumentCaptor<ReportPortalUser> userArgumentCaptor = ArgumentCaptor.forClass(ReportPortalUser.class);
		ArgumentCaptor<ReportPortalUser.ProjectDetails> projectDetailsArgumentCaptor = ArgumentCaptor.forClass(ReportPortalUser.ProjectDetails.class);
		ArgumentCaptor<StartTestItemRQ> requestArgumentCaptor = ArgumentCaptor.forClass(StartTestItemRQ.class);

		when(projectExtractor.extractProjectDetails(any(ReportPortalUser.class), anyString())).thenReturn(user.getProjectDetails()
				.get("test_project"));

		testItemAsyncController.startRootItem("test_project", user, startTestItemRQ);
		verify(startTestItemHandler).startRootItem(userArgumentCaptor.capture(),
				projectDetailsArgumentCaptor.capture(),
				requestArgumentCaptor.capture()
		);
		assertEquals(user, userArgumentCaptor.getValue());
		assertEquals(user.getProjectDetails().get("test_project"), projectDetailsArgumentCaptor.getValue());
		assertEquals(startTestItemRQ, requestArgumentCaptor.getValue());
	}

	@Test
	void startChildItem() {
		ReportPortalUser user = getRpUser("test", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L);

		StartTestItemRQ startTestItemRQ = new StartTestItemRQ();
		String parentItem = "parent";

		ArgumentCaptor<ReportPortalUser> userArgumentCaptor = ArgumentCaptor.forClass(ReportPortalUser.class);
		ArgumentCaptor<ReportPortalUser.ProjectDetails> projectDetailsArgumentCaptor = ArgumentCaptor.forClass(ReportPortalUser.ProjectDetails.class);
		ArgumentCaptor<StartTestItemRQ> requestArgumentCaptor = ArgumentCaptor.forClass(StartTestItemRQ.class);
		ArgumentCaptor<String> parentArgumentCaptor = ArgumentCaptor.forClass(String.class);

		when(projectExtractor.extractProjectDetails(any(ReportPortalUser.class), anyString())).thenReturn(user.getProjectDetails()
				.get("test_project"));

		testItemAsyncController.startChildItem("test_project", user, parentItem, startTestItemRQ);
		verify(startTestItemHandler).startChildItem(userArgumentCaptor.capture(),
				projectDetailsArgumentCaptor.capture(),
				requestArgumentCaptor.capture(),
				parentArgumentCaptor.capture()
		);
		assertEquals(user, userArgumentCaptor.getValue());
		assertEquals(user.getProjectDetails().get("test_project"), projectDetailsArgumentCaptor.getValue());
		assertEquals(startTestItemRQ, requestArgumentCaptor.getValue());
	}

	@Test
	void finishTestItem() {
		ReportPortalUser user = getRpUser("test", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L);

		FinishTestItemRQ finishTestItemRQ = new FinishTestItemRQ();
		String testItemId = UUID.randomUUID().toString();
		finishTestItemRQ.setLaunchUuid(UUID.randomUUID().toString());

		ArgumentCaptor<ReportPortalUser> userArgumentCaptor = ArgumentCaptor.forClass(ReportPortalUser.class);
		ArgumentCaptor<ReportPortalUser.ProjectDetails> projectDetailsArgumentCaptor = ArgumentCaptor.forClass(ReportPortalUser.ProjectDetails.class);
		ArgumentCaptor<String> testItemCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<FinishTestItemRQ> requestArgumentCaptor = ArgumentCaptor.forClass(FinishTestItemRQ.class);

		when(projectExtractor.extractProjectDetails(any(ReportPortalUser.class), anyString())).thenReturn(user.getProjectDetails()
				.get("test_project"));

		testItemAsyncController.finishTestItem("test_project", user, testItemId, finishTestItemRQ);
		verify(finishTestItemHandler).finishTestItem(userArgumentCaptor.capture(),
				projectDetailsArgumentCaptor.capture(),
				testItemCaptor.capture(),
				requestArgumentCaptor.capture()
		);
		assertEquals(user, userArgumentCaptor.getValue());
		assertEquals(user.getProjectDetails().get("test_project"), projectDetailsArgumentCaptor.getValue());
		assertEquals(finishTestItemRQ, requestArgumentCaptor.getValue());
	}
}