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

package com.epam.ta.reportportal.core.project.impl;

import com.epam.ta.reportportal.core.events.attachment.DeleteAttachmentEvent;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.dao.AttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class AttachmentEventPublisher {

	private final AttachmentRepository attachmentRepository;

	private final MessageBus messageBus;

	@Autowired
	public AttachmentEventPublisher(AttachmentRepository attachmentRepository, MessageBus messageBus) {
		this.attachmentRepository = attachmentRepository;
		this.messageBus = messageBus;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
	public void publishDeleteProjectAttachmentsEvent(Long projectId) {

		try (Stream<Long> ids = attachmentRepository.streamIdsByProjectId(projectId)) {
			ids.forEach(this::publishDeleteAttachmentEvent);
		}

	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
	public void publishDeleteLaunchAttachmentsEvent(Long launchId) {

		try (Stream<Long> ids = attachmentRepository.streamIdsByLaunchId(launchId)) {
			ids.forEach(this::publishDeleteAttachmentEvent);
		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
	public void publishDeleteItemAttachmentsEvent(Long itemId) {

		try (Stream<Long> ids = attachmentRepository.streamIdsByItemId(itemId)) {
			ids.forEach(this::publishDeleteAttachmentEvent);
		}
	}

	private void publishDeleteAttachmentEvent(Long id) {
		messageBus.publishDeleteAttachmentEvent(new DeleteAttachmentEvent(id));
	}
}
