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
import com.epam.ta.reportportal.core.shareable.impl.GetShareableDashboardHandlerImpl;
import com.epam.ta.reportportal.dao.DashboardRepository;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.util.TestProjectExtractor.extractProjectDetails;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class GetShareableDashboardHandlerImplTest {

	@Mock
	private DashboardRepository dashboardRepository;

	@InjectMocks
	private GetShareableDashboardHandlerImpl handler;

	@Test
	void getPermittedNotFound() {
		long projectId = 2L;
		long dashboardId = 1L;
		ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.PROJECT_MANAGER, projectId);

		when(dashboardRepository.findByIdAndProjectId(dashboardId, projectId)).thenReturn(Optional.empty());

		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.getPermitted(dashboardId, extractProjectDetails(user, "test_project"))
		);
		assertEquals("Dashboard with ID '1' not found on project 'test_project'. Did you use correct Dashboard ID?",
				exception.getMessage()
		);
	}

	@Test
	void getAdministratedNotFound() {
		long projectId = 2L;
		long dashboardId = 1L;
		ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.PROJECT_MANAGER, projectId);

		when(dashboardRepository.findByIdAndProjectId(dashboardId, projectId)).thenReturn(Optional.empty());

		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.getAdministrated(dashboardId, extractProjectDetails(user, "test_project"))
		);
		assertEquals("Dashboard with ID '1' not found on project 'test_project'. Did you use correct Dashboard ID?",
				exception.getMessage()
		);
	}
}