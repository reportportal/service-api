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

package com.epam.ta.reportportal.ws.rabbit;

import com.epam.ta.reportportal.binary.DataStoreService;
import com.epam.ta.reportportal.core.events.attachment.DeleteAttachmentEvent;
import com.epam.ta.reportportal.dao.AttachmentRepository;
import com.epam.ta.reportportal.entity.attachment.Attachment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class AttachmentConsumerTest {

	@Mock
	private DataStoreService dataStoreService;

	@Mock
	private AttachmentRepository attachmentRepository;

	@InjectMocks
	private AttachmentConsumer attachmentConsumer;

	@Test
	void consume() {
		long id = 1L;
		DeleteAttachmentEvent event = new DeleteAttachmentEvent(id);

		Attachment attachment = new Attachment();
		attachment.setFileId("fileId");
		attachment.setThumbnailId("thimbnailId");
		attachment.setId(event.getId());
		when(attachmentRepository.findById(event.getId())).thenReturn(Optional.of(attachment));

		attachmentConsumer.onEvent(event);

		verify(attachmentRepository, times(1)).deleteById(event.getId());
		verify(dataStoreService, times(1)).delete(attachment.getFileId());
		verify(dataStoreService, times(1)).delete(attachment.getThumbnailId());
	}
}