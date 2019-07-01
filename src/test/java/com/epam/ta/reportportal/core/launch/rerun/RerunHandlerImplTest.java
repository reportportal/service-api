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

package com.epam.ta.reportportal.core.launch.rerun;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.LaunchStartedEvent;
import com.epam.ta.reportportal.core.item.UniqueIdGenerator;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributesRQ;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRS;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class RerunHandlerImplTest {

	@Mock
	private TestItemRepository testItemRepository;

	@Mock
	private LaunchRepository launchRepository;

	@Mock
	private UniqueIdGenerator uniqueIdGenerator;

	@Mock
	private MessageBus messageBus;

	@InjectMocks
	private RerunHandlerImpl rerunHandler;

	@Test
	void exceptionWhenLaunchIsNotStoredInDbByName() {
		StartLaunchRQ request = new StartLaunchRQ();
		String launchName = "launch";
		long projectId = 1L;
		request.setRerun(true);
		request.setName(launchName);
		ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.PROJECT_MANAGER, projectId);

		when(launchRepository.findLatestByNameAndProjectId(launchName, projectId)).thenReturn(Optional.empty());

		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> rerunHandler.handleLaunch(request, projectId, rpUser)
		);
		assertEquals("Launch 'launch' not found. Did you use correct Launch ID?", exception.getMessage());
	}

	@Test
	void exceptionWhenLaunchIsNotStoredInDbByUuid() {
		StartLaunchRQ request = new StartLaunchRQ();
		String launchName = "launch";
		String uuid = "uuid";
		long projectId = 1L;
		request.setRerun(true);
		request.setRerunOf(uuid);
		request.setName(launchName);
		ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.PROJECT_MANAGER, projectId);

		when(launchRepository.findByUuid(uuid)).thenReturn(Optional.empty());

		ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> rerunHandler.handleLaunch(request, projectId, rpUser)
		);
		assertEquals("Launch 'uuid' not found. Did you use correct Launch ID?", exception.getMessage());
	}

	@Test
	void happyRerunLaunch() {
		StartLaunchRQ request = new StartLaunchRQ();
		String launchName = "launch";
		long projectId = 1L;
		request.setRerun(true);
		request.setName(launchName);
		request.setMode(Mode.DEFAULT);
		request.setDescription("desc");
		request.setAttributes(Sets.newHashSet(new ItemAttributesRQ("test", "test")));
		ReportPortalUser rpUser = getRpUser("test", UserRole.USER, ProjectRole.PROJECT_MANAGER, projectId);

		when(launchRepository.findLatestByNameAndProjectId("launch", projectId)).thenReturn(Optional.of(getLaunch("uuid")));

		Optional<StartLaunchRS> responseOptional = rerunHandler.handleLaunch(request, projectId, rpUser);

		verify(messageBus, times(1)).publishActivity(any(LaunchStartedEvent.class));
		assertTrue(responseOptional.isPresent());
		assertNotNull(responseOptional.get().getNumber());
		assertNotNull(responseOptional.get().getId());
		assertNotNull(responseOptional.get().getUuid());

	}

	private Launch getLaunch(String uuid) {
		Launch launch = new Launch();
		launch.setUuid(uuid);
		launch.setNumber(1L);
		launch.setId(1L);
		return launch;
	}
}