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

package com.epam.ta.reportportal.core.project.impl;

import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.attachment.DeleteAttachmentEvent;
import com.epam.ta.reportportal.core.events.attachment.DeleteTestItemAttachmentsEvent;
import com.epam.ta.reportportal.dao.AttachmentRepository;
import com.epam.ta.reportportal.job.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_ID;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class AttachmentEventPublisher {

	private static final Integer ATTACHMENTS_BATCH_SIZE = 300;

	private final AttachmentRepository attachmentRepository;

	private final MessageBus messageBus;

	@Autowired
	public AttachmentEventPublisher(AttachmentRepository attachmentRepository, MessageBus messageBus) {
		this.attachmentRepository = attachmentRepository;
		this.messageBus = messageBus;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
	public void publishDeleteProjectAttachmentsEvent(Long projectId) {
		PageUtil.iterateOverPages(
				ATTACHMENTS_BATCH_SIZE,
				Sort.by(Sort.Order.asc(CRITERIA_ID)),
				pageable -> attachmentRepository.findIdsByProjectId(projectId, pageable),
				this::publishDeleteAttachmentEvent
		);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
	public void publishDeleteLaunchAttachmentsEvent(Long launchId) {
		PageUtil.iterateOverPages(
				ATTACHMENTS_BATCH_SIZE,
				Sort.by(Sort.Order.asc(CRITERIA_ID)),
				pageable -> attachmentRepository.findIdsByLaunchId(launchId, pageable),
				this::publishDeleteAttachmentEvent
		);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
	public void publishDeleteItemAttachmentsEvent(DeleteTestItemAttachmentsEvent event) {
		PageUtil.iterateOverPages(
				ATTACHMENTS_BATCH_SIZE,
				Sort.by(Sort.Order.asc(CRITERIA_ID)),
				pageable -> attachmentRepository.findIdsByTestItemId(event.getItemIds(), pageable),
				this::publishDeleteAttachmentEvent
		);
	}

	private void publishDeleteAttachmentEvent(List<Long> ids) {
		messageBus.publishDeleteAttachmentEvent(new DeleteAttachmentEvent(ids));
	}
}
