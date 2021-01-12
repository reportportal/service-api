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
import com.epam.ta.reportportal.job.service.LaunchCleanerService;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobExecutionException;
import org.springframework.data.domain.PageImpl;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class CleanLaunchesJobTest {

	private final LaunchCleanerService launchCleanerService = mock(LaunchCleanerService.class);

	private final ProjectRepository projectRepository = mock(ProjectRepository.class);
	private final ActivityRepository activityRepository = mock(ActivityRepository.class);
	private final LaunchRepository launchRepository = mock(LaunchRepository.class);

	private final CleanLaunchesJob cleanLaunchesJob = new CleanLaunchesJob(5,
			100,
			launchCleanerService,
			projectRepository,
			activityRepository,
			launchRepository
	);

	@Test
	void executeTest() throws JobExecutionException {
		String name = "name";
		Project project = new Project();
		project.setId(123L);
		final ProjectAttribute projectAttribute = new ProjectAttribute();
		final Attribute attribute = new Attribute();
		attribute.setName("job.keepLaunches");
		projectAttribute.setAttribute(attribute);

		//1 month in seconds
		projectAttribute.setValue(String.valueOf(3600 * 24 * 30));
		project.setProjectAttributes(Sets.newHashSet(projectAttribute));

		project.setName(name);

		when(projectRepository.findAllIdsAndProjectAttributes(any())).thenReturn(new PageImpl<>(Collections.singletonList(project)));
		when(launchRepository.findIdsByProjectIdAndStartTimeBefore(eq(project.getId()), any(), anyInt())).thenReturn(Lists.newArrayList(1L));

		cleanLaunchesJob.execute(null);

		verify(launchCleanerService, times(1)).cleanLaunch(any(), any(), any());
	}

	@Test
	void wrongAttributeValue() throws JobExecutionException {
		String name = "name";
		Project project = new Project();
		final ProjectAttribute projectAttribute = new ProjectAttribute();
		final Attribute attribute = new Attribute();
		attribute.setName("job.keepLaunches");
		projectAttribute.setAttribute(attribute);
		projectAttribute.setValue("wrong");
		project.setProjectAttributes(Sets.newHashSet(projectAttribute));

		project.setName(name);

		when(projectRepository.findAllIdsAndProjectAttributes(any())).thenReturn(new PageImpl<>(Collections.singletonList(project)));

		cleanLaunchesJob.execute(null);

		verify(launchCleanerService, times(0)).cleanLaunch(any(), any(), any());
	}
}