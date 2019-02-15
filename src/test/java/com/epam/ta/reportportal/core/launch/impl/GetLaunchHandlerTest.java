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
import com.epam.ta.reportportal.core.jasper.GetJasperReportHandler;
import com.epam.ta.reportportal.core.jasper.util.JasperDataProvider;
import com.epam.ta.reportportal.dao.*;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.converters.LaunchConverter;
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
class GetLaunchHandlerTest {

	@Mock
	private LaunchRepository launchRepository;

	@Mock
	private ItemAttributeRepository itemAttributeRepository;

	@Mock
	private ProjectRepository projectRepository;

	@Mock
	private WidgetContentRepository widgetContentRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private JasperDataProvider jasperDataProvider;

	@Mock
	private GetJasperReportHandler<Launch> getJasperReportHandler;

	@Mock
	private LaunchConverter launchConverter;

	@InjectMocks
	private GetLaunchHandler handler;

	@Test
	void getLaunchFromOtherProject() {
		final ReportPortalUser rpUser = getRpUser("test", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 2L);
		when(launchRepository.findById(1L)).thenReturn(getLaunch(StatusEnum.FAILED, LaunchModeEnum.DEFAULT));

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.getLaunch(1L, extractProjectDetails(rpUser, "test_project"))
		);
		assertEquals("You do not have enough permissions.", exception.getMessage());
	}

	@Test
	void getDebugLaunchWithCustomerRole() {
		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.CUSTOMER, 1L);
		when(launchRepository.findById(1L)).thenReturn(getLaunch(StatusEnum.PASSED, LaunchModeEnum.DEBUG));

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.getLaunch(1L, extractProjectDetails(rpUser, "test_project"))
		);
		assertEquals("You do not have enough permissions.", exception.getMessage());
	}

	@Test
	void getLaunchNamesIncorrectInput() {
		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);

		assertThrows(ReportPortalException.class, () -> handler.getLaunchNames(extractProjectDetails(rpUser, "test_project"), "qw"));
	}
}