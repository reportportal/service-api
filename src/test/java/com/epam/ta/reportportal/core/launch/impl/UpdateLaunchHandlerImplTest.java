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

package com.epam.ta.reportportal.core.launch.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.launch.UpdateLaunchRQ;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.core.launch.impl.LaunchTestUtil.getLaunch;
import static com.epam.ta.reportportal.util.TestProjectExtractor.extractProjectDetails;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class UpdateLaunchHandlerImplTest {

	@Mock
	private LaunchRepository launchRepository;

	@Mock
	private ProjectRepository projectRepository;

	@Mock
	private TestItemRepository testItemRepository;

	@InjectMocks
	private UpdateLaunchHandlerImpl handler;

	@Test
	void updateNotOwnLaunch() {
		final ReportPortalUser rpUser = getRpUser("not owner", UserRole.USER, ProjectRole.MEMBER, 1L);
		rpUser.setUserId(2L);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(new Project()));
		when(launchRepository.findById(1L)).thenReturn(getLaunch(StatusEnum.PASSED, LaunchModeEnum.DEFAULT));
		final ReportPortalException exception = assertThrows(
				ReportPortalException.class,
				() -> handler.updateLaunch(1L, extractProjectDetails(rpUser, "test_project"), rpUser, new UpdateLaunchRQ())
		);
		assertEquals("You do not have enough permissions.", exception.getMessage());
	}

	@Test
	void updateDebugLaunchByCustomer() {
		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.CUSTOMER, 1L);

		when(projectRepository.findById(1L)).thenReturn(Optional.of(new Project()));
		when(launchRepository.findById(1L)).thenReturn(getLaunch(StatusEnum.PASSED, LaunchModeEnum.DEFAULT));
		final UpdateLaunchRQ updateLaunchRQ = new UpdateLaunchRQ();
		updateLaunchRQ.setMode(Mode.DEBUG);

		final ReportPortalException exception = assertThrows(
				ReportPortalException.class,
				() -> handler.updateLaunch(1L, extractProjectDetails(rpUser, "test_project"), rpUser, updateLaunchRQ)
		);
		assertEquals("You do not have enough permissions.", exception.getMessage());
	}
}