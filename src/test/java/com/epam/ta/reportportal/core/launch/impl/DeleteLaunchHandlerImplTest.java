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
import com.epam.ta.reportportal.dao.AttachmentRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.core.launch.impl.LaunchTestUtil.getLaunch;
import static com.epam.ta.reportportal.util.ProjectExtractor.extractProjectDetails;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class DeleteLaunchHandlerImplTest {

	@Mock
	private LaunchRepository launchRepository;

	@Mock
	private AttachmentRepository attachmentRepository;

	@InjectMocks
	private DeleteLaunchHandlerImpl handler;

	@Test
	void deleteNotOwnLaunch() {
		final ReportPortalUser rpUser = getRpUser("not owner", UserRole.USER, ProjectRole.MEMBER, 1L);
		rpUser.setUserId(2L);
		when(launchRepository.findById(1L)).thenReturn(getLaunch(StatusEnum.PASSED, LaunchModeEnum.DEFAULT));

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.deleteLaunch(1L, extractProjectDetails(rpUser, "test_project"), rpUser)
		);
		assertEquals("You do not have enough permissions. You are not launch owner.", exception.getMessage());
	}

	@Test
	void deleteLaunchFromAnotherProject() {
		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 2L);
		when(launchRepository.findById(1L)).thenReturn(getLaunch(StatusEnum.PASSED, LaunchModeEnum.DEFAULT));

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.deleteLaunch(1L, extractProjectDetails(rpUser, "test_project"), rpUser)
		);
		assertEquals("Forbidden operation. Target launch '1' not under specified project '2'", exception.getMessage());
	}

	@Test
	void deleteLaunchInProgressStatus() {

		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);
		when(launchRepository.findById(1L)).thenReturn(getLaunch(StatusEnum.IN_PROGRESS, LaunchModeEnum.DEFAULT));

		assertThrows(ReportPortalException.class, () -> handler.deleteLaunch(1L, extractProjectDetails(rpUser, "test_project"), rpUser));
	}

}