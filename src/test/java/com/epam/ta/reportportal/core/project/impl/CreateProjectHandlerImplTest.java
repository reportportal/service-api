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

package com.epam.ta.reportportal.core.project.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.project.CreateProjectRQ;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class CreateProjectHandlerImplTest {

	@Mock
	private ProjectRepository projectRepository;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private CreateProjectHandlerImpl handler;

	@Test
	void createProjectWithWrongType() {
		ReportPortalUser rpUser = getRpUser("user", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L);

		CreateProjectRQ createProjectRQ = new CreateProjectRQ();
		String projectName = "projectName";
		createProjectRQ.setProjectName(projectName);
		createProjectRQ.setEntryType("wrongType");

		when(projectRepository.findByName(projectName.toLowerCase().trim())).thenReturn(Optional.empty());

		ReportPortalException exception = assertThrows(ReportPortalException.class, () -> handler.createProject(createProjectRQ, rpUser));

		assertEquals("Error in handled Request. Please, check specified parameters: 'wrongType'", exception.getMessage());
	}

	@Test
	void createProjectByNotExistUser() {
		ReportPortalUser rpUser = getRpUser("user", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 1L);

		CreateProjectRQ createProjectRQ = new CreateProjectRQ();
		String projectName = "projectName";
		createProjectRQ.setProjectName(projectName);
		createProjectRQ.setEntryType("internal");

		when(projectRepository.findByName(projectName.toLowerCase().trim())).thenReturn(Optional.empty());
		when(userRepository.findRawById(rpUser.getUserId())).thenReturn(Optional.empty());

		ReportPortalException exception = assertThrows(ReportPortalException.class, () -> handler.createProject(createProjectRQ, rpUser));

		assertEquals("User 'user' not found.", exception.getMessage());
	}
}