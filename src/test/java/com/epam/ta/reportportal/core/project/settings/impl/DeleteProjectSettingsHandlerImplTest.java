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

package com.epam.ta.reportportal.core.project.settings.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.project.Project;
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
import static com.epam.ta.reportportal.util.ProjectExtractor.extractProjectDetails;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class DeleteProjectSettingsHandlerImplTest {

	@Mock
	private ProjectRepository projectRepository;

	@InjectMocks
	private DeleteProjectSettingsHandlerImpl handler;

	@Test
	void deleteSubtypeOnNotExistProject() {
		long projectId = 1L;
		ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.PROJECT_MANAGER, projectId);

		when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

		ReportPortalException exception = assertThrows(
				ReportPortalException.class,
				() -> handler.deleteProjectIssueSubType(extractProjectDetails(user, "test_project"), user, 1L)
		);

		assertEquals("Project '1' not found. Did you use correct project name?", exception.getMessage());
	}

	@Test
	void deleteNotExistSubtype() {
		long projectId = 1L;
		ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.PROJECT_MANAGER, projectId);

		when(projectRepository.findById(projectId)).thenReturn(Optional.of(new Project()));

		ReportPortalException exception = assertThrows(
				ReportPortalException.class,
				() -> handler.deleteProjectIssueSubType(extractProjectDetails(user, "test_project"), user, 1L)
		);

		assertEquals("Issue Type '1' not found.", exception.getMessage());
	}

}