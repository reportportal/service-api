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

package com.epam.ta.reportportal.job;

import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.attribute.Attribute;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectAttribute;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class InterruptBrokenLaunchesJobTest {

	@Mock
	private LaunchRepository launchRepository;

	@Mock
	private LogRepository logRepository;

	@Mock
	private TestItemRepository testItemRepository;

	@Mock
	private ProjectRepository projectRepository;

	@InjectMocks
	private InterruptBrokenLaunchesJob interruptBrokenLaunchesJob;

	@Test
	void noInProgressItemsTest() {
		String name = "name";
		Project project = new Project();
		final ProjectAttribute projectAttribute = new ProjectAttribute();
		final Attribute attribute = new Attribute();
		attribute.setName("job.interruptJobTime");
		projectAttribute.setAttribute(attribute);

		//1 day in seconds
		projectAttribute.setValue(String.valueOf(3600 * 24));
		project.setProjectAttributes(Sets.newHashSet(projectAttribute));
		project.setName(name);

		long launchId = 1L;

		when(projectRepository.findAllIdsAndProjectAttributes(any())).thenReturn(new PageImpl<>(Collections.singletonList(project)));
		when(launchRepository.streamIdsWithStatusAndStartTimeBefore(any(), any(), any())).thenReturn(Stream.of(launchId));
		when(testItemRepository.hasItemsInStatusByLaunch(launchId, StatusEnum.IN_PROGRESS)).thenReturn(false);
		when(launchRepository.findById(launchId)).thenReturn(Optional.of(new Launch()));

		interruptBrokenLaunchesJob.execute(null);

		verify(launchRepository, times(1)).findById(launchId);
		verify(launchRepository, times(1)).save(any());

	}

	@Test
	void interruptLaunchWithInProgressItemsTest() {
		String name = "name";
		Project project = new Project();
		final ProjectAttribute projectAttribute = new ProjectAttribute();
		final Attribute attribute = new Attribute();
		attribute.setName("job.interruptJobTime");
		projectAttribute.setAttribute(attribute);

		//1 day in seconds
		projectAttribute.setValue(String.valueOf(3600 * 24));
		project.setProjectAttributes(Sets.newHashSet(projectAttribute));
		project.setName(name);

		long launchId = 1L;

		when(projectRepository.findAllIdsAndProjectAttributes(any())).thenReturn(new PageImpl<>(Collections.singletonList(project)));
		when(launchRepository.streamIdsWithStatusAndStartTimeBefore(any(), any(), any())).thenReturn(Stream.of(launchId));
		when(testItemRepository.hasItemsInStatusByLaunch(launchId, StatusEnum.IN_PROGRESS)).thenReturn(true);
		when(testItemRepository.hasItemsInStatusAddedLately(launchId, Duration.ofSeconds(3600 * 24),StatusEnum.IN_PROGRESS)).thenReturn(false);
		when(testItemRepository.hasLogs(launchId, Duration.ofSeconds(3600 * 24), StatusEnum.IN_PROGRESS)).thenReturn(true);
		when(logRepository.hasLogsAddedLately(Duration.ofSeconds(3600 * 24), launchId, StatusEnum.IN_PROGRESS)).thenReturn(false);
		when(launchRepository.findById(launchId)).thenReturn(Optional.of(new Launch()));

		interruptBrokenLaunchesJob.execute(null);

		verify(testItemRepository, times(1)).interruptInProgressItems(launchId);
		verify(launchRepository, times(1)).findById(launchId);
		verify(launchRepository, times(1)).save(any());

	}
}