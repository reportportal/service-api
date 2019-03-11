/*
 * Copyright 2018 EPAM Systems
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
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Component
public class AttachmentConsumer {

	private final DataStoreService dataStoreService;

	private final AttachmentRepository attachmentRepository;

	@Autowired
	public AttachmentConsumer(DataStoreService dataStoreService, AttachmentRepository attachmentRepository) {
		this.dataStoreService = dataStoreService;
		this.attachmentRepository = attachmentRepository;
	}

	@RabbitListener(queues = "#{ @deleteAttachmentQueue.name }")
	public void onEvent(@Payload DeleteAttachmentEvent event) {

		attachmentRepository.findById(event.getId()).ifPresent(a -> {

			attachmentRepository.deleteById(a.getId());
			ofNullable(a.getFileId()).ifPresent(dataStoreService::delete);
			ofNullable(a.getThumbnailId()).ifPresent(dataStoreService::delete);
		});

	}
}
