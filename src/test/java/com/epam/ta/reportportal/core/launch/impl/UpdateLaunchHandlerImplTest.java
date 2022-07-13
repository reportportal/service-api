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
import com.epam.ta.reportportal.core.item.impl.LaunchAccessValidator;
import com.epam.ta.reportportal.core.launch.GetLaunchHandler;
import com.epam.ta.reportportal.core.launch.cluster.UniqueErrorAnalysisStarter;
import com.epam.ta.reportportal.core.launch.cluster.config.ClusterEntityContext;
import com.epam.ta.reportportal.core.project.GetProjectHandler;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.launch.UpdateLaunchRQ;
import com.epam.ta.reportportal.ws.model.launch.cluster.CreateClustersRQ;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.core.launch.impl.LaunchTestUtil.getLaunch;
import static com.epam.ta.reportportal.util.TestProjectExtractor.extractProjectDetails;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class UpdateLaunchHandlerImplTest {

	@Mock
	private LaunchAccessValidator launchAccessValidator;

	@Mock
	private LaunchRepository launchRepository;

	@Mock
	private GetProjectHandler getProjectHandler;

	@Mock
	private GetLaunchHandler getLaunchHandler;

	@Mock
	private TestItemRepository testItemRepository;

	@Mock
	private UniqueErrorAnalysisStarter starter;

	@InjectMocks
	private UpdateLaunchHandlerImpl handler;

	@Test
	void updateNotOwnLaunch() {
		final ReportPortalUser rpUser = getRpUser("not owner", UserRole.USER, ProjectRole.MEMBER, 1L);
		rpUser.setUserId(2L);
		when(getProjectHandler.get(any(ReportPortalUser.ProjectDetails.class))).thenReturn(new Project());
		when(launchRepository.findById(1L)).thenReturn(getLaunch(StatusEnum.PASSED, LaunchModeEnum.DEFAULT));
		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.updateLaunch(1L, extractProjectDetails(rpUser, "test_project"), rpUser, new UpdateLaunchRQ())
		);
		assertEquals("You do not have enough permissions.", exception.getMessage());
	}

	@Test
	void updateDebugLaunchByCustomer() {
		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.CUSTOMER, 1L);

		when(getProjectHandler.get(any(ReportPortalUser.ProjectDetails.class))).thenReturn(new Project());
		when(launchRepository.findById(1L)).thenReturn(getLaunch(StatusEnum.PASSED, LaunchModeEnum.DEFAULT));
		final UpdateLaunchRQ updateLaunchRQ = new UpdateLaunchRQ();
		updateLaunchRQ.setMode(Mode.DEBUG);

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.updateLaunch(1L, extractProjectDetails(rpUser, "test_project"), rpUser, updateLaunchRQ)
		);
		assertEquals("You do not have enough permissions.", exception.getMessage());
	}

	@Test
	void createClustersLaunchInProgress() {

		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.CUSTOMER, 1L);

		when(getLaunchHandler.get(1L)).thenReturn(getLaunch(StatusEnum.IN_PROGRESS, LaunchModeEnum.DEFAULT).get());
		final CreateClustersRQ createClustersRQ = new CreateClustersRQ();
		createClustersRQ.setLaunchId(1L);
		createClustersRQ.setRemoveNumbers(false);

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> handler.createClusters(createClustersRQ, extractProjectDetails(rpUser, "test_project"), rpUser)
		);
		assertEquals("Incorrect Request. Cannot analyze launch in progress.", exception.getMessage());
		verify(launchAccessValidator, times(1)).validate(any(Launch.class), any(ReportPortalUser.ProjectDetails.class), eq(rpUser));
	}

	@Test
	void createClusters() {

		final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.CUSTOMER, 1L);

		final Project project = new Project();
		project.setId(1L);

		final Launch launch = getLaunch(StatusEnum.PASSED, LaunchModeEnum.DEFAULT).get();
		when(getLaunchHandler.get(1L)).thenReturn(launch);
		when(getProjectHandler.get(launch.getProjectId())).thenReturn(project);

		final CreateClustersRQ createClustersRQ = new CreateClustersRQ();
		createClustersRQ.setLaunchId(1L);
		final boolean defaultRemoveNumbers = Boolean.parseBoolean(ProjectAttributeEnum.UNIQUE_ERROR_ANALYZER_REMOVE_NUMBERS.getDefaultValue());
		createClustersRQ.setRemoveNumbers(!defaultRemoveNumbers);

		handler.createClusters(createClustersRQ, extractProjectDetails(rpUser, "test_project"), rpUser);

		verify(launchAccessValidator, times(1)).validate(any(Launch.class), any(ReportPortalUser.ProjectDetails.class), eq(rpUser));

		final ArgumentCaptor<ClusterEntityContext> contextCaptor = ArgumentCaptor.forClass(ClusterEntityContext.class);
		final ArgumentCaptor<Map<String, String>> mapCaptor = ArgumentCaptor.forClass(Map.class);
		verify(starter, times(1)).start(contextCaptor.capture(), mapCaptor.capture());

		final ClusterEntityContext entityContext = contextCaptor.getValue();

		assertEquals(1L, entityContext.getProjectId().longValue());
		assertEquals(1L, entityContext.getLaunchId().longValue());

		final Map<String, String> providedConfig = mapCaptor.getValue();

		final boolean providedRemoveNumbers = Boolean.parseBoolean(providedConfig.get(ProjectAttributeEnum.UNIQUE_ERROR_ANALYZER_REMOVE_NUMBERS.getAttribute()));
		assertNotEquals(providedRemoveNumbers, defaultRemoveNumbers);
		assertEquals(createClustersRQ.isRemoveNumbers(), providedRemoveNumbers);
	}
}