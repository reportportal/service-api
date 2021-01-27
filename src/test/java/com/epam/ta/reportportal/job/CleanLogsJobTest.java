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

import com.epam.ta.reportportal.dao.ActivityRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.attribute.Attribute;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectAttribute;
import com.epam.ta.reportportal.job.service.LogCleanerService;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobExecutionException;
import org.springframework.data.domain.PageImpl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class CleanLogsJobTest {

	private final LogCleanerService logCleanerService = mock(LogCleanerService.class);

	private final ProjectRepository projectRepository = mock(ProjectRepository.class);
	private final ActivityRepository activityRepository = mock(ActivityRepository.class);
	private final LaunchRepository launchRepository = mock(LaunchRepository.class);

	private final CleanLogsJob cleanLogsJob = new CleanLogsJob(5,
			100,
			logCleanerService,
			projectRepository,
			activityRepository,
			launchRepository
	);

	@Test
	void executeTest() throws JobExecutionException {

		String name = "name";
		Project project = new Project();
		final ProjectAttribute projectAttribute = new ProjectAttribute();
		final Attribute attribute = new Attribute();
		attribute.setName("job.keepLogs");
		final Duration duration = Duration.ofSeconds(3600 * 24 * 30);
		final LocalDateTime toCompare = java.time.LocalDateTime.now(ZoneOffset.UTC).minus(duration);
		projectAttribute.setAttribute(attribute);
		//1 month in seconds
		projectAttribute.setValue(String.valueOf(duration.getSeconds()));
		project.setProjectAttributes(Sets.newHashSet(projectAttribute));

		project.setName(name);

		when(projectRepository.findAllIdsAndProjectAttributes(any())).thenReturn(new PageImpl<>(Collections.singletonList(project)));
		when(launchRepository.findIdsByProjectIdAndStartTimeBefore(eq(project.getId()),
				any(),
				anyInt(),
				anyLong()
		)).thenReturn(Lists.newArrayList(1L));

		cleanLogsJob.execute(null);

		ArgumentCaptor<LocalDateTime> dateTimeArgumentCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
		verify(logCleanerService, times(1)).removeOutdatedLogs(any(), dateTimeArgumentCaptor.capture(), any(), any());

		LocalDateTime value = dateTimeArgumentCaptor.getValue();

		Assertions.assertTrue(value.compareTo(toCompare) >= 0);
	}

	@Test
	void wrongAttributeValue() throws JobExecutionException {
		String name = "name";
		Project project = new Project();
		final ProjectAttribute projectAttribute = new ProjectAttribute();
		final Attribute attribute = new Attribute();
		attribute.setName("job.keepLogs");
		projectAttribute.setAttribute(attribute);
		projectAttribute.setValue("wrong");
		project.setProjectAttributes(Sets.newHashSet(projectAttribute));

		project.setName(name);

		when(projectRepository.findAllIdsAndProjectAttributes(any())).thenReturn(new PageImpl<>(Collections.singletonList(project)));

		cleanLogsJob.execute(null);

		verify(logCleanerService, times(0)).removeOutdatedLogs(any(), any(), any(), any());
	}
}