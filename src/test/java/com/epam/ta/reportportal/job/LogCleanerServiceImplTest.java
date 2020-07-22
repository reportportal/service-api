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

import com.epam.ta.reportportal.binary.DataStoreService;
import com.epam.ta.reportportal.dao.*;
import com.epam.ta.reportportal.entity.attachment.Attachment;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.job.service.impl.AttachmentCleanerServiceImpl;
import com.epam.ta.reportportal.job.service.impl.LogCleanerServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static java.time.Duration.ofDays;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class LogCleanerServiceImplTest {

	@Mock
	private LogRepository logRepository;

	@Mock
	private LaunchRepository launchRepository;

	@Mock
	private TestItemRepository testItemRepository;

	@Mock
	private DataStoreService dataStoreService;

	@Mock
	private ActivityRepository activityRepository;

	@Mock
	private AttachmentRepository attachmentRepository;

	@Mock
	private AttachmentCleanerServiceImpl attachmentCleanerService;

	@InjectMocks
	private LogCleanerServiceImpl logCleanerService;

	@Test
	void removeOutdatedLogs() {

		Project project = new Project();
		project.setId(1L);
		Duration period = ofDays(180);
		AtomicLong removedLogsCount = new AtomicLong();

		long launchId = 1L;
		long testItemId = 2L;
		Log log1 = new Log();
		Attachment attachment1 = new Attachment();
		attachment1.setId(1L);
		attachment1.setFileId("qewr");
		attachment1.setThumbnailId("asd");
		log1.setAttachment(attachment1);
		Log log2 = new Log();
		Attachment attachment2 = new Attachment();
		attachment2.setId(2L);
		attachment2.setFileId("zxc");
		attachment2.setThumbnailId("jkl");
		log2.setAttachment(attachment2);

		int deletedLogsCount = 2;

		when(launchRepository.streamIdsByStartTimeBefore(eq(project.getId()), any(LocalDateTime.class))).thenReturn(Stream.of(launchId));
		when(testItemRepository.streamTestItemIdsByLaunchId(launchId)).thenReturn(Stream.of(testItemId));
		when(logRepository.deleteByPeriodAndTestItemIds(eq(period), any())).thenReturn(deletedLogsCount);
		when(logRepository.deleteByPeriodAndLaunchIds(eq(period), any())).thenReturn(deletedLogsCount);
		logCleanerService.removeOutdatedLogs(project, period, removedLogsCount);

		assertEquals(deletedLogsCount * 2, removedLogsCount.get());
		verify(attachmentCleanerService, times(1)).removeOutdatedItemsAttachments(eq(Collections.singletonList(testItemId)),
				any(),
				any(),
				any()
		);
		verify(attachmentCleanerService, times(1)).removeOutdatedLaunchesAttachments(eq(Collections.singletonList(launchId)),
				any(),
				any(),
				any()
		);
		verify(activityRepository, times(1)).deleteModifiedLaterAgo(project.getId(), period);
	}
}