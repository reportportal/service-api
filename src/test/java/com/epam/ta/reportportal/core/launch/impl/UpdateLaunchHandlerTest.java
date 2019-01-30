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

import com.epam.ta.reportportal.dao.LaunchRepository;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.mockito.Mockito.mock;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class UpdateLaunchHandlerTest {

	private LaunchRepository launchRepository = mock(LaunchRepository.class);

//	private UpdateLaunchHandler handler = new UpdateLaunchHandler(launchRepository);

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void updateNotOwnLaunch() {
//		thrown.expect(ReportPortalException.class);
//		thrown.expectMessage("You do not have enough permissions.");
//
//		final ReportPortalUser rpUser = getRpUser("not owner", UserRole.USER, ProjectRole.MEMBER, 1L);
//		when(launchRepository.findById(1L)).thenReturn(getLaunch(StatusEnum.PASSED, LaunchModeEnum.DEFAULT));
//
//		handler.updateLaunch(1L, extractProjectDetails(rpUser, "test_project"), rpUser, new UpdateLaunchRQ());
	}

	@Test
	public void updateDebugLaunchByCustomer() {
//		thrown.expect(ReportPortalException.class);
//		thrown.expectMessage("You do not have enough permissions.");
//
//		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.CUSTOMER, 1L);
//		when(launchRepository.findById(1L)).thenReturn(getLaunch(StatusEnum.PASSED, LaunchModeEnum.DEFAULT));
//		final UpdateLaunchRQ updateLaunchRQ = new UpdateLaunchRQ();
//		updateLaunchRQ.setMode(Mode.DEBUG);
//
//		handler.updateLaunch(1L, extractProjectDetails(rpUser, "test_project"), rpUser, updateLaunchRQ);
	}
}