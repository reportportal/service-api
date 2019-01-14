/*
 * Copyright 2018 EPAM Systems
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

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.epam.ta.reportportal.core.launch.impl.LaunchTestUtil.getLaunch;
import static com.epam.ta.reportportal.core.launch.impl.LaunchTestUtil.getReportPortalUser;
import static com.epam.ta.reportportal.util.ProjectExtractor.extractProjectDetails;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class DeleteLaunchHandlerTest {

	private LaunchRepository launchRepository = mock(LaunchRepository.class);
	private MessageBus messageBus = mock(MessageBus.class);

	private DeleteLaunchHandler handler = new DeleteLaunchHandler(launchRepository, messageBus);

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void deleteNotOwnLaunch() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("You do not have enough permissions.");

		final ReportPortalUser rpUser = getReportPortalUser("not owner", UserRole.USER, ProjectRole.MEMBER, 1L);
		when(launchRepository.findById(1L)).thenReturn(getLaunch(StatusEnum.PASSED, LaunchModeEnum.DEFAULT));

		handler.deleteLaunch(1L, extractProjectDetails(rpUser, "superadmin_personal"), rpUser);
	}

	@Test
	public void deleteLaunchFromAnotherProject() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("Forbidden operation. Target launch '1' not under specified project '2'");

		final ReportPortalUser rpUser = getReportPortalUser("test", UserRole.USER, ProjectRole.MEMBER, 2L);
		when(launchRepository.findById(1L)).thenReturn(getLaunch(StatusEnum.PASSED, LaunchModeEnum.DEFAULT));

		handler.deleteLaunch(1L, extractProjectDetails(rpUser, "superadmin_personal"), rpUser);
	}

	@Test
	public void deleteLaunchInProgressStatus() {
		thrown.expect(ReportPortalException.class);

		final ReportPortalUser rpUser = getReportPortalUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);
		when(launchRepository.findById(1L)).thenReturn(getLaunch(StatusEnum.IN_PROGRESS, LaunchModeEnum.DEFAULT));

		handler.deleteLaunch(1L, extractProjectDetails(rpUser, "superadmin_personal"), rpUser);
	}

}