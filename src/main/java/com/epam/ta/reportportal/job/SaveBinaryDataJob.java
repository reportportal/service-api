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

package com.epam.ta.reportportal.job;

import com.epam.ta.reportportal.binary.DataStoreService;
import com.epam.ta.reportportal.commons.BinaryDataMetaInfo;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.entity.attachment.Attachment;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.AttachmentBuilder;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

/**
 * Save binary data job. Expected to be executed asynchronously. Statefull, so
 * cannot be a singleton bean. Saves binary data, then updates related log entry
 * with saved data id
 *
 * @author Andrei Varabyeu
 */
@Service("saveBinaryDataJob")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SaveBinaryDataJob implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(SaveBinaryDataJob.class);

	@Autowired
	private LogRepository logRepository;

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
	@Transactional
	public void run() {

		Optional<BinaryDataMetaInfo> maybeBinaryDataMetaInfo = dataStoreService.save(projectId, file);

		maybeBinaryDataMetaInfo.ifPresent(binaryDataMetaInfo -> {
			try {
				Log log = logRepository.findById(logId).orElseThrow(() -> new ReportPortalException(ErrorType.LOG_NOT_FOUND, logId));

				Attachment attachment = new AttachmentBuilder().withFileId(maybeBinaryDataMetaInfo.get().getFileId())
						.withThumbnailId(maybeBinaryDataMetaInfo.get().getThumbnailFileId())
						.withContentType(file.getContentType())
						.withProjectId(projectId)
						.withLaunchId(launchId)
						.withItemId(itemId)
						.get();

				log.setAttachment(attachment);

				logRepository.save(log);
			} catch (Exception exception) {

				LOGGER.error("Cannot save log to database, remove files ", exception);

				dataStoreService.delete(binaryDataMetaInfo.getFileId());
				dataStoreService.delete(binaryDataMetaInfo.getThumbnailFileId());
				throw exception;
			}
		});
	}

	public SaveBinaryDataJob withFile(MultipartFile file) {
		Preconditions.checkNotNull(file, "Binary data shouldn't be null");
		this.file = file;
		return this;
	}

	public SaveBinaryDataJob withProjectId(Long projectId) {
		Preconditions.checkNotNull(projectId, "Project id should not be null");
		this.projectId = projectId;
		return this;
	}

	public SaveBinaryDataJob withLaunchId(Long launchId) {
		Preconditions.checkNotNull(launchId, "Launch id shouldn't be null");
		this.launchId = launchId;
		return this;
	}

	public SaveBinaryDataJob withItemId(Long itemId) {
		Preconditions.checkNotNull(itemId, "Item id shouldn't be null");
		this.itemId = itemId;
		return this;
	}

	public SaveBinaryDataJob withLogId(Long logId) {
		Preconditions.checkNotNull(logId, "Log id shouldn't be null");
		this.logId = logId;
		return this;
	}
}
