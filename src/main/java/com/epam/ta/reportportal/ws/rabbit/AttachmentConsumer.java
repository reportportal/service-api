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
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.events.attachment.DeleteAttachmentEvent;
import com.epam.ta.reportportal.dao.AttachmentRepository;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Component
public class AttachmentConsumer {

	public static final Logger LOGGER = LoggerFactory.getLogger(AttachmentConsumer.class);

	private final DataStoreService dataStoreService;

	private final AttachmentRepository attachmentRepository;

	@Autowired
	public AttachmentConsumer(@Qualifier("attachmentDataStoreService") DataStoreService dataStoreService,
			AttachmentRepository attachmentRepository) {
		this.dataStoreService = dataStoreService;
		this.attachmentRepository = attachmentRepository;
	}

	@RabbitListener(queues = "#{ @deleteAttachmentQueue.name }")
	public void onEvent(@Payload DeleteAttachmentEvent event) {

		List<Long> ids = Lists.newArrayListWithExpectedSize(event.getIds().size());
		event.getIds().forEach(id -> attachmentRepository.findById(id).ifPresent(a -> {
			try {
				ofNullable(a.getFileId()).ifPresent(dataStoreService::delete);
				ofNullable(a.getThumbnailId()).ifPresent(dataStoreService::delete);
				ids.add(id);
			} catch (Exception e) {
				LOGGER.error(Suppliers.formattedSupplier("Error during removing attachment with id = {}", id).get());
			}
		}));
		ofNullable(event.getPaths()).ifPresent(paths -> paths.forEach(dataStoreService::delete));
		attachmentRepository.deleteAllByIds(ids);
	}
}
