/*
 * Copyright 2020 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.job.service.impl;

import com.epam.ta.reportportal.binary.DataStoreService;
import com.epam.ta.reportportal.dao.AttachmentRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.attachment.Attachment;
import com.epam.ta.reportportal.entity.project.Project;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class AttachmentCleanerServiceImplTest {

	private AttachmentRepository attachmentRepository = mock(AttachmentRepository.class);
	private LaunchRepository launchRepository = mock(LaunchRepository.class);
	private TestItemRepository testItemRepository = mock(TestItemRepository.class);
	private DataStoreService dataStoreService = mock(DataStoreService.class);

	private final AttachmentCleanerServiceImpl attachmentCleanerService = new AttachmentCleanerServiceImpl(500,
			attachmentRepository,
			launchRepository,
			testItemRepository,
			dataStoreService
	);

	@Test
	void removeOutdatedItemsAttachmentsPositiveTest() {
		ArrayList<Long> itemIds = Lists.newArrayList(1L, 2L, 3L);
		LocalDateTime before = LocalDateTime.now().minus(ChronoUnit.WEEKS.getDuration());
		AtomicLong attachmentCount = new AtomicLong();
		AtomicLong thumbnailCount = new AtomicLong();

		String fileId = "fileId";
		String thumbnailId = "thumbnailId";
		Attachment attachment = testAttachment(fileId, thumbnailId);

		when(attachmentRepository.findByItemIdsAndLogTimeBefore(itemIds, before)).thenReturn(Collections.singletonList(attachment));

		attachmentCleanerService.removeOutdatedItemsAttachments(itemIds, before, attachmentCount, thumbnailCount);

		assertEquals(1L, attachmentCount.get());
		assertEquals(1L, thumbnailCount.get());
		verify(dataStoreService, times(1)).delete(fileId);
		verify(dataStoreService, times(1)).delete(thumbnailId);
		verify(attachmentRepository, times(1)).deleteAllByIds(Collections.singletonList(attachment.getId()));
	}

	@Test
	void removeOutdatedLaunchesAttachmentsPositive() {
		ArrayList<Long> launchIds = Lists.newArrayList(1L, 2L, 3L);
		AtomicLong attachmentCount = new AtomicLong();
		AtomicLong thumbnailCount = new AtomicLong();

		String fileId = "fileId";
		String thumbnailId = "thumbnailId";
		Attachment attachment = testAttachment(fileId, thumbnailId);

		when(attachmentRepository.findAllByLaunchIdIn(launchIds)).thenReturn(Collections.singletonList(attachment));

		attachmentCleanerService.removeOutdatedLaunchesAttachments(launchIds, attachmentCount, thumbnailCount);

		assertEquals(1L, attachmentCount.get());
		assertEquals(1L, thumbnailCount.get());
		verify(dataStoreService, times(1)).delete(fileId);
		verify(dataStoreService, times(1)).delete(thumbnailId);
		verify(attachmentRepository, times(1)).deleteAllByIds(Collections.singletonList(attachment.getId()));
	}

	@Test
	void removeProjectAttachmentsPositive() {
		Project project = new Project();
		project.setId(111L);
		LocalDateTime before = LocalDateTime.now().minus(ChronoUnit.WEEKS.getDuration());
		AtomicLong attachmentCount = new AtomicLong();
		AtomicLong thumbnailCount = new AtomicLong();

		when(launchRepository.streamIdsByStartTimeBefore(project.getId(), before)).thenReturn(Stream.of(1L, 2L, 3L));
		when(testItemRepository.findTestItemIdsByLaunchId(eq(1L), any(Pageable.class))).thenReturn(Lists.newArrayList(11L));
		when(testItemRepository.findTestItemIdsByLaunchId(eq(2L), any(Pageable.class))).thenReturn(Lists.newArrayList(22L));
		when(testItemRepository.findTestItemIdsByLaunchId(eq(3L), any(Pageable.class))).thenReturn(Lists.newArrayList(33L));
		when(attachmentRepository.findByItemIdsAndLogTimeBefore(Collections.singletonList(11L),
				before
		)).thenReturn(Collections.singletonList(testAttachment("one", "two")));
		when(attachmentRepository.findByItemIdsAndLogTimeBefore(Collections.singletonList(22L),
				before
		)).thenReturn(Collections.singletonList(testAttachment("three", "four")));
		when(attachmentRepository.findByItemIdsAndLogTimeBefore(Collections.singletonList(33L),
				before
		)).thenReturn(Collections.singletonList(testAttachment("five", null)));

		when(attachmentRepository.findByLaunchIdsAndLogTimeBefore(Collections.singletonList(1L),
				before
		)).thenReturn(Collections.singletonList(testAttachment("firstLaunch", "two")));
		when(attachmentRepository.findByLaunchIdsAndLogTimeBefore(Collections.singletonList(2L),
				before
		)).thenReturn(Collections.singletonList(testAttachment("secondLaunch", null)));

		attachmentCleanerService.removeProjectAttachments(project, before, attachmentCount, thumbnailCount);

		assertEquals(5L, attachmentCount.get());
		assertEquals(3L, thumbnailCount.get());
		verify(dataStoreService, times(8)).delete(anyString());
		verify(attachmentRepository, times(5)).deleteAllByIds(anyCollection());
	}

	private static Attachment testAttachment(String fileId, String thumbnailId) {
		Attachment attachment = new Attachment();
		attachment.setId(100L);
		attachment.setFileId(fileId);
		attachment.setThumbnailId(thumbnailId);
		return attachment;
	}
}