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

package com.epam.ta.reportportal.core.log.impl;

import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.entity.attachment.Attachment;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class CreateAttachmentHandlerTest {

	@Mock
	private LogRepository logRepository;

	@InjectMocks
	private CreateAttachmentHandler createAttachmentHandler;

	@Test
	void createAttachmentPositive() {
		Log log = getLogWitoutAttachment();
		Attachment attachment = getAttachement();
		when(logRepository.findById(1L)).thenReturn(Optional.of(log));

		createAttachmentHandler.create(attachment, 1L);

		verify(logRepository, times(1)).save(log);

		assertEquals(log.getAttachment().getFileId(), attachment.getFileId());
		assertEquals(log.getAttachment().getThumbnailId(), attachment.getThumbnailId());
		assertEquals(log.getAttachment().getContentType(), attachment.getContentType());
	}

	@Test
	void createAttachmentOnNotExistLog() {
		long logId = 1L;
		when(logRepository.findById(logId)).thenReturn(Optional.empty());

		assertThrows(ReportPortalException.class, () -> createAttachmentHandler.create(getAttachement(), logId));
	}

	private Log getLogWitoutAttachment() {
		Log log = new Log();
		log.setId(1L);
		log.setLaunch(new Launch(2L));
		log.setTestItem(new TestItem(3L));
		log.setLogLevel(4000);
		log.setLogMessage("message");
		log.setLogTime(LocalDateTime.now());
		return log;
	}

	private Attachment getAttachement() {
		Attachment attachment = new Attachment();
		attachment.setFileId("fileId");
		attachment.setThumbnailId("thumbnailId");
		attachment.setContentType("contentType");
		return attachment;
	}
}