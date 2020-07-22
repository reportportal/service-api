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
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.job.service.AttachmentCleanerService;
import com.epam.ta.reportportal.job.service.impl.LaunchCleanerServiceImpl;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

import static java.time.Duration.ofDays;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class LaunchCleanerServiceImplTest {

	@Mock
	private LaunchRepository launchRepository;

	@Mock
	private ActivityRepository activityRepository;

	@Mock
	private AttachmentCleanerService attachmentCleanerService;

	@InjectMocks
	private LaunchCleanerServiceImpl launchCleanerService;

	@Test
	void runTest() {
		Project project = new Project();
		project.setId(1L);
		Duration period = ofDays(180);
		AtomicLong launchesRemoved = new AtomicLong();
		AtomicLong attachmentsRemoved = new AtomicLong();
		AtomicLong thumbnailsRemoved = new AtomicLong();
		ArrayList<Long> launchIds = Lists.newArrayList(1L, 2L, 3L);

		when(launchRepository.findIdsByProjectIdAndStartTimeBefore(eq(project.getId()), any(LocalDateTime.class))).thenReturn(launchIds);

		launchCleanerService.cleanOutdatedLaunches(project, period, launchesRemoved, attachmentsRemoved, thumbnailsRemoved);

		assertEquals(launchIds.size(), launchesRemoved.get());
		verify(activityRepository, times(1)).deleteModifiedLaterAgo(project.getId(), period);
		verify(launchRepository, times(1)).deleteAllByIdIn(launchIds);
		verify(attachmentCleanerService, times(1)).removeOutdatedLaunchesAttachments(launchIds, attachmentsRemoved, thumbnailsRemoved);
	}
}