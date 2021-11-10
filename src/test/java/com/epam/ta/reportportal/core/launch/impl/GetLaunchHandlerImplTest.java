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
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.core.jasper.GetJasperReportHandler;
import com.epam.ta.reportportal.core.jasper.util.JasperDataProvider;
import com.epam.ta.reportportal.dao.*;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.jasper.ReportFormat;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.converters.LaunchConverter;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.commons.querygen.constant.LaunchCriteriaConstant.CRITERIA_LAUNCH_STATUS;
import static com.epam.ta.reportportal.core.launch.impl.LaunchTestUtil.getLaunch;
import static com.epam.ta.reportportal.util.TestProjectExtractor.extractProjectDetails;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class GetLaunchHandlerImplTest {

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
	private GetLaunchHandlerImpl handler;

	@Test
	void getLaunchFromOtherProject() {
		final ReportPortalUser rpUser = getRpUser("test", UserRole.ADMINISTRATOR, ProjectRole.PROJECT_MANAGER, 2L);
		when(launchRepository.findById(1L)).thenReturn(getLaunch(StatusEnum.FAILED, LaunchModeEnum.DEFAULT));

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.getLaunch("1", extractProjectDetails(rpUser, "test_project"))
		);
		assertEquals("You do not have enough permissions.", exception.getMessage());
	}

	@Test
	void getDebugLaunchWithCustomerRole() {
		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.CUSTOMER, 1L);
		when(launchRepository.findById(1L)).thenReturn(getLaunch(StatusEnum.PASSED, LaunchModeEnum.DEBUG));

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.getLaunch("1", extractProjectDetails(rpUser, "test_project"))
		);
		assertEquals("You do not have enough permissions.", exception.getMessage());
	}

	@Test
	void getLaunchNamesIncorrectInput() {
		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.MEMBER, 1L);

		assertThrows(ReportPortalException.class, () -> handler.getLaunchNames(extractProjectDetails(rpUser, "test_project"), ""));
		assertThrows(
				ReportPortalException.class,
				() -> handler.getLaunchNames(extractProjectDetails(rpUser, "test_project"), RandomStringUtils.random(257))
		);
	}

	@Test
	void getNotExistLaunch() {
		ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.MEMBER, 1L);
		String launchId = "1";

		when(launchRepository.findById(Long.parseLong(launchId))).thenReturn(Optional.empty());

		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.getLaunch(launchId, extractProjectDetails(user, "test_project"))
		);
		assertEquals("Launch '1' not found. Did you use correct Launch ID?", exception.getMessage());
	}

	@Test
	void getLaunchByNotExistProjectName() {
		String projectName = "not_exist";

		when(projectRepository.findByName(projectName)).thenReturn(Optional.empty());

		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.getLaunchByProjectName(projectName, PageRequest.of(0, 10), getDefaultFilter(), "user")
		);
		assertEquals("Project 'not_exist' not found. Did you use correct project name?", exception.getMessage());
	}

	@Test
	void getLaunchByProjectNameNotFound() {
		String projectName = "not_exist";

		when(projectRepository.findByName(projectName)).thenReturn(Optional.of(new Project()));
		when(launchRepository.findByFilter(any(), any())).thenReturn(null);

		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.getLaunchByProjectName(projectName, PageRequest.of(0, 10), getDefaultFilter(), "user")
		);
		assertEquals("Launch '' not found. Did you use correct Launch ID?", exception.getMessage());
	}

	@Test
	void getLaunchesByNotExistProject() {
		long projectId = 1L;
		ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.PROJECT_MANAGER, projectId);

		when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.getProjectLaunches(extractProjectDetails(user, "test_project"),
						getDefaultFilter(),
						PageRequest.of(0, 10),
						"user"
				)
		);
		assertEquals("Project '1' not found. Did you use correct project name?", exception.getMessage());
	}

	@Test
	void getLatestLaunchesOnNotExistProject() {
		long projectId = 1L;
		ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.PROJECT_MANAGER, projectId);

		when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.getLatestLaunches(extractProjectDetails(user, "test_project"), getDefaultFilter(), PageRequest.of(0, 10))
		);
		assertEquals("Project '1' not found. Did you use correct project name?", exception.getMessage());
	}

	@Test
	void getOwnersWrongTerm() {
		long projectId = 1L;
		ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.PROJECT_MANAGER, projectId);

		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.getOwners(extractProjectDetails(user, "test_project"), "qw", LaunchModeEnum.DEFAULT.name())
		);
		assertEquals("Incorrect filtering parameters. Length of the filtering string 'qw' is less than 3 symbols", exception.getMessage());
	}

	@Test
	void getOwnersWrongMode() {
		long projectId = 1L;
		ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.PROJECT_MANAGER, projectId);

		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.getOwners(extractProjectDetails(user, "test_project"), "qwe", "incorrectMode")
		);
		assertEquals("Incorrect filtering parameters. Mode - incorrectMode doesn't exist.", exception.getMessage());
	}

	@Test
	void exportLaunchNotFound() {
		long launchId = 1L;
		ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.MEMBER, 1L);

		when(launchRepository.findById(launchId)).thenReturn(Optional.empty());

		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.exportLaunch(launchId, ReportFormat.PDF, null, user)
		);
		assertEquals("Launch '1' not found. Did you use correct Launch ID?", exception.getMessage());
	}

	@Test
	void exportLaunchUserNotFound() {
		long launchId = 1L;
		ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.MEMBER, 1L);

		Launch launch = new Launch();
		launch.setStatus(StatusEnum.FAILED);
		when(launchRepository.findById(launchId)).thenReturn(Optional.of(launch));
		when(userRepository.findById(1L)).thenReturn(Optional.empty());

		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.exportLaunch(launchId, ReportFormat.PDF, null, user)
		);
		assertEquals("User '1' not found.", exception.getMessage());
	}

	@Test
	void getLaunchInDebugModeByCustomer() {
		long projectId = 1L;
		ReportPortalUser user = getRpUser("user", UserRole.USER, ProjectRole.CUSTOMER, projectId);
		String launchId = "1";

		Launch launch = new Launch();
		launch.setProjectId(projectId);
		launch.setMode(LaunchModeEnum.DEBUG);
		when(launchRepository.findById(Long.parseLong(launchId))).thenReturn(Optional.of(launch));

		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.getLaunch(launchId, extractProjectDetails(user, "test_project"))
		);
		assertEquals("You do not have enough permissions.", exception.getMessage());
	}

	private Filter getDefaultFilter() {
		return Filter.builder()
				.withTarget(Launch.class)
				.withCondition(FilterCondition.builder().eq(CRITERIA_LAUNCH_STATUS, "PASSED").build())
				.build();
	}
}