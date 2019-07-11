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

import com.epam.ta.reportportal.binary.DataStoreService;
import com.epam.ta.reportportal.commons.BinaryDataMetaInfo;
import com.epam.ta.reportportal.entity.attachment.Attachment;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.ws.converter.builders.AttachmentBuilder;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

/**
 * Save binary data task. Expected to be executed asynchronously. Statefull, so
 * cannot be a singleton bean. Saves binary data, then updates related log entry
 * with saved data id
 * <p>
 * NOTE: run asynchronously in sense of run in Executor. This class is not used with RabbitMQ.
 * It is original implementation for synchronous LogController
 *
 * @author Andrei Varabyeu
 */
public class SaveLogBinaryDataTask implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(SaveLogBinaryDataTask.class);

	@Autowired
	private CreateAttachmentHandler createAttachmentHandler;

	@Autowired
	private DataStoreService dataStoreService;

	/**
	 * Binary data representation
	 */
	private MultipartFile file;

	private Long projectId;

	private Long launchId;

	private Long itemId;
	/**
	 * {@link Log#id} related to this binary data
	 */
	private Long logId;

	@Override
	public void run() {

		Optional<BinaryDataMetaInfo> maybeBinaryDataMetaInfo = dataStoreService.save(projectId, file);

		maybeBinaryDataMetaInfo.ifPresent(binaryDataMetaInfo -> {
			try {
				Attachment attachment = new AttachmentBuilder().withFileId(maybeBinaryDataMetaInfo.get().getFileId())
						.withThumbnailId(maybeBinaryDataMetaInfo.get().getThumbnailFileId())
						.withContentType(file.getContentType())
						.withProjectId(projectId)
						.withLaunchId(launchId)
						.withItemId(itemId)
						.get();

				createAttachmentHandler.create(attachment, logId);
			} catch (Exception exception) {

				LOGGER.error("Cannot save log to database, remove files ", exception);

				dataStoreService.delete(binaryDataMetaInfo.getFileId());
				dataStoreService.delete(binaryDataMetaInfo.getThumbnailFileId());
				throw exception;
			}
		});
	}

	public SaveLogBinaryDataTask withFile(MultipartFile file) {
		Preconditions.checkNotNull(file, "Binary data shouldn't be null");
		this.file = file;
		return this;
	}

	public SaveLogBinaryDataTask withProjectId(Long projectId) {
		Preconditions.checkNotNull(projectId, "Project id should not be null");
		this.projectId = projectId;
		return this;
	}

	public SaveLogBinaryDataTask withLaunchId(Long launchId) {
		Preconditions.checkNotNull(launchId, "Launch id shouldn't be null");
		this.launchId = launchId;
		return this;
	}

	public SaveLogBinaryDataTask withItemId(Long itemId) {
		this.itemId = itemId;
		return this;
	}

	public SaveLogBinaryDataTask withLogId(Long logId) {
		Preconditions.checkNotNull(logId, "Log id shouldn't be null");
		this.logId = logId;
		return this;
	}
}
