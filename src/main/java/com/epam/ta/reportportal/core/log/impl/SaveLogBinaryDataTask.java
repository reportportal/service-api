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

import com.epam.ta.reportportal.binary.AttachmentDataStoreService;
import com.epam.ta.reportportal.entity.attachment.AttachmentMetaInfo;
import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

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

	@Autowired
	private AttachmentDataStoreService attachmentDataStoreService;

	/**
	 * Binary data representation
	 */
	private MultipartFile file;

	private AttachmentMetaInfo attachmentMetaInfo;

	@Override
	public void run() {
		attachmentDataStoreService.saveFileAndAttachToLog(file, attachmentMetaInfo);
	}

	public SaveLogBinaryDataTask withFile(MultipartFile file) {
		Preconditions.checkNotNull(file, "Binary data shouldn't be null");
		this.file = file;
		return this;
	}

	public SaveLogBinaryDataTask withAttachmentMetaInfo(AttachmentMetaInfo metaInfo) {
		Preconditions.checkNotNull(metaInfo);
		this.attachmentMetaInfo = metaInfo;
		return this;
	}
}
