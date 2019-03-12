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

package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.entity.attachment.Attachment;

import java.util.function.Supplier;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class AttachmentBuilder implements Supplier<Attachment> {

	private final Attachment attachment;

	public AttachmentBuilder() {
		this.attachment = new Attachment();
	}

	public AttachmentBuilder withFileId(String fileId) {
		attachment.setFileId(fileId);
		return this;
	}

	public AttachmentBuilder withThumbnailId(String thumbnailId) {
		attachment.setThumbnailId(thumbnailId);
		return this;
	}

	public AttachmentBuilder withContentType(String contentType) {
		attachment.setContentType(contentType);
		return this;
	}

	public AttachmentBuilder withProjectId(Long projectId) {
		attachment.setProjectId(projectId);
		return this;
	}

	public AttachmentBuilder withLaunchId(Long launchId) {
		attachment.setLaunchId(launchId);
		return this;
	}

	public AttachmentBuilder withItemId(Long itemId) {
		attachment.setItemId(itemId);
		return this;
	}

	@Override
	public Attachment get() {
		return attachment;
	}
}
