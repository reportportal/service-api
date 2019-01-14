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
import com.epam.ta.reportportal.dao.ItemAttributeRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
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
public class GetLaunchHandlerTest {

	private LaunchRepository launchRepository = mock(LaunchRepository.class);
	private ItemAttributeRepository itemAttributeRepository = mock(ItemAttributeRepository.class);
	private ProjectRepository projectRepository = mock(ProjectRepository.class);
	private WidgetContentRepository widgetContentRepository = mock(WidgetContentRepository.class);

	private GetLaunchHandler handler = new GetLaunchHandler(launchRepository,
			itemAttributeRepository,
			projectRepository,
			widgetContentRepository
	);

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void getLaunchFromOtherProject() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("You do not have enough permissions.");

		final ReportPortalUser rpUser = getReportPortalUser("test", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 2L);
		when(launchRepository.findById(1L)).thenReturn(getLaunch(StatusEnum.FAILED, LaunchModeEnum.DEFAULT));

		handler.getLaunch(1L, extractProjectDetails(rpUser, "superadmin_personal"));
	}

	@Test
	public void getDebugLaunchWithCustomerRole() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage("You do not have enough permissions.");

		final ReportPortalUser rpUser = getReportPortalUser("test", UserRole.USER, ProjectRole.CUSTOMER, 1L);
		when(launchRepository.findById(1L)).thenReturn(getLaunch(StatusEnum.PASSED, LaunchModeEnum.DEBUG));

		handler.getLaunch(1L, extractProjectDetails(rpUser, "superadmin_personal"));
	}

	@Test
	public void getLaunchNamesIncorrectInput() {
		thrown.expect(ReportPortalException.class);

		final ReportPortalUser rpUser = getReportPortalUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);

		handler.getLaunchNames(extractProjectDetails(rpUser, "superadmin_personal"), "qw");
	}
}