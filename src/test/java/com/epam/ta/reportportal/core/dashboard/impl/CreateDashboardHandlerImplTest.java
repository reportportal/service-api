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

package com.epam.ta.reportportal.core.dashboard.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.dao.DashboardRepository;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.dashboard.CreateDashboardRQ;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.util.TestProjectExtractor.extractProjectDetails;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class CreateDashboardHandlerImplTest {

	@Mock
	private DashboardRepository dashboardRepository;

	@InjectMocks
	private CreateDashboardHandlerImpl handler;

	@Test
	void createAlreadyExistDashboard() {
		CreateDashboardRQ createDashboardRQ = new CreateDashboardRQ();
		createDashboardRQ.setName("exist");

		final ReportPortalUser rpUser = getRpUser("owner", UserRole.USER, ProjectRole.MEMBER, 1L);

		when(dashboardRepository.existsByNameAndOwnerAndProjectId("exist", "owner", 1L)).thenReturn(true);
		final ReportPortalException exception = assertThrows(
				ReportPortalException.class,
				() -> handler.createDashboard(extractProjectDetails(rpUser, "test_project"), createDashboardRQ, rpUser)
		);
		assertEquals("Resource 'exist' already exists. You couldn't create the duplicate.", exception.getMessage());
	}
}