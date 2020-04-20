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

package com.epam.ta.reportportal.core.events.handler;

import com.epam.ta.reportportal.core.events.attachment.DeleteLaunchAttachmentsEvent;
import com.epam.ta.reportportal.core.events.attachment.DeleteProjectAttachmentsEvent;
import com.epam.ta.reportportal.core.events.attachment.DeleteTestItemAttachmentsEvent;
import com.epam.ta.reportportal.core.project.impl.AttachmentEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class AttachmentRemovingEventHandler {

	private final AttachmentEventPublisher attachmentEventPublisher;

	@Autowired
	public AttachmentRemovingEventHandler(AttachmentEventPublisher attachmentEventPublisher) {
		this.attachmentEventPublisher = attachmentEventPublisher;
	}

	@TransactionalEventListener
	public void onApplicationEvent(DeleteProjectAttachmentsEvent event) {

		attachmentEventPublisher.publishDeleteProjectAttachmentsEvent(event.getId());
	}

	@TransactionalEventListener
	public void onApplicationEvent(DeleteLaunchAttachmentsEvent event) {

		attachmentEventPublisher.publishDeleteLaunchAttachmentsEvent(event.getId());
	}

	@TransactionalEventListener
	public void onApplicationEvent(DeleteTestItemAttachmentsEvent event) {

		attachmentEventPublisher.publishDeleteItemAttachmentsEvent(event);
	}
}
